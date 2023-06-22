package zstreamapp

import zio.Schedule.WithState

import java.time.LocalTime
import zio._

import scala.annotation.nowarn

object RetryWithBackoff extends ZIOAppDefault {


  // type WithState[State0, -Env, -In0, +Out0] = Schedule[Env, In0, Out0] { type State = State0 }

  val repeatSchedule: WithState[Long, Any, Any, Long] = Schedule.spaced(1.second)
  val retryScheduleExp: WithState[Long, Any, Any, zio.Duration] = Schedule.exponential(1.second)
  val retryScheduleRecur: WithState[Long, Any, Any, Long] = Schedule.recurs(3)
  @nowarn
  val retrySchedule: WithState[(Long, Long), Any, Any, (zio.Duration, Long)] = retryScheduleExp && retryScheduleRecur // exponential backoff, max. 3 retries

  val logic: ZIO[Any, Throwable, Unit] = for {
    next <- ZIO.random.map(_.nextDouble.map(_ < 0.25))
    continue <- next
    _ = continue match {
      case true => ZIO.log(s"${LocalTime.now} Success!")
      case false => ZIO.log(s"${LocalTime.now} Error!") *> ZIO.fail(new RuntimeException("Error"))
    }
  } yield ()

  @nowarn
  val retriedLogic =
    logic
      .retry(retrySchedule)
      .catchAll(_ => ZIO.log("Exceeded max. number of retries."))

  @nowarn
  override def run =
    retriedLogic
      .repeat(repeatSchedule)
      .timeout(30.seconds)
      .map(_ => ExitCode.success)
}