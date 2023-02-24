package cbapi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CustomCallbackGenerator {
  def repeatA(name: String, callback: ((Int, String)) => Unit) = {
    def looper() = {
      Range.inclusive(1, 5).foreach { i =>
        callback((i, s"$name: $i"))
      }
    }
    val _ = Future(looper())
  }

  def repeatB(name: String, key: Int, callback: ((Int, String)) => Unit) = {
    def looper() = {
      Range.inclusive(1, 2).foreach { a =>
        callback((key, s"$name (data point $a): ${(key*10)+a.toDouble}"))
      }
    }
    val _ = Future(looper())
  }
}
