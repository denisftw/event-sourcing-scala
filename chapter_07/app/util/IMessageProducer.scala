package util

trait IMessageProducer {
  def send(bytes: Array[Byte]): Unit
}
