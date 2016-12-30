package monix.forkJoin

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ThreadFactory
import monix.forkJoin.ForkJoinPool.ForkJoinWorkerThreadFactory

final class StandardWorkerThreadFactory(
  prefix: String,
  reporter: Throwable => Unit,
  daemonic: Boolean)
  extends ThreadFactory with ForkJoinWorkerThreadFactory {

  def wire[T <: Thread](thread: T): T = {
    thread.setDaemon(daemonic)
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable): Unit =
        reporter(e)
    })

    thread.setName(prefix + "-" + thread.getId)
    thread
  }

  override def newThread(r: Runnable): Thread =
    wire(new Thread(r))

  override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
    wire(new ForkJoinWorkerThread(pool) {})
}
