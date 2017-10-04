package util

import akka.actor.ActorSystem



import play.api.Configuration


class ServiceKafkaProducer(topicName: String,
    actorSystem: ActorSystem, configuration: Configuration) {

  val bootstrapServers = configuration.
    getOptional[String]("kafka.bootstrap.servers").getOrElse(
    throw new Exception(
      "No config element for 'kafka.bootstrap.servers'!"))

  import akka.kafka.ProducerSettings
  import org.apache.kafka.common.serialization.StringSerializer

  val producerSettings = ProducerSettings(actorSystem,
    new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  val producer = producerSettings.createKafkaProducer()

  import org.apache.kafka.clients.producer.ProducerRecord
  def send(logRecordStr: String): Unit = {
    producer.send(new ProducerRecord(topicName, logRecordStr))
  }
}