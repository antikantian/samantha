package com.twitter.finagle
package samantha.protocol

import com.twitter.finagle.samantha._
import com.twitter.io.Buf

sealed abstract class Feedback

sealed abstract class SingleLineFeedback extends Feedback

sealed abstract class MultiLineFeedback extends Feedback

case object NoFeedback extends Feedback

case class TextualFeedback(message: String) extends SingleLineFeedback

case class ErrorFeedback(message: String) extends SingleLineFeedback

case class IntegerFeedback(id: Long) extends SingleLineFeedback

case class FloatFeedback(id: Float) extends SingleLineFeedback

case class BulkFeedback(message: Buf) extends MultiLineFeedback

case object EmptyBulkFeedback extends MultiLineFeedback

object Feedback {
  
  val EOL                 = Buf.Utf8("\r\n")
  val STATUS_REPLY        = Buf.Utf8("+")
  val ERROR_REPLY         = Buf.Utf8("-")
  val INTEGER_REPLY       = Buf.Utf8(":")
  val BULK_REPLY          = Buf.Utf8("$")
  val MBULK_REPLY         = Buf.Utf8("*")
  
  import Stage.NextStep
  
  private[this] val decodeTextual =
    Stage.readLine(line => NextStep.Emit(TextualFeedback(line)))
  
  private[this] val decodeError =
    Stage.readLine(line => NextStep.Emit(ErrorFeedback(line)))
  
  private[this] val decodeInteger =
    Stage.readLine(line =>
      NextStep.Emit(IntegerFeedback(line.toLong))
    )
  
  private[this] val decodeBulk =
    Stage.readLine { line =>
      val num = line.toInt
      
      if (num < 0) NextStep.Emit(EmptyBulkFeedback)
      else NextStep.Goto(Stage.readBytes(num) { bytes =>
        NextStep.Goto(Stage.readBytes(2) {
          case EOL => NextStep.Emit(BulkFeedback(bytes))
          case _ => throw ServerError("Expected EOL after line data and didn't find it")
        })
      })
    }
  
  private[samantha] val decode = Stage.readAllBytes(_ => NextStep.Goto(decodeTextual))
  
}