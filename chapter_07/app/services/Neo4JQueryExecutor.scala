package services

import org.neo4j.driver.summary.SummaryCounters
import org.neo4j.driver.{AuthTokens, GraphDatabase, Session}

import java.util.UUID
import org.neo4j.driver.Record

import scala.util.{Try, Using}
import play.api.{Logger => PlayLogger}

import scala.concurrent.Future

case class Neo4JUpdate(queries: Seq[Neo4JQuery], updateId: Option[UUID] = None)
case class Neo4JQuery(query: String, params: Map[String, AnyRef]) {
  def paramsAsJava: java.util.Map[String, AnyRef] = {
    import scala.jdk.CollectionConverters._
    params.asJava
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

  import util.ThreadPools.IO

  private def doWithSession[A](block: Session => A): Future[A] = {
    Future {
      Using.resource(driver.session()) { session =>
        block(session)
      }
    }
  }

  def executeBatch(updates: Seq[Neo4JQuery]): Future[Unit] = {
    Future.delegate {
      Using.resource(driver.session()) { session =>
        Using.resource(session.beginTransaction()) { transaction =>
          val result = Future.traverse(updates) { update =>
            transaction.run(update.query, update.paramsAsJava)
            Future.successful(())
          }
          transaction.commit()
          result.map(_ => ())
        }
      }
    }
  }

  def executeUpdate(update: Neo4JQuery): Future[SummaryCounters] = {
    doWithSession { session =>
      val result = session.run(update.query, update.paramsAsJava)
      val summary = result.consume()
      summary.counters()
    }
  }

  def executeQuery(query: Neo4JQuery): Future[Seq[Record]] = {
    doWithSession { session =>
      val result = session.run(query.query, query.paramsAsJava)
      import scala.jdk.CollectionConverters._
      result.asScala.toSeq
    }
  }
}
