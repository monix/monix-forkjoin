package monix.forkJoin

import java.lang.Thread.UncaughtExceptionHandler
import monix.forkJoin.ForkJoinPool.ForkJoinWorkerThreadFactory

final class AdaptedForkJoinPool(
  parallelism: Int,
  factory: ForkJoinWorkerThreadFactory,
  handler: UncaughtExceptionHandler,
  asyncMode: Boolean)
  extends ForkJoinPool(parallelism, factory, handler, asyncMode) {

  override def execute(runnable: Runnable): Unit = {
    val fjt: ForkJoinTask[_] = runnable match {
      case t: ForkJoinTask[_] => t
      case r => new AdaptedForkJoinTask(r)
    }

    Thread.currentThread match {
      case fjw: ForkJoinWorkerThread if fjw.getPool eq this => fjt.fork()
      case _ => super.execute(fjt)
    }
  }
}
