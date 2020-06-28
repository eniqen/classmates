package com.github.eniqen

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatterBuilder

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
package object classmates {
  implicit final class LongOps(private val self: Long) extends AnyVal {
    def formatTs: String = {
      val formatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .toFormatter()

      val instant = Instant.ofEpochMilli(self)
      val dt = LocalDateTime.ofEpochSecond(instant.getEpochSecond, instant.getNano, ZoneOffset.UTC)

      formatter.format(dt)
    }
  }
}
