package cbapi

import cbapi.CbApi.Subscriber
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CbApi {
  implicit val log = Logger(this.getClass)

  def subscribeToA(delay: Long, subscriber: Subscriber): Unit = {
    CustomCallbackGenerator.repeatA(delay, "location", subscriber.onItem)
  }

  def subscribeToB(key: Int, subscriber: Subscriber): Unit = {
    CustomCallbackGenerator.repeatB("temperature", key, subscriber.onItem)
  }
}
object CbApi {
  type Item = (Int, String)

  def start(): Future[CbApi] = Future(new CbApi)

  trait Subscriber {
    def onItem(item: Item): Unit
  }
}
