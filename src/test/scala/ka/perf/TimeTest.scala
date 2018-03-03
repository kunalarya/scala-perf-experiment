package ka.perf

import org.scalactic.TolerantNumerics
import org.scalatest.FunSuite

class TimeTest extends FunSuite {

  val epsilon = 5e5f  // 500us

  implicit val doubleEq = TolerantNumerics.tolerantDoubleEquality(epsilon)

  test("Accurate timer") {
    val (time, _) = TimeUtil.timeIt {
      Thread.sleep(1)
    }
    assert(time === 1e6)
  }

  test("Accurate average") {
    val time = TimeUtil.timeItAvg(10) {
      Thread.sleep(1)
    }
    assert(time === 1e6)
  }

}
