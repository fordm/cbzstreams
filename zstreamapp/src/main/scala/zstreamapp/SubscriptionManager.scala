package zstreamapp

import cbapi.CbApi
import cbapi.CbApi.Item
import zio.ZIO
import zio.stream.ZStream

case class SubscriptionManager(cbApi: CbApi) {

  def subscribeToApiChannelA(delay: Long, name: String): ZStream[Any, Throwable, Item] = {
    val subscribeFn = cbApi.subscribeToA(delay, _)
    val stream = ZStreamAllSubscriber.stream(name, subscribeFn)
    stream
  }

  def subscribeToApiChannelAQueue(delay: Long, name: String): ZIO[Any, Throwable, ZStream[Any, Nothing, Item]] = {
    val subscribeFn = cbApi.subscribeToA(delay, _)
    val stream = ZStreamAllSubscriber.streamFromQueue1(name, subscribeFn)
    stream
  }

  def subscribeToApiChannelB(name: String, key: Int): ZStream[Any, Throwable, Item] = {
    val subscribeFn = cbApi.subscribeToB(key, _)
    val stream = ZStreamByKeySubscriber.stream(name, key, subscribeFn)
    stream
  }
}
