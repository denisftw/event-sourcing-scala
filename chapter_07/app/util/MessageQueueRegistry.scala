package util

import com.rabbitmq.client.{BuiltinExchangeType, CancelCallback, ConnectionFactory, DeliverCallback}
import play.api.{Configuration, Logger}

class MessageQueueRegistry(configuration: Configuration) extends IMessageProcessingRegistry {
  private val log = Logger(this.getClass)
  private lazy val MessageBrokerHost = configuration.get[String]("messageBroker.host")
  private lazy val ConnectionFactory = {
    val factory = new ConnectionFactory
    factory.setHost(MessageBrokerHost)
    factory
  }
  private lazy val ProducerConnection = ConnectionFactory.newConnection
  private lazy val ConsumerConnection = ConnectionFactory.newConnection

  def shutdown(): Unit = {
    log.info("Closing message broker connections")
    ProducerConnection.close()
    ConsumerConnection.close()
  }

  private val logQueueName = "log.*"
  def createProducer(exchange: String): IMessageProducer = {
    val readQueueName = s"read.$exchange"
    log.info(s"Creating an exchange for '$exchange' and binding it to '$logQueueName' and '$readQueueName'")
    val channel = ProducerConnection.createChannel
    channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT)
    channel.queueDeclare(logQueueName, false, false, false, null)
    channel.queueDeclare(readQueueName, false, false, false, null)
    channel.queueBind(logQueueName, exchange, "")
    channel.queueBind(readQueueName, exchange, "")
    (message: Array[Byte]) => {
      channel.basicPublish(exchange, "", null, message)
    }
  }

  def registerConsumer(queue: String, consumer: IMessageConsumer): Unit = {
    log.info(s"Registering a new consumer for '$queue'")
    val channel = ConsumerConnection.createChannel
    channel.queueDeclare(queue, false, false, false, null)
    val deliverCallback: DeliverCallback = (_, delivery) => {
      consumer.messageReceived(delivery.getBody)
      channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
    }
    val cancelCallback: CancelCallback = _ => {}
    channel.basicConsume(queue, false, deliverCallback, cancelCallback)
  }
}
