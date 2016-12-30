package monix.forkJoin

import java.lang.Thread.UncaughtExceptionHandler
import scala.concurrent.ExecutionContext

object ForkJoinExecutionContext {
  /** Creates a Scala `ExecutionContext` powered by a JSR-166
    * `ForkJoinPool` implementation with the ability to add threads
    * in case blocking operations happen.
    *
    * The resulting instance collaborates with Scala's `BlockContext`
    * and has the same behavior as Scala's own `global`.
    */
  def createDynamic(
    parallelism: Int,
    maxThreads: Int,
    name: String = "forkJoin-dynamic",
    daemonic: Boolean = true,
    reporter: Throwable => Unit = _.printStackTrace()): ExecutionContext = {

    val exceptionHandler = new UncaughtExceptionHandler {
      def uncaughtException(t: Thread, e: Throwable) =
        reporter(e)
    }

    val pool = new AdaptedForkJoinPool(
      parallelism,
      new DynamicWorkerThreadFactory(name, maxThreads, exceptionHandler, daemonic),
      exceptionHandler,
      asyncMode = true
    )

    ExecutionContext.fromExecutor(pool, reporter)
  }

  /** Creates a Scala `ExecutionContext` powered by a JSR-166
    * `ForkJoinPool` implementation.
    */
  def createStandard(
    parallelism: Int,
    name: String = "forkJoin-standard",
    daemonic: Boolean = true,
    reporter: Throwable => Unit = _.printStackTrace()): ExecutionContext = {

    val exceptionHandler = new UncaughtExceptionHandler {
      def uncaughtException(t: Thread, e: Throwable) =
        reporter(e)
    }

    val pool = new AdaptedForkJoinPool(
      parallelism,
      new StandardWorkerThreadFactory(name, reporter, daemonic),
      exceptionHandler,
      asyncMode = true
    )

    ExecutionContext.fromExecutor(pool, reporter)
  }
}
