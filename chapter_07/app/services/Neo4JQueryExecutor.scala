package services

import org.neo4j.driver.summary.SummaryCounters
import org.neo4j.driver.{AuthTokens, GraphDatabase, Session}

import java.util.UUID
import org.neo4j.driver.Record
import scala.util.Try
import play.api.{Logger => PlayLogger}

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
class Neo4JQueryExecutor(configuration: Configuration) {

  val log = PlayLogger(this.getClass)
  val config = configuration.get[Configuration]("neo4j")

  val driver = GraphDatabase.driver(config.get[String]("url"),
    AuthTokens.basic(config.get[String]("username"), config.get[String]("password")))

  private def doWithSession[A](block: Session => A): Try[A] = {
    val session = driver.session()
    val resultT = Try {
      block(session)
    }
    session.close()
    resultT
  }


  def executeBatch(updates: Seq[Neo4JQuery]): Try[Unit] = {
    val session = driver.session()

    val result = Try {
      val transaction = session.beginTransaction()
      updates.foreach { update =>
        transaction.run(update.query, update.paramsAsJava)
      }
      transaction.commit()
      transaction.close()
    }
    result.recover { case th =>
      // TODO: [LOGBACK-1027]
      log.error("Error occurred while executing the Neo4J batch", th)
    }
    session.close()
    result
  }

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
      import collection.JavaConverters._
      result.asScala.toList
    }
  }
}
