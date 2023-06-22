package datamap

import com.typesafe.scalalogging.Logger
import datamap.mapKdb._

object Runner extends App {
  val log = Logger(this.getClass)
  log.info("start")

  val dataA = Data1("data a")
  val showA = dataA.mapKdb

  log.info(s"showA is $showA")

  log.info("complete")
}
