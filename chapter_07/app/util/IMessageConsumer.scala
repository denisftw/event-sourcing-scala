package util

trait IMessageConsumer {
  def messageReceived(message: Array[Byte]): Unit
}
