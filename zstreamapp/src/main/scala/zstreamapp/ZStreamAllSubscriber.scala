package zstreamapp

import cbapi.CbApi
import cbapi.CbApi.Item
import com.typesafe.scalalogging.Logger
import zio._
import zio.stream.ZStream

import scala.util.Try

class ZStreamAllSubscriber(name: String,
                           onUpdate: Item => Unit,
                           onError: Throwable => Unit) extends CbApi.Subscriber {
  val log = Logger(this.getClass)

  def onItem(item: Item): Unit = {
    Try {
      log.info(s"onItem $name: $item")
      item
    }.fold(onError, (item: Item) => {
      log.debug(s"passing $item to onUpdate")
      onUpdate(item)
    })
  }
}

object ZStreamAllSubscriber {
  val log = Logger(this.getClass)

  def registerCallback(name: String, subscribe: CbApi.Subscriber => Unit, onUpdate: Item => Unit, onError: Throwable => Unit): Task[Unit] = {
    log.debug(s"registering $name callback")
    ZIO.attempt {
      val subscriber = new ZStreamAllSubscriber(name, onUpdate, onError)
      log.debug(s"registering and starting callback $name")
      subscribe(subscriber)
      log.debug(s"registered and started callback $name")
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

  def registerQueue(name: String, subscribe: CbApi.Subscriber => Unit, queue: Queue[Item]): Task[Unit] =
    registerCallback(
      name,
      subscribe,
      event => {
        log.debug(s"offering $event to queue...")
        val _ = Unsafe.unsafe { implicit unsafe =>
          log.debug(s"offering $event to queue (inner)...")
          val exit = Runtime.default.unsafe.run(
            for {
              _ <- ZIO.succeed({
                log.debug(s"offering $event to queue (inner 2)...")
                for {
                  queued <- queue.offer(event)
                  _ = log.debug(s"...offered $event to queue (inner 2) result=$queued")
                } yield ()
              }).fork
            } yield ())
          exit.foldExit(_ => log.error("failed"), _ => log.info("success"))
          log.debug(s"...offered $event to queue (inner)")
        }
        log.debug(s"...offered $event to queue")
      },
      error => log.error(s"$error")
    )

  def stream(name: String, subscribe: CbApi.Subscriber => Unit): ZStream[Any, Throwable, Item] =
    ZStream.asyncZIO((register(name, subscribe)))

  def streamFromQueue0(name: String, subscribe: CbApi.Subscriber => Unit): ZStream[Any, Throwable, Item] = {
    val queue: ZIO[Any, Throwable, Queue[(Int, String)]] = for {
      q <- Queue.unbounded[Item]
      _ <- registerQueue(name, subscribe, q)
    } yield q
    ZStream.fromZIO(queue).flatMap(a => ZStream.fromQueue(a))
  }

  def streamFromQueue1(name: String, subscribe: CbApi.Subscriber => Unit): ZIO[Any, Throwable, ZStream[Any, Nothing, Item]] = {
    for {
      q <- Queue.bounded[Item](100)
      stream = ZStream.fromQueue(q, 1).debug("FROM QUEUE: ")
      _ <- registerQueue(name, subscribe, q)
    } yield stream
  }
}
