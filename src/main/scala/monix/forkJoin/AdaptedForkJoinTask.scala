package monix.forkJoin

final class AdaptedForkJoinTask(runnable: Runnable)
  extends ForkJoinTask[Unit] {

  def setRawResult(u: Unit): Unit = ()
  def getRawResult(): Unit = ()

  def exec(): Boolean =
    try { runnable.run(); true } catch {
      case anything: Throwable =>
        val t = Thread.currentThread
        t.getUncaughtExceptionHandler match {
          case null =>
          case some => some.uncaughtException(t, anything)
        }
        throw anything
    }
}