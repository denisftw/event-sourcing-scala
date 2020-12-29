package util

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ThreadPools {
  implicit val IO = ExecutionContext.
    fromExecutor(Executors.newCachedThreadPool())
  implicit val CPU = ExecutionContext.Implicits.global
}
