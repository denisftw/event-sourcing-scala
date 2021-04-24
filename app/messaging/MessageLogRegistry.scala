package messaging

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}
import play.api.{Configuration, Logger}
import akka.kafka.ConsumerMessage
import akka.kafka.scaladsl.Committer
import akka.kafka.CommitterSettings
import org.apache.kafka.clients.admin.AdminClient
import java.util.Properties
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import views.html.pages.admin
import scala.util.Using
import java.util.concurrent.CopyOnWriteArrayList
import org.apache.kafka.clients.producer.Producer
import java.util.concurrent.CopyOnWriteArraySet

class MessageLogRegistry(configuration: Configuration,
                         actorSystem: ActorSystem)
                        (implicit val mat: Materializer)
  extends IMessageProcessingRegistry {
  private val log = Logger(this.getClass)
  private val bootstrapServers = configuration.
    get[String]("kafka.bootstrap.servers")
  private val offsetReset = configuration.
    get[String]("kafka.auto.offset.reset")

  private def consumerSettings(groupName: String) = ConsumerSettings(actorSystem,
    new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(groupName)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset)

  private val AllTopics = Set("tags", "users", "questions", "answers")
  private val createdProducers = new CopyOnWriteArraySet[Producer[_, _]]()

  initTopics()

  private def initTopics(): Unit = {
    val numPartitions = 1
    val replicationFactor = 1.toShort
    val adminConfig = {
      val result = new Properties()
      result.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      result
    }
    Using(AdminClient.create(adminConfig)) { adminClient =>
      val newTopics = AllTopics.map { topicName =>
        new NewTopic(topicName, numPartitions, replicationFactor)
      }
      import scala.jdk.CollectionConverters._
      adminClient.createTopics(newTopics.asJava)
    }
  }

  private def parseConsumerParams(queue: String): ConsumerParams = {
    val parts = queue.split("\\.")
    val topics = if (parts(1) == "*") AllTopics else Set(parts(1))
    ConsumerParams(parts(0), topics)
  }

  override def shutdown(): Unit = {
    createdProducers.forEach(_.close())
  }

  override def createProducer(topic: String): IMessageProducer = {
    val producerSettings = ProducerSettings(actorSystem,
      new ByteArraySerializer, new ByteArraySerializer)
      .withBootstrapServers(bootstrapServers)
    val producer = producerSettings.createKafkaProducer()
    createdProducers.add(producer)
    (bytes: Array[Byte]) => producer.send(new ProducerRecord(topic, bytes))
  }

  override def registerConsumer(queue: String, consumer: IMessageConsumer): Unit = {
    val ConsumerParams(groupName, topics) = parseConsumerParams(queue)
    val committerSettings = CommitterSettings.create(actorSystem).withMaxBatch(1)
    val committerFlow = Committer.flow(committerSettings)
    Consumer.committableSource(consumerSettings(groupName), 
      Subscriptions.topics(topics)).map { message =>
      val ConsumerMessage.CommittableMessage(record, _) = message
      consumer.messageReceived(record.value())
      message.committableOffset
    }.via(committerFlow).toMat(Sink.ignore)(DrainingControl.apply).run()
  }

  case class ConsumerParams(groupName: String, topics: Set[String])
}
