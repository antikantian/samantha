package io.samantha

trait NetworkGateway[A, B, C] {
  def send(a: A): Unit
  def receive(c: C): B
}