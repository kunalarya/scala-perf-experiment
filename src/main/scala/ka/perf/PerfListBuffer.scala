package ka.perf

import scala.collection.mutable

object MeasureListBuffer {

  def measInsert(iters: Int, elmsToAdd: Int, maxInitial: Int): Double = {
    val list = mutable.ListBuffer.empty[Int]
    list ++= (0 to maxInitial).toList
    TimeUtil.timeItAvg(iters) {
      for (i <- 0 to elmsToAdd) {
        list.insert(0, i)
      }
    }
  }

  def apply(): Unit = {
    println("ListBuffer.insert")
    println("_________________")
    println("maxInitial,time")
    for (maxInitial <- Seq(10, 100, 1000, 10000, 100000,  500000, 1000000, 2000000, 3000000)) {
      val elmsToAdd = 10
      val t = measInsert(100, elmsToAdd, maxInitial)
      println(s"$maxInitial,$elmsToAdd,$t")
    }
  }
}
