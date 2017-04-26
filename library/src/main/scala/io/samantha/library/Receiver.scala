package io.samantha
package library

trait Receiver[F[_], I, O] {
  def powerOn: F[O]
  def powerOff: F[O]
  def volume(vol: Int): F[O]
  def input[A](in: A): F[O]
}