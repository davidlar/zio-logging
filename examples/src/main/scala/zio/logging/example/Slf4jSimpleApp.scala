package zio.logging.example

import zio.logging.LogAnnotation
import zio.logging.backend.SLF4J
import zio.{ ExitCode, Runtime, Scope, ZIO, ZIOAppDefault, _ }

import java.util.UUID

object Slf4jSimpleApp extends ZIOAppDefault {

  private val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val users = List.fill(2)(UUID.randomUUID())

  override def run: ZIO[Scope, Any, ExitCode] =
    (for {
      _       <- ZIO.logInfo("Start")
      traceId <- ZIO.succeed(UUID.randomUUID())
      _       <- ZIO.foreachPar(users) { uId =>
                   {
                     ZIO.logInfo("Starting user operation") *>
                       ZIO.sleep(500.millis) *>
                       ZIO.logInfo("Stopping user operation")
                   } @@ ZIOAspect.annotated("user", uId.toString)
                 } @@ LogAnnotation.TraceId(traceId) @@ SLF4J.loggerName("zio.logging.example.UserOperation")
      _       <- ZIO.logInfo("Done")
    } yield ExitCode.success).provide(logger)

}
