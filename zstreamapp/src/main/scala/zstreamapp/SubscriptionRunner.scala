package zstreamapp

import service.{Api, ApiService, ApiServiceLive}
import zio._
import zio.stream._

object SubscriptionRunner extends ZIOAppDefault {

  def process: ZIO[ApiService, Nothing, Unit] = for {
    _ <- ZIO.log("starting")
    stream = EnhancedSubscription.subscribeIntersect()
    tapped = stream.tap(a => ZIO.logInfo(s"tap: $a"))
    fiber <- tapped.run(ZSink.count).fork
    exit <- fiber.await
    _ <- ZIO.log(s"completed with $exit")
  } yield ()

  def runLive: ZIO[Any, Throwable, Unit] = process.provide(Api.apiLiveLayer, ApiServiceLive.layer)

  override def run: ZIO[Any, Throwable, Unit] = runLive
}
