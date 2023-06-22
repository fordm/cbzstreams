package zstreamapp

import zio.logging.backend.SLF4J
import zio.{ZIOApp, ZIOAppArgs, ZLayer}

trait ZioLogging {
  this: ZIOApp =>
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j
}
