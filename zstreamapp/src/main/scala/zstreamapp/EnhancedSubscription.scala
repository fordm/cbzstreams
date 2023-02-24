package zstreamapp

import com.typesafe.scalalogging.Logger
import service.ApiService
import zio.stream.ZStream

object EnhancedSubscription {
  val log = Logger(this.getClass)

  def subscribeIntersect(): ZStream[ApiService, Throwable, (Int, (Int, String))] = {
    log.debug(s"subscribeIntersect")
    for {

      apiService <- ZStream.environment[ApiService].map(_.get)
      itemAKey <- apiService.subscribeToApiChannelA("location").map(a => { log.debug(s"received $a"); a._1 })
      _ = log.debug(s"subscribing to $itemAKey")
      itemB <- apiService.subscribeToApiChannelB("temperature", itemAKey).map(a => { log.debug(s"received $a"); a })
    } yield (itemAKey, itemB)
  }
}
