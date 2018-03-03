package ka.perf

import scala.collection.mutable

object TimeUtil {

  // ht: http://biercoff.com/easily-measuring-code-execution-time-in-scala/
  def timeIt[R](fn: => R): Double = {
    val t0 = System.nanoTime()
    fn
    val t1 = System.nanoTime()
    t1 - t0
  }

}
