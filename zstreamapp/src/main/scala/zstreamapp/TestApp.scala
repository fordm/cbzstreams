package zstreamapp

//import cbapi.CbApi
import com.typesafe.scalalogging.Logger
import zio._
import zio.stream._

import scala.annotation.nowarn

@nowarn
object TestApp extends ZIOAppDefault with ZioLogging {
  val log = Logger(this.getClass)

  val tickStream: ZStream[Any, Nothing, UIO[Int]] = ZStream.tick(1.second).map(_ => Random.nextInt)

  val tickQueueProcessDoesNotWork = for {
    q <- Queue.bounded[Int](100)
    _ <- q.offer(-2)
    consumer = ZStream.fromQueue(q, 1).tap(a => ZIO.logDebug(s"consumer from queue: $a"))
    _ <- q.offer(-1)
    _ = Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(
        tickStream.map(_.map { a =>
          log.debug(s"offer $a...")
          val added = q.offer(a)
          log.debug(s"...offered $a $added")
        }).runDrain
      )
    }
  } yield consumer

  val tickQueueProcess = for {
    q <- Queue.bounded[Int](5)
    _ <- q.offer(-5)
    _ <- q.offer(-4)
    consumer = ZStream
      .fromQueue(q, 1)
      //.flattenTake
      .aggregateAsync(
        ZSink
          .foldLeft[Int, List[Int]](Nil) { case (l, n) => n :: l }
      )
      .tap(a => ZIO.logDebug(s"consumer from queue: $a"))
    _ <- q.offer(-3)
    _ <- q.offer(-2)
    _ <- q.offer(-1)
    producer = tickStream.map(_.map { a =>
          //log.debug(s"offer $a...")
          val added = q.offer(a)
          //log.debug(s"...offered $a $added")
          a
        }).tap(a => a.flatMap(b => ZIO.logDebug(s"producer to queue: $b")))
    _ <- producer.runDrain.fork
    both = consumer
  } yield both

  def toFromQueue: ZIO[Any with Scope, Nothing, ZStream[Any, Nothing, Take[Nothing, Int]]] = {
    val q = ZStream
      .range(0, 10)
      .toQueue(1)

    // val s = q.flatMap(q => ZStream.fromQueue(q, 1).map(_.exit).flattenExitOption.runCollect)
    val s = q.map(q => ZStream.fromQueue(q, 1)) //.map(_.exit).flattenExitOption.runCollect)
    s
    //.map(_.flatMap(_.toList))
  }
  /*def apiSubscriptionManager: Task[SubscriptionManager] = for {
    api <- ZIO.fromFuture(_ => CbApi.start())
  } yield SubscriptionManager(api)

  lazy val subscriptionManagerTask: Task[SubscriptionManager] = apiSubscriptionManager

  lazy val streamA: ZIO[Any, Throwable, ZStream[Any, Nothing, (Int, String)]] = for {
    subscriptionManager <- subscriptionManagerTask
    itemA <- subscriptionManager.subscribeToApiChannelAQueue(1000, "location")
  } yield itemA

  def process: ZIO[Any, Nothing, ZStream[Any, Nothing, Any]] = {
    val result: ZIO[Any, Nothing, ZStream[Any, Nothing, Any]] = for {
      queue <- Queue.bounded[Int](1)
      consumer = ZStream.fromQueue(queue).debug
      producer = ZStream.fromZIO(ZIO.foreach(Range(1, 10).toList)(a => {
        val _ = queue.offer(a)
        ZIO.unit
      }))
      zipped = consumer.merge(producer)
    } yield zipped
    result
  }*/

  def runLive: Task[Unit] = for {
    // stream <- process
    // stream <- streamA
    _ <- ZIO.logInfo("starting")
    // stream <- tickQueueProcess
    ///stream <- toFromQueue
    _ <- ZIO.logInfo("forking")
    ///fibre <- stream.tap(a => ZIO.logInfo(s"tapRun: $a")).runDrain.fork
    _ <- ZIO.logInfo("waiting")
    ///_ <- fibre.await
    _ <- ZIO.logInfo("complete")
  } yield ()

  override def run = runLive
}
