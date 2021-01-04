package messaging

trait IMessageConsumer {
  def messageReceived(message: Array[Byte]): Unit
}
