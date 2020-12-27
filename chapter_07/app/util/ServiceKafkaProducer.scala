/*
package util

import akka.actor.ActorSystem



import play.api.Configuration


class ServiceKafkaProducer(topicName: String,
    actorSystem: ActorSystem, configuration: Configuration) {

  val bootstrapServers = configuration.
    get[String]("kafka.bootstrap.servers")

  import akka.kafka.ProducerSettings
  import org.apache.kafka.common.serialization.ByteArraySerializer

  val producerSettings = ProducerSettings(actorSystem,
    new ByteArraySerializer, new ByteArraySerializer)
    .withBootstrapServers(bootstrapServers)

  val producer = producerSettings.createKafkaProducer()

  import org.apache.kafka.clients.producer.ProducerRecord
  def send(logRecord: Array[Byte]): Unit = {
    producer.send(new ProducerRecord(topicName, logRecord))
  }
}*/
