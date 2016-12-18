package com.github.dunmatt.obo.utils.implicits

import java.nio.ByteBuffer

object ByteBufferEnhancements {
  implicit class SerializableByteBuffer(bb: ByteBuffer) {
    def mkString(sep: String): String = mkString("", sep, "")

    def mkString(start: String, sep: String, end: String): String = {
      (0 until bb.limit).map { n =>
        String.format("%02X", new Integer(bb.get(n) & 0xff))
      }.mkString(start, sep, end)
    }
  }
}
