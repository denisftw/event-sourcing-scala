package messaging

trait IMessageProducer {
  def send(bytes: Array[Byte]): Unit
}
