package monix.forkJoin

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import monix.forkJoin.DynamicWorkerThreadFactory.EmptyBlockContext
import monix.forkJoin.ForkJoinPool.{ForkJoinWorkerThreadFactory, ManagedBlocker}
import scala.annotation.tailrec
import scala.concurrent.{BlockContext, CanAwait}

// Implement BlockContext on FJP threads
final class DynamicWorkerThreadFactory(
  prefix: String,
  maxThreads: Int,
  uncaught: Thread.UncaughtExceptionHandler,
  daemonic: Boolean)
  extends ThreadFactory with ForkJoinWorkerThreadFactory {

  require(prefix ne null, "DefaultWorkerThreadFactory.prefix must be non null")
  require(maxThreads > 0, "DefaultWorkerThreadFactory.maxThreads must be greater than 0")

  private[this] val currentNumberOfThreads = new AtomicInteger(0)

  @tailrec private def reserveThread(): Boolean =
    currentNumberOfThreads.get match {
      case `maxThreads` | Int.`MaxValue` => false
      case other => currentNumberOfThreads.compareAndSet(other, other + 1) || reserveThread()
    }

  @tailrec private def deregisterThread(): Boolean =
    currentNumberOfThreads.get match {
      case 0 => false
      case other => currentNumberOfThreads.compareAndSet(other, other - 1) || deregisterThread()
    }

  def wire[T <: Thread](thread: T): T = {
    thread.setDaemon(daemonic)
    thread.setUncaughtExceptionHandler(uncaught)
    thread.setName(prefix + "-" + thread.getId)
    thread
  }

  // As per ThreadFactory contract newThread should return `null` if cannot create new thread.
  def newThread(runnable: Runnable): Thread =
    if (!reserveThread()) null else
      wire(new Thread(new Runnable {
        // We have to decrement the current thread count when the thread exits
        override def run() =
          try runnable.run() finally deregisterThread()
      }))

  def newThread(fjp: ForkJoinPool): ForkJoinWorkerThread =
    if (!reserveThread()) null else {
      wire(new ForkJoinWorkerThread(fjp) with BlockContext {
        // We have to decrement the current thread count when the thread exits
        final override def onTermination(exception: Throwable): Unit =
          deregisterThread()

        final override def blockOn[T](thunk: =>T)(implicit permission: CanAwait): T = {
          var result: T = null.asInstanceOf[T]
          ForkJoinPool.managedBlock(new ManagedBlocker {
            @volatile
            private[this] var isDone = false
            def isReleasable: Boolean = isDone

            def block(): Boolean = {
              result = try {
                // When we block, switch out the BlockContext temporarily so that nested
                // blocking does not created N new Threads
                BlockContext.withBlockContext(EmptyBlockContext) { thunk }
              } finally {
                isDone = true
              }
              true
            }
          })

          result
        }
      })
    }
}

object DynamicWorkerThreadFactory {
  /** Reusable instance that doesn't do anything special. */
  private object EmptyBlockContext extends BlockContext {
    override def blockOn[T](thunk: => T)(implicit permission: CanAwait): T =
      thunk
  }
}