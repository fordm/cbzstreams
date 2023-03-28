package cbapi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CustomCallbackGenerator {
  def repeatA(delay: Long, name: String, callback: ((Int, String)) => Unit) = {
    def looper() = {
      Range.inclusive(1, 5000).foreach { i =>
        Thread.sleep(delay)
        callback((i, s"$name: $i"))
      }
    }
    val _ = Future(looper())
  }

  def repeatB(name: String, key: Int, callback: ((Int, String)) => Unit) = {
    def looper() = {
      Range.inclusive(1, 5000).foreach { a =>
        Thread.sleep(400)
        callback((key, s"$name (data point $a): ${(key*10)+a.toDouble}"))
      }
    }
    val _ = Future(looper())
  }
}
