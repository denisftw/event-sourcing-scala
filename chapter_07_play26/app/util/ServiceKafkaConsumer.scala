package util

import akka.actor.ActorSystem


import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import play.api.Configuration

import scala.concurrent.Future

/**
  * Created by denis on 12/3/16.
  */
class ServiceKafkaConsumer(topicNames: Set[String], groupName: String,
    implicit val mat: Materializer, actorSystem: ActorSystem,
    configuration: Configuration, handleEvent: String => Unit) {

  val config = configuration.getOptional[Configuration]("kafka").
    getOrElse(throw new Exception("No config element for Kafka!")).
    underlying

  import akka.kafka.{ConsumerSettings, Subscriptions}
  import org.apache.kafka.common.serialization.StringDeserializer
  import org.apache.kafka.clients.consumer.ConsumerConfig

  val consumerSettings = ConsumerSettings(actorSystem,
    new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(config.getString("bootstrap.servers"))
    .withGroupId(groupName)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
      config.getString("auto.offset.reset"))

  import akka.kafka.scaladsl.Consumer
  Consumer.committableSource(consumerSettings,
    Subscriptions.topics(topicNames)).mapAsync(1) { msg =>
    val event = msg.record.value()
    handleEvent(event)
    Future.successful(msg)
  }.mapAsync(1) { msg =>
    msg.committableOffset.commitScaladsl()
  }.runWith(Sink.ignore)

  val blah = Consumer.committableSource(consumerSettings,
    Subscriptions.topics(topicNames))
}
