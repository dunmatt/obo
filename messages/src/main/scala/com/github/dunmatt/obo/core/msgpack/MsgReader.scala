package com.github.dunmatt.obo.core.msgpack

import com.github.dunmatt.obo.core.Message
import com.github.dunmatt.obo.utils.implicits.ByteBufferEnhancements._
import java.nio.{ ByteBuffer, ByteOrder }
import scala.util.{ Failure, Success, Try }

// This is a reader for the messagepack serialization format, see https://github.com/msgpack/msgpack/blob/master/spec.md 

class MsgReader(private val data: ByteBuffer) {
  def this(bytes: Array[Byte]) = this(ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN))
  import Constants._
  protected val map = makeMap  // maps field numbers to buffer offsets

  def dataAsString = data.mkString("[", ", ", "]")

  def getBoolean(field: Int): Try[Boolean] = data.get(map(field)) match {
    case TRUE => Try(true)
    case FALSE => Try(false)
    case _ => Failure(new Exception(s"Boolean not found at ${map(field)}."))
  }

  def getOptionalBoolean(field: Int): Try[Option[Boolean]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getBoolean(field).map(Option.apply)
  }

  def getFloat(field: Int): Try[Double] = data.get(map(field)) match {
    case FLOAT_32 => Try(data.getFloat(1 + map(field)))
    case FLOAT_64 => Try(data.getDouble(1 + map(field)))
    case _ => Failure(new Exception(s"Float type not found at ${map(field)}."))
  }

  def getOptionalFloat(field: Int): Try[Option[Double]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getFloat(field).map(Option.apply)
  }

  def getInt(field: Int): Try[Long] = data.get(map(field)) match {
    case b if b < POSITIVE_FIXINT_CUTOFF => Try(b & POSITIVE_FIXINT_MASK)
    case b if (b & 0xff) > NEGATIVE_FIXINT_THRESHHOLD => Try(-1 * (b & NEGATIVE_FIXINT_VALUE_MASK))  // TODO: check if we need to do a twos complement
    case INT_8 => Try(data.get(1 + map(field)))
    case INT_16 => Try(data.getShort(1 + map(field)))
    case INT_32 => Try(data.getInt(1 + map(field)))
    case INT_64 => Try(data.getLong(1 + map(field)))
    case UINT_8 => Try(data.get(1 + map(field)) & 0xffffffffffffffffL)
    case UINT_16 => Try(data.getShort(1 + map(field)) & 0xffffffffffffffffL)
    case UINT_32 => Try(data.getInt(1 + map(field)) & 0xffffffffffffffffL)
    case UINT_64 => Try(data.getLong(1 + map(field)) & 0xffffffffffffffffL)
    case _ => Failure(new Exception(s"Int type not found at ${map(field)}."))
  }

  def getOptionalInt(field: Int): Try[Option[Long]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getInt(field).map(Option.apply)
  }

  def getString(field: Int): Try[String] = {
    val idx = map(field)
    if (MsgReader.isString(data.get(idx))) {
      val metaLength = sizeOfMetadataAt(idx)
      val length = sizeOfFieldAt(idx) - metaLength
      if (idx + metaLength + length < data.limit) {
        val buff = Array.ofDim[Byte](length)
        data.get(buff, idx + metaLength, length)
        Success(new String(buff, "UTF-8"))
      } else {
        Failure(new Exception("String would exceed the underlying buffer, perhaps a malformed packet?"))
      }
    } else {
      Failure(new Exception(s"String type not found at $idx."))
    }
  }

  def getOptionalString(field: Int): Try[Option[String]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getString(field).map(Option.apply)
  }

  // NOTE: getBlob is not thread safe
  def getBlob(field: Int): Try[ByteBuffer] = {
    val idx = map(field)
    def size = data.get(idx) match {
      case BIN_8 => Try(data.get(idx + 1) & 0xff)
      case BIN_16 => Try(data.getShort(idx + 1) & 0xffff)
      case BIN_32 => Try(data.getInt(idx + 1))  // yes this means blobs top out at 2GB
      case _ => Failure(new Exception(s"Binary format not found at ${map(field)}."))
    }
    size.map { s =>
      val pos = data.position
      data.position(idx + sizeOfMetadataAt(idx))
      val result = data.slice
      data.position(pos)
      result.limit(s)
      result.asReadOnlyBuffer
    }
  }

  def getOptionalBlob(field: Int): Try[Option[ByteBuffer]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getBlob(field).map(Option.apply)
  }

  def getBytes: Array[Byte] = data.array

  // TODO: add getters for arrays, maps, and exts

  protected def makeMap: Array[Int] = {
    var inprog: List[Int] = Nil
    var i = 0
    while (i < data.limit) {
      inprog = i :: inprog
      i += sizeOfFieldAt(i)
    }
    inprog.reverse.toArray
  }

  protected def sizeOfFieldAt(i: Int): Int = MsgReader.sizeOfFieldAt(i, data)
  protected def sizeOfMetadataAt(i: Int): Int = MsgReader.sizeOfMetadataAt(i, data)
}

object MsgReader {
  import Constants._
  def sizeOfFieldAt(i: Int, data: ByteBuffer): Int = data.get(i) match {
    // the order of these case statements matters
    case b if (b & POSITIVE_FIXINT_CUTOFF) == 0 => 1
    case b if (b & 0xff) > NEGATIVE_FIXINT_THRESHHOLD => 1
    case b if (b & 0xff) < FIXMAP_CUTOFF => 1 + sizeOfCollectionAt(i+1, 2 * (b & FIXMAP_VALUE_MASK), data)
    case b if (b & 0xff) < FIXARRAY_CUTOFF => 1 + sizeOfCollectionAt(i+1, b & FIXARRAY_VALUE_MASK, data)
    case b if (b & 0xff) < FIXSTR_CUTOFF => 1 + (b & FIXSTR_VALUE_MASK)
    case INT_8 => 2
    case INT_16 => 3
    case INT_32 => 5
    case INT_64 => 9
    case UINT_8 => 2
    case UINT_16 => 3
    case UINT_32 => 5
    case UINT_64 => 9
    case FLOAT_32 => 5
    case FLOAT_64 => 9
    case STR_8 => sizeOfMetadataAt(i, data) + (data.get(i+1) & 0xff)
    case STR_16 => sizeOfMetadataAt(i, data) + (data.getShort(i+1) & 0xffff)
    case STR_32 => sizeOfMetadataAt(i, data) + data.getInt(i+1)  // technically this would fail if your string was more than two gigabytes... but that seems pretty unlikely at this scale
    case BIN_8 => 2 + (data.get(i+1) & 0xff)
    case BIN_16 => 3 + (data.getShort(i+1) & 0xffff)
    case BIN_32 => 5 + data.getInt(i+1)  // same comment as STR_32
    case ARRAY_16 => 3 + sizeOfCollectionAt(i+3, data.getShort(i+1) & 0xffff, data)
    case ARRAY_32 => 5 + sizeOfCollectionAt(i+5, data.getInt(i+1), data)
    case MAP_16 => 3 + sizeOfCollectionAt(i+3, 2 * (data.getShort(i+1) & 0xffff), data)
    case MAP_32 => 5 + sizeOfCollectionAt(i+5, 2 * data.getInt(i+1), data)
    case FIXEXT_1 => 3
    case FIXEXT_2 => 4
    case FIXEXT_4 => 6
    case FIXEXT_8 => 10
    case FIXEXT_16 => 18
    case EXT_8 => 3 + (data.get(i+1) & 0xff)
    case EXT_16 => 4 + (data.getShort(i+1) & 0xffff)
    case EXT_32 => 6 + data.getInt(i+1)
    case _ => 1
  }

  def sizeOfMetadataAt(i: Int, data: ByteBuffer): Int = data.get(i) match {
    case BIN_8 => 2
    case BIN_16 => 3
    case BIN_32 => 5
    case STR_8 => 2
    case STR_16 => 3
    case STR_32 => 5
    // TODO: finish me!
  }

  def sizeOfCollectionAt(start: Int, itemCount: Int, data: ByteBuffer): Int = {
    var size = 0
    (0 until itemCount).foreach { i =>
      size += sizeOfFieldAt(start + size, data)
    }
    size
  }

  def isString(format: Byte): Boolean = {
    format == STR_8 || format == STR_16 || format == STR_32 || (format & FIXSTR_MASK) == FIXARRAY_CUTOFF
  }
}
