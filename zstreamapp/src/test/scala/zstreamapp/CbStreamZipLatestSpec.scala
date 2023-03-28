package zstreamapp

import cbapi.CbApi
import com.typesafe.scalalogging.Logger
import zio._
import zio.stream.ZStream
import zio.test._

object CbStreamZipLatestSpec extends ZIOSpecDefault {
  val log = Logger(this.getClass)
  def apiSubscriptionManager: Task[SubscriptionManager] = for {
    api <- ZIO.fromFuture(_ => CbApi.start())
  } yield SubscriptionManager(api)

  def streamB(key: Int): ZStream[Any, Nothing, (Int, String)] = {
    if (key == 1)
      ZStream.fromIterable(List((1, "11"), (2, "12")))
    else if (key == 2)
      ZStream.fromIterable(List((1, "21"), (2, "22")))
    else
      ZStream.empty
  }

  def spec =
    suite("ZStream.async flatMap Spec")(
      test("full overlap zipLatest test with stream b from ZStream.fromIterable") {
        log.info("running test 1")
        val subscriptionManagerTask: Task[SubscriptionManager] = apiSubscriptionManager
        val streamA = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemA <- subscriptionManager.subscribeToApiChannelA(1000, "location")
        } yield itemA
        val combined = streamA
          .tap(a => ZIO.succeed(log.info(s"tap1A: $a")))
          .take(2)
          .zipLatest(streamB(1).tap(a => ZIO.succeed(log.info(s"tap1B: $a"))))
        for {
          fibre <- combined.tap(a => ZIO.succeed(log.info(s"tap1Zip: $a"))).runCollect.fork
          data <- fibre.await
        } yield assertTrue {
          data.toEither.toOption.get == Chunk(
            (1, "location: 1", (1, "11")),
            (1, "location: 1", (2, "12")),
            (2, "location: 2", (2, "12")))
        }
      },

      test("full overlap zipLatest test with stream b from ZStream.async") {
        log.info("running test 2")
        val subscriptionManagerTask: Task[SubscriptionManager] = apiSubscriptionManager
        val streamA: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemA <- subscriptionManager.subscribeToApiChannelA(1000, "location")
        } yield itemA
        val streamBAsync: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemB <- subscriptionManager.subscribeToApiChannelB("temperature", 1)
        } yield itemB
        val combined = streamA
          .tap(a => ZIO.succeed(log.info(s"tap2A: $a")))
          .take(2)
          .zipLatest(streamBAsync
            .tap(a => ZIO.succeed(log.info(s"tap2B: $a")))
            .take(3))
        for {
          fibre <- combined.tap(a => ZIO.succeed(log.info(s"tap2Zip: $a"))).runCollect.fork
          data <- fibre.await
        } yield assertTrue {
          data.toEither.toOption.get == Chunk(
            (1, "location: 1", (1, "temperature (data point 1): 11.0")),
            (1, "location: 1", (1, "temperature (data point 2): 12.0")),
            (1, "location: 1", (1, "temperature (data point 3): 13.0")),
            (2, "location: 2", (1, "temperature (data point 3): 13.0")))
        }
      },

      test("full overlap zipLatest and dropWhile test with stream b from ZStream.async") {
        log.info("running test 3")
        val subscriptionManagerTask: Task[SubscriptionManager] = apiSubscriptionManager
        val streamA: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemA <- subscriptionManager.subscribeToApiChannelA(1000, "location")
        } yield itemA
        val streamBAsync: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemB <- subscriptionManager.subscribeToApiChannelB("temperature", 1)
        } yield itemB
        val combined = streamA
          .tap(a => ZIO.succeed(log.info(s"tap3A: $a")))
          .dropWhile(_._1 < 4)
          .take(2)
          .zipLatest(streamBAsync
            .tap(a => ZIO.succeed(log.info(s"tap3B: $a")))
            .take(3))
        for {
          fibre <- combined.tap(a => ZIO.succeed(log.info(s"tap3Zip: $a"))).runCollect.fork
          data <- fibre.await
        } yield assertTrue {
          data.toEither.toOption.get == Chunk(
            (4, "location: 4", (1, "temperature (data point 1): 11.0")),
            (4, "location: 4", (1, "temperature (data point 2): 12.0")),
            (4, "location: 4", (1, "temperature (data point 3): 13.0")),
            (5, "location: 5", (1, "temperature (data point 3): 13.0")))
        }
      },

      test ("full overlap zipLatest and dropWhile test with stream b from ZStream.async -- should fill buffer B for a while before zipping") {
        log.info("running test 4")
        val subscriptionManagerTask: Task[SubscriptionManager] = apiSubscriptionManager
        val streamA: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemA <- subscriptionManager.subscribeToApiChannelA(1000*40, "location")
        } yield itemA
        val streamBAsync: ZStream[Any, Throwable, (Int, String)] = for {
          subscriptionManager <- ZStream.fromZIO(subscriptionManagerTask)
          itemB <- subscriptionManager.subscribeToApiChannelB("temperature", 1)
        } yield itemB
        val combined = streamA
          .tap(a => ZIO.succeed(log.info(s"tap3A: $a")))
          .dropWhile(_._1 < 2)
          .take(1)
          .zipLatest(streamBAsync
            .tap(a => ZIO.succeed(log.info(s"tap3B: $a")))
          )
        for {
          fibre <- combined.tap(a => ZIO.succeed(log.info(s"tap3Zip: $a"))).take(4).runCollect.fork
          data <- fibre.await
        } yield assertTrue {
          data.toEither.toOption.get == Chunk(
            (2, "location: 2", (1, "temperature (data point 1): 11.0")),
            (2, "location: 2", (1, "temperature (data point 2): 12.0")),
            (2, "location: 2", (1, "temperature (data point 3): 13.0")),
            (2, "location: 2", (1, "temperature (data point 4): 14.0")))
        }
      } // @@ TestAspect.timeout(1.second)

    )
}
