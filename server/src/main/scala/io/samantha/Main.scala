package io.samantha

import com.twitter.app.Flag
import com.twitter.finagle.Redis
import com.twitter.server.TwitterServer

object Main extends TwitterServer {

  val port: Flag[Int] = flag("port", 80, "Samantha server port")
  
  val redisClient = Redis.newService("192.168.10.13:6379")
  
  
}