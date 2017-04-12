package com.twitter.finagle
package samantha.protocol

import com.twitter.finagle.util.BufReader
import com.twitter.io.Buf
import java.nio.charset.StandardCharsets

private[samantha] trait Stage {
  def apply(reader: BufReader): Stage.NextStep
}

private[samantha] object Stage {
  
  sealed trait NextStep
  
  object NextStep {
    case object Incomplete extends NextStep
    case class Goto(stage: Stage) extends NextStep
    case class Emit(a: Feedback) extends NextStep
    case class Accumulate(
      n: Long,
      finish: List[Feedback] => Feedback) extends NextStep
  }

  def apply(f: BufReader => NextStep): Stage = (buf: BufReader) => f(buf)
  
  def const(next: NextStep): Stage = apply(_ => next)
  
  def readBytes(count: Int)(process: Buf => NextStep): Stage = Stage { reader =>
    if (reader.remaining < count) {
      NextStep.Incomplete
    } else {
      process(reader.readBytes(count))
    }
  }
  
  def readAllBytes(process: Buf => NextStep): Stage =
    Stage { reader => process(reader.readAll) }
  
  def readLine(process: String => NextStep): Stage = Stage { reader =>
    val untilNewLine = reader.remainingUntil('\n')
    if (untilNewLine == -1) NextStep.Incomplete
    else {
      val line = reader.readBytes(untilNewLine) match {
        case Buf.Empty => ""
        case buf =>
          val bytes = Buf.ByteArray.Owned.extract(buf)
          if (bytes(bytes.length - 1) == '\r')
            new String(bytes, 0, bytes.length - 1, StandardCharsets.UTF_8)
          else
            new String(bytes, StandardCharsets.UTF_8)
      }
      
      reader.skip(1) // skip LF
      
      process(line)
    }
  }
}