package zstreamapp

import cbapi.CbApi
import cbapi.CbApi.Item
import zio.stream.ZStream

case class SubscriptionManager(cbApi: CbApi) {

  def subscribeToApiChannelA(name: String): ZStream[Any, Throwable, Item] = {
    val subscribeFn = cbApi.subscribeToA _
    val stream = ZStreamAllSubscriber.stream(name, subscribeFn)
    stream
  }

  def subscribeToApiChannelB(name: String, key: Int): ZStream[Any, Throwable, Item] = {
    val subscribeFn = cbApi.subscribeToB(key, _)
    val stream = ZStreamByKeySubscriber.stream(name, key, subscribeFn)
    stream
  }
}
