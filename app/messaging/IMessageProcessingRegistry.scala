package messaging

trait IMessageProcessingRegistry {
  def shutdown(): Unit
  def createProducer(exchange: String): IMessageProducer
  def registerConsumer(queue: String, consumer: IMessageConsumer): Unit
}
