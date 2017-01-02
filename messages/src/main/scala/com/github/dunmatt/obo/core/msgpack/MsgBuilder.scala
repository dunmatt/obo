package com.github.dunmatt.obo.core.msgpack

import com.github.dunmatt.obo.core.Message
import java.lang.Enum
import java.nio.{ ByteBuffer, ByteOrder }

class MsgBuilder {
  protected var fields = Seq.empty[MsgField]

  def addField(mf: MsgField): MsgBuilder = { fields = fields :+ mf; this }

  def putSeq[T <: MsgField](xs: Seq[T]) = { addField(SeqField(xs)); this }

  def putBlob(b: ByteBuffer): MsgBuilder = { addField(BlobField(b)); this }

  def putBoolean(b: Boolean): MsgBuilder = { addField(BooleanField(b)); this }

  def putEnum[E <: Enum[E]](e: E): MsgBuilder = { addField(StringField(e.name)); this }

  def putFloat(d: Double): MsgBuilder = { addField(DoubleField(d)); this }

  def putInt(n: Long): MsgBuilder = { addField(IntField(n)); this }

  def putNil: MsgBuilder = { addField(NilField); this }

  def putString(s: String): MsgBuilder = { addField(StringField(s)); this }

  def build: ByteBuffer = {
    val size = fields.map(_.serializedLength).sum
    val buf = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)
    fields.foreach(_.populate(buf))
    buf.flip.asInstanceOf[ByteBuffer]
  }

  def getBytes: Array[Byte] = build.array
}
