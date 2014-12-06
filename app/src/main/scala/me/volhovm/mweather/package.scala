package me.volhovm

package object mweather {
  // heh mda kostyl
  def cast[A, B](x: A): B = x match {
    case a: B => a
    case _ => throw new ClassCastException
  }
}