package zstreamapp

import cbapi.CbApi
import zio._
import zio.stream.ZStream
import zio.test._

object CbStreamFlatmapSpec extends ZIOSpecDefault {
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
      test("full overlap intersect test with stream b from ZStream.fromIterable") {
        val streamA = for {
          subscriptionManager <- ZStream.fromZIO(apiSubscriptionManager)
          itemAKey <- subscriptionManager.subscribeToApiChannelA(1000, "location").map(_._1).take(2)
        } yield itemAKey
        val flatMapped = streamA.flatMapPar(10)(a => streamB(a).map(b => (a, b)))
        for {
          data <- flatMapped.tap(a => ZIO.logInfo(s"tap: $a")).runCollect
        } yield assertTrue {
          data == Chunk((1, (1, "11")), (1, (2, "12")), (2, (1, "21")), (2, (2, "22")))
        }
      },

      test ("full overlap intersect par test with stream b from ZStream.async") {
        val streamA: ZStream[Any, Throwable, (SubscriptionManager, Int)] = for {
          subscriptionManager <- ZStream.fromZIO(apiSubscriptionManager)
          itemAKey <- subscriptionManager.subscribeToApiChannelA(1000, "location").map(_._1)
        } yield (subscriptionManager, itemAKey)
        val flatMapped = streamA.take(2).flatMapPar(1, 1)(a => a._1.subscribeToApiChannelB("temperature", a._2).map(b => (a._2, b)).take(3))
        for {
          fibre <- flatMapped.tap(a => ZIO.logInfo(s"tap: $a")).runCollect.fork
          data <- fibre.await
        } yield assertTrue {
          data.toEither.toOption.get.sorted == Chunk(
            (1, (1, "temperature (data point 1): 11.0")),
            (1, (1, "temperature (data point 2): 12.0")),
            (1, (1, "temperature (data point 3): 13.0")),
            (2, (2, "temperature (data point 1): 21.0")),
            (2, (2, "temperature (data point 2): 22.0")),
            (2, (2, "temperature (data point 3): 23.0")))
        }
      }
    )
}
