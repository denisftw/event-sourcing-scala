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

import scala.concurrent.Future

class MessageLogRegistry(configuration: Configuration, actorSystem: ActorSystem)
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

  private val AllTopics = Set("tags" , "users", "questions" ,"answers")
  private def parseConsumerParams(queue: String): ConsumerParams = {
    val parts = queue.split("\\.")
    val topics = if (parts(1) == "*") AllTopics else Set(parts(1))
    ConsumerParams(parts(0), topics)
  }

  override def shutdown(): Unit = {
    // Not needed
  }

  override def createProducer(topic: String): IMessageProducer = {
    val producerSettings = ProducerSettings(actorSystem,
      new ByteArraySerializer, new ByteArraySerializer)
      .withBootstrapServers(bootstrapServers)
    val producer = producerSettings.createKafkaProducer()
    (bytes: Array[Byte]) => producer.send(new ProducerRecord(topic, bytes))
  }

  override def registerConsumer(queue: String, consumer: IMessageConsumer): Unit = {
    val ConsumerParams(groupName, topics) = parseConsumerParams(queue)
    Consumer.atMostOnceSource(consumerSettings(groupName),
      Subscriptions.topics(topics)).mapAsync(parallelism = 1) { msg =>
      val event = msg.value()
      consumer.messageReceived(event)
      Future.successful(msg)
    }.toMat(Sink.ignore)(DrainingControl.apply).run()
  }

  case class ConsumerParams(groupName: String, topics: Set[String])
}
