package lesson8.android.weather

package object weather {
  def cast[A, B](x: A): B = x match {
    case a: B => a
    case _ => throw new ClassCastException
  }
}