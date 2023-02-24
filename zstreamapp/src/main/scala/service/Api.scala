package service

import cbapi.CbApi
import zio.{ZIO, ZLayer}
import zstreamapp.SubscriptionManager

import scala.concurrent.ExecutionContext

object Api {
  def apiSubscriptionManager = for {
    api <- ZIO.fromFuture((_: ExecutionContext) => CbApi.start())
  } yield SubscriptionManager(api)

  def apiLiveLayer = ZLayer.fromZIO(apiSubscriptionManager)
}
