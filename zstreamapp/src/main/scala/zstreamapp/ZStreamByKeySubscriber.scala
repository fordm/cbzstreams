package zstreamapp

import cbapi.CbApi
import cbapi.CbApi.Item
import com.typesafe.scalalogging.Logger
import zio.stream.ZStream
import zio.{Chunk, Task, ZIO}

import scala.util.Try

class ZStreamByKeySubscriber(name: String,
                             key: Int,
                             onUpdate: Item => Unit,
                             onError: Throwable => Unit) extends CbApi.Subscriber {
  val log = Logger(this.getClass)

  def onItem(item: Item): Unit = {
    Try {
      log.info(s"onItem for key=$key $name: $item")
      item
    }.fold(onError, onUpdate)
  }
}

object ZStreamByKeySubscriber {
  val log = Logger(this.getClass)

  def registerCallback(name: String, key: Int, subscribe: CbApi.Subscriber => Unit, onUpdate: Item => Unit, onError: Throwable => Unit): Task[Unit] = {
    log.debug(s"registering $name callback for key $key")
    ZIO.attempt {
      val subscriber = new ZStreamByKeySubscriber(name, key, onUpdate, onError)
      subscribe(subscriber)
      log.debug(s"registered $name callback for key $key")
    }
  }

  def register(name: String, key: Int, subscribe: CbApi.Subscriber => Unit): ZStream.Emit[Any, Throwable, Item, Unit] => Task[Unit] =
    (cb: ZStream.Emit[Any, Throwable, Item, Unit]) =>
      registerCallback(
        name,
        key,
        subscribe,
        event => cb(ZIO.succeed(Chunk.single(event))),
        error => cb(ZIO.fail(error).mapError(Some(_)))
      )

  def stream(name: String, key: Int, subscribe: CbApi.Subscriber => Unit): ZStream[Any, Throwable, Item] =
    ZStream.asyncZIO((register(name, key, subscribe)))
}
