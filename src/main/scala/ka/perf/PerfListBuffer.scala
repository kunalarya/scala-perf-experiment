package ka.perf

import scala.collection.mutable
import java.io._

abstract class InsertAt
case class InsertHead() extends InsertAt
case class InsertMiddle() extends InsertAt
case class InsertTail() extends InsertAt

case class Measurement(actualMaxInitial: Int, time: Double)

object MeasUtil {
  def avg(iters: Int, ignore: Int = 10)(fn: => Double): Double = {
    /*
     * Ignore the first several iterations to decouple caching from the timing metric.
     */
    val measurements = mutable.ListBuffer.empty[Double]
    for (i <- 0 to iters) {
      val time = fn
      if (i > ignore) {
        measurements.append(time)
      }
    }
    measurements.foldLeft(0.0)(_ + _) / measurements.length
  }
}

abstract class MeasureBuffer[T <: mutable.Buffer[Int]] {
  def makeBuffer: T

  def measInsert(iters: Int,
                 elmsToAdd: Int,
                 maxInitial: Int,
                 insertAt: InsertAt): Measurement = {
    val maxInitialHalf = (maxInitial / 2).toInt

    Measurement(
      maxInitial,
      MeasUtil.avg(iters) {
        val collection = makeBuffer
        collection ++= (0 to maxInitial).toSeq
        val insertIndex = insertAt match {
          case InsertHead()   => 0
          case InsertMiddle() => maxInitialHalf
          case InsertTail()   => maxInitial
        }
        TimeUtil.timeIt {
          for (i <- 0 to elmsToAdd) {
            collection.insert(insertIndex, i)
          }
        }
      }
    )
  }

}

object MeasureListBuffer extends MeasureBuffer[mutable.ListBuffer[Int]] {
  def makeBuffer = mutable.ListBuffer.empty[Int]
}

object MeasureArrayBuffer extends MeasureBuffer[mutable.ArrayBuffer[Int]] {
  def makeBuffer = mutable.ArrayBuffer.empty[Int]
}

object MeasureQueue {
  private sealed abstract class EnqOrDeq
  private case class Enq() extends EnqOrDeq
  private case class Deq() extends EnqOrDeq

  private def meas(iters: Int,
                   elmsToAdd: Int,
                   maxInitial: Int,
                   enqOrDeq: EnqOrDeq): Measurement = {
    enqOrDeq match {
      case Enq() =>
        Measurement(maxInitial, MeasUtil.avg(iters) {
          val queue = mutable.Queue.empty[Int]
          queue ++= (0 to maxInitial).toSeq
          TimeUtil.timeIt {
            for (i <- 0 to elmsToAdd) {
              queue.enqueue(i)
            }
          }
        })
      case Deq() =>
        Measurement(maxInitial, MeasUtil.avg(iters) {
          val queue = mutable.Queue.empty[Int]
          queue ++= (0 to maxInitial).toSeq
          TimeUtil.timeIt {
            for (i <- 0 to elmsToAdd) {
              queue.dequeue()
            }
          }
        })
    }
  }

  def measEnqueue(iters: Int, elmsToAdd: Int, maxInitial: Int): Measurement = {
    meas(iters, elmsToAdd, maxInitial, Enq())
  }
  def measDequeue(iters: Int, elmsToDeq: Int, maxInitial: Int): Measurement = {
    meas(iters, elmsToDeq, elmsToDeq + maxInitial, Deq())
  }
}

object Measure {

  def header(str: String): Unit = {
    println()
    println(s"+${"=" * str.length}+")
    println(s" $str ")
    println(s"+${"=" * str.length}+")
  }

  def saveToCsv[R](filename: String)(fn: BufferedWriter => R): R = {
    val csvFile = new File(filename)
    val writer = new BufferedWriter(new FileWriter(csvFile))
    val ret = fn(writer)
    println("Saving CSV file...")
    writer.close()
    ret
  }

  def apply(): Unit = {

    val sizes =
      Seq(20, 30, 40, 60, 75, 100, 200, 350, 500, 750, 1000, 2000, 5000, 10000,
        20000, 30000, 50000, 75000, 100000, 250000, 300000)

    def runMeasurement(title: String, writer: BufferedWriter)(
        body: Int => Measurement): Unit = {
      header(title)
      // To get around the quote escape bug in string interpolation.
      val quote = "\""
      for (maxInitial <- sizes) {
        body(maxInitial) match {
          case Measurement(actualMaxInitial, t) =>
            writer.write(f"${quote}${title}${quote},$actualMaxInitial,$t%2f\n")
        }
      }
    }

    val elmsToAdd = 100
    val iters = 20

    saveToCsv("list_buf.csv") { writer =>
      writer.write("name,length,time\n")
      runMeasurement("ListBuffer.insert(0, ...)", writer) { x =>
        MeasureListBuffer.measInsert(iters, elmsToAdd, x, InsertHead())
      }

      runMeasurement("ListBuffer.insert(N/2, ...)", writer) { x =>
        MeasureListBuffer.measInsert(iters, elmsToAdd, x, InsertMiddle())
      }

      runMeasurement("ListBuffer.insert(N, ...)", writer) { x =>
        MeasureListBuffer.measInsert(iters, elmsToAdd, x, InsertTail())
      }
    }

    saveToCsv("array_buf.csv") { writer =>
      writer.write("name,length,time\n")
      runMeasurement("ArrayBuffer.insert(0, ...)", writer) { x =>
        MeasureArrayBuffer.measInsert(iters, elmsToAdd, x, InsertHead())
      }

      runMeasurement("ArrayBuffer.insert(N/2, ...)", writer) { x =>
        MeasureArrayBuffer.measInsert(iters, elmsToAdd, x, InsertMiddle())
      }

      runMeasurement("ArrayBuffer.insert(N, ...)", writer) { x =>
        MeasureArrayBuffer.measInsert(iters, elmsToAdd, x, InsertTail())
      }
    }

    saveToCsv("queue.csv") { writer =>
      writer.write("name,length,time\n")
      runMeasurement("Queue.enqueue(...)", writer) { x =>
        MeasureQueue.measEnqueue(iters, elmsToAdd, x)
      }
      runMeasurement("Queue.dequeue(...)", writer) { x =>
        MeasureQueue.measDequeue(iters, elmsToAdd, x)
      }
    }

  }
}
