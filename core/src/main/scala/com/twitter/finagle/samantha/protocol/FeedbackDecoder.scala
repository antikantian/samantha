package com.twitter.finagle
package samantha.protocol

import com.twitter.io.Buf

class FeedbackDecoder(val prefix: Buf, val suffix: Buf) {
  
  //private[samantha] val decode = Stage.readBytes(1)
  
}

object FeedbackDecoder {

}