package service

import cbapi.CbApi.Item
import zio.ZLayer
import zio.stream.ZStream

import scala.util.Try

case class ApiServiceLocal(playbackA: List[(Int, String)],
                           playbackB: Map[Int, List[(Int, String)]]) extends ApiService {
  def subscribeToApiChannelA(name: String): ZStream[Any, Throwable, Item] = ZStream.fromIterable(playbackA)
  def subscribeToApiChannelB(name: String, key: Int): ZStream[Any, Throwable, Item] = Try(playbackB(key)).fold(_ => ZStream.empty, a => ZStream.fromIterable(a))
}
object ApiServiceLocal {
  val layer: ZLayer[List[(Int, String)] with Map[Int, List[(Int, String)]], Nothing, ApiServiceLocal] = ZLayer.fromFunction {
    (a: List[(Int, String)], b: Map[Int, List[(Int, String)]]) => ApiServiceLocal(a, b)
  }
}
