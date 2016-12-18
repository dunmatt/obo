package com.github.dunmatt.obo.core.msgpack

import java.nio.ByteBuffer

trait MsgField {
  def serializedLength: Int
  def populate(buf: ByteBuffer): Unit

  protected def smallestUintByteCount(n: Int): Int = n match {
    case b if b <= Constants.MAX_UINT_8 => 1
    case s if s <= Constants.MAX_UINT_16 => 2
    case _ => 4
  }
}

case class BlobField(b: ByteBuffer) extends MsgField {
  // TODO: this assumes the blob starts at index zero of the buffer, should it be based on the mark?
  def serializedLength: Int = 1 + smallestUintByteCount(b.limit) + b.limit

  def populate(buf: ByteBuffer): Unit = {
    b.flip
    b.limit match {
      case len if len < Constants.MAX_UINT_8 =>
        buf.put(Constants.BIN_8)
        buf.put(len.toByte)
      case len if len < Constants.MAX_UINT_16 =>
        buf.put(Constants.BIN_16)
        buf.putShort(len.toShort)
      case len =>
        buf.put(Constants.BIN_32)
        buf.putInt(len)
    }
    buf.put(b)
  }
}

case class BooleanField(b: Boolean) extends MsgField {
  val serializedLength = 1
  def populate(buf: ByteBuffer) = b match {
    case true => buf.put(Constants.TRUE)
    case false => buf.put(Constants.FALSE)
  }
}

case class DoubleField(d: Double) extends MsgField {
  val serializedLength = 9
  def populate(buf: ByteBuffer): Unit = {
    buf.put(Constants.FLOAT_64)
    buf.putDouble(d)
  }
}

case class IntField(n: Long) extends MsgField {
  def serializedLength: Int = n match {
    case pf if pf < Constants.POSITIVE_FIXINT_CUTOFF => 1
    case nf if math.abs(nf) <= Constants.NEGATIVE_FIXINT_VALUE_MASK => 1
    case b if b == b.toByte => 2
    case s if s == s.toShort => 3
    case i if i == i.toInt => 5
    case _ => 9
  }

  def populate(buf: ByteBuffer): Unit = {
    serializedLength match {
      case 9 =>
        buf.put(Constants.INT_64)
        buf.putLong(n)
      case 5 =>
        buf.put(Constants.INT_32)
        buf.putInt(n.toInt)
      case 3 =>
        buf.put(Constants.INT_16)
        buf.putShort(n.toShort)
      case 2 =>
        buf.put(Constants.INT_8)
        buf.put(n.toByte)
      case 1 =>
        if (n >= 0) {
          buf.put(n.toByte)
        } else {
          buf.put((Constants.NEGATIVE_FIXINT_THRESHHOLD | math.abs(n)).toByte)
        }
    }
  }
}

object NilField extends MsgField {
  val serializedLength = 1
  def populate(buf: ByteBuffer) = buf.put(Constants.NIL)
}

case class StringField(s: String) extends MsgField {
  val asBytes = s.getBytes("UTF-8")

  val serializedLength = 1 + bytesRequiredFor(asBytes.length) + asBytes.length

  def populate(buf: ByteBuffer): Unit = {
    asBytes.length match {
      case len if len <= Constants.FIXSTR_VALUE_MASK =>
        buf.put((Constants.FIXARRAY_CUTOFF | len).toByte)
      case len if len <= Constants.MAX_UINT_8 =>
        buf.put(Constants.STR_8)
        buf.put(len.toByte)
      case len if len <= Constants.MAX_UINT_16 =>
        buf.put(Constants.STR_16)
        buf.putShort(len.toShort)
      case len if len <= Integer.MAX_VALUE =>
        buf.put(Constants.STR_32)
        buf.putInt(len)
    }
    buf.put(asBytes)
  }

  protected def bytesRequiredFor(n: Int): Int = n match {
    case f if f <= Constants.FIXSTR_VALUE_MASK => 0
    case _ => smallestUintByteCount(n)
  }
}
