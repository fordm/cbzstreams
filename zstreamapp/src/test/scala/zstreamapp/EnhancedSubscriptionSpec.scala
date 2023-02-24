package zstreamapp

import service.ApiServiceLocal
import zio._
import zio.test._

object EnhancedSubscriptionSpec extends ZIOSpecDefault {

  def spec =
    suite("EnhancedSubscriptionSpec")(
      test("empty") {
        for {
          result <- EnhancedSubscription.subscribeIntersect().runCollect
        } yield assertTrue {
          result == Chunk.empty[(Int, (Int, String))]
        }
      }.provide(emptyLayerA, emptyLayerB, ApiServiceLocal.layer),

      test("full overlap") {
        for {
          result <- EnhancedSubscription.subscribeIntersect().tap(a => ZIO.logInfo(s"tap: $a")).runCollect
        } yield assertTrue {
          result == Chunk((1, (1, "10")), (2, (2, "20")))
        }
      }.provide(fullOverlapLayerA, fullOverlapLayerB, ApiServiceLocal.layer)
    )

  val fullOverlapDataA: List[(Int, String)] = List((1, "Place1"), (2, "Place2"))
  val fullOverlapDataB: Map[Int, List[(Int, String)]] = Map(1 -> List((1, "10")), 2 -> List((2, "20")))
  val fullOverlapLayerA = ZLayer.succeed(fullOverlapDataA)
  val fullOverlapLayerB = ZLayer.succeed(fullOverlapDataB)

  val emptyDataA: List[(Int, String)] = List()
  val emptyDataB: Map[Int, List[(Int, String)]] = Map.empty
  val emptyLayerA = ZLayer.succeed(emptyDataA)
  val emptyLayerB = ZLayer.succeed(emptyDataB)
}
