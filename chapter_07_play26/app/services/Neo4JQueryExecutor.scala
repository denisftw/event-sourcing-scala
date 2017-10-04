package services

import java.util.UUID

import scala.collection.mutable.ListBuffer
import scala.util.Try

case class Neo4JUpdate(queries: Seq[Neo4JQuery], updateId: Option[UUID] = None)
case class Neo4JQuery(query: String, params: Map[String, AnyRef]) {
  def paramsAsJava: java.util.Map[String, AnyRef] = {
    import collection.JavaConverters._
    mapAsJavaMap(params)
  }
}
object Neo4JQuery {
  def simple(query: String): Neo4JQuery = Neo4JQuery(query, Map.empty[String, AnyRef])
}

import play.api.Configuration
import org.neo4j.driver.v1._
class Neo4JQueryExecutor(configuration: Configuration) {

  val config = configuration.getOptional[Configuration]("neo4j").
    getOrElse(throw new Exception("No config element for Neo4J!")).underlying

  val driver = GraphDatabase.driver(config.getString("url"),
    AuthTokens.basic(config.getString("username"), config.getString("password")))

  private def doWithSession[A](block: Session => A): Try[A] = {
    val session = driver.session()
    val resultT = Try {
      block(session)
    }
    session.close()
    resultT
  }

  import play.api.{Logger => PlayLogger}
  def executeBatch(updates: Seq[Neo4JQuery]): Try[Unit] = {
    val session = driver.session()

    val result = Try {
      val transaction = session.beginTransaction()
      updates.foreach { update =>
        transaction.run(update.query, update.paramsAsJava)
      }
      transaction.success()
      transaction.close()
    }
    result.recover { case th =>
      PlayLogger.error("Error occurred while executing the Neo4J batch", th)
    }
    session.close()
    result
  }

  import org.neo4j.driver.v1.summary.SummaryCounters
  def executeUpdate(update: Neo4JQuery): Try[SummaryCounters] = {
    doWithSession { session =>
      val result = session.run(update.query, update.paramsAsJava)
      val summary = result.consume()
      summary.counters()
    }
  }

  def executeQuery(query: Neo4JQuery): Try[Seq[Record]] = {
    doWithSession { session =>
      val result = session.run(query.query, query.paramsAsJava)
      val recordBuffer = ListBuffer.newBuilder[Record]
      while (result.hasNext) {
        recordBuffer += result.next()
      }
      recordBuffer.result().toList
    }
  }
}
