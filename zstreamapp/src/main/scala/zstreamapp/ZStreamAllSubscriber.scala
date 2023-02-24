package zstreamapp

import cbapi.CbApi
import cbapi.CbApi.Item
import com.typesafe.scalalogging.Logger
import zio.stream.ZStream
import zio.{Chunk, Task, ZIO}

import scala.util.Try

class ZStreamAllSubscriber(name: String,
                           onUpdate: Item => Unit,
                           onError: Throwable => Unit) extends CbApi.Subscriber {
  val log = Logger(this.getClass)

  def onItem(item: Item): Unit = {
    Try {
      log.info(s"onItem $name: $item")
      item
    }.fold(onError, onUpdate)
  }
}

object ZStreamAllSubscriber {
  val log = Logger(this.getClass)

  def registerCallback(name: String, subscribe: CbApi.Subscriber => Unit, onUpdate: Item => Unit, onError: Throwable => Unit): Task[Unit] = {
    log.debug(s"registering $name callback")
    ZIO.attempt {
      val subscriber = new ZStreamAllSubscriber(name, onUpdate, onError)
      subscribe(subscriber)
      log.debug(s"registered $name callback")
    }
  }

  def register(name: String, subscribe: CbApi.Subscriber => Unit): ZStream.Emit[Any, Throwable, Item, Unit] => Task[Unit] =
    (cb: ZStream.Emit[Any, Throwable, Item, Unit]) =>
      registerCallback(
        name,
        subscribe,
        event => cb(ZIO.succeed(Chunk.single(event))),
        error => cb(ZIO.fail(error).mapError(Some(_)))
      )

  def stream(name: String, subscribe: CbApi.Subscriber => Unit): ZStream[Any, Throwable, Item] =
    ZStream.asyncZIO((register(name, subscribe)))
}
