package io.samantha

object ServerIO {
  
  def delay[A](a: => A): ServerIO[A] = ???
  
  def pure[A](a: A): ServerIO[A] = ???
  
  val unit: ServerIO[Unit] = pure(())
  
}

trait ServerIOFunctions {
  
  type ServerIO[A] =
  
}