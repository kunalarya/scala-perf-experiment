package ka.perf

import scala.collection.mutable

object TimeUtil {
  // ht: http://biercoff.com/easily-measuring-code-execution-time-in-scala/
  def timeIt[R](fn: => R): Tuple2[Double, R] = {
    val t0 = System.nanoTime()
    val result = fn
    val t1 = System.nanoTime()
    (t1 - t0, result)
  }

  def timeItAvg[R](iters: Int)(fn: => R): Double = {
    val measurements = mutable.ListBuffer.empty[Double]
    for (i <- 0 to iters) {
      val (time, _) = timeIt(fn)
      measurements.append(time)
    }

    measurements.foldLeft(0.0)(_ + _) / measurements.length
  }

}
