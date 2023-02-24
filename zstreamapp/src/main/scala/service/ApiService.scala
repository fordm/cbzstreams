package service

import cbapi.CbApi.Item
import zio.ZLayer
import zio.stream.ZStream
import zstreamapp.SubscriptionManager

trait ApiService {
  def subscribeToApiChannelA(name: String): ZStream[Any, Throwable, Item]
  def subscribeToApiChannelB(name: String, key: Int): ZStream[Any, Throwable, Item]
}
object ApiService {
  def subscribeToApiChannelA(name: String): ZStream[ApiService, Throwable, Item] = ZStream.serviceWithStream[ApiService](_.subscribeToApiChannelA(name))
  def subscribeToApiChannelB(name: String, key: Int): ZStream[ApiService, Throwable, Item] = ZStream.serviceWithStream[ApiService](_.subscribeToApiChannelB(name, key))
}
case class ApiServiceLive(subscriptionManager: SubscriptionManager) extends ApiService {
  def subscribeToApiChannelA(name: String): ZStream[Any, Throwable, Item] = subscriptionManager.subscribeToApiChannelA(name)
  def subscribeToApiChannelB(name: String, key: Int): ZStream[Any, Throwable, Item] = subscriptionManager.subscribeToApiChannelB(name, key)
}
object ApiServiceLive {
  val layer: ZLayer[SubscriptionManager, Nothing, ApiService] = ZLayer.fromFunction(ApiServiceLive(_))
}