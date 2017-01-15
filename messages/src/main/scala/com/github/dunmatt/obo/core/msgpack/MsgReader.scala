package com.github.dunmatt.obo.core.msgpack

import com.github.dunmatt.obo.core.Message
import com.github.dunmatt.obo.utils.implicits.ByteBufferEnhancements._
import java.lang.Enum
import java.nio.{ ByteBuffer, ByteOrder }
import scala.util.{ Failure, Success, Try }

// This is a reader for the messagepack serialization format, see https://github.com/msgpack/msgpack/blob/master/spec.md 

class MsgReader(private val data: ByteBuffer) {
  def this(bytes: Array[Byte]) = this(ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN))
  import Constants._
  protected val map = makeMap  // maps field numbers to buffer offsets

  def dataAsString = data.mkString("[", ", ", "]")

  protected def getAnyByIndex(idx: Int): Try[Any] = {
    val b = data.get(idx)
    if (MsgReader.isArray(b)) getAnyByIndex(idx)
    else if (MsgReader.isBlob(b)) getBlobByIndex(idx)
    else if (MsgReader.isBoolean(b)) getBooleanByIndex(idx)
    else if (MsgReader.isFloat(b)) getFloatByIndex(idx)
    else if (MsgReader.isInt(b)) getIntByIndex(idx)
    else if (MsgReader.isMap(b)) getMapByIndex(idx)
    else if (MsgReader.isNil(b)) Try(Nil)
    else if (MsgReader.isString(b)) getStringByIndex(idx)
    else Failure(new Exception(s"Unsupported array item at $idx"))
  }

  def getBoolean(field: Int): Try[Boolean] = getBooleanByIndex(map(field))

  protected def getBooleanByIndex(idx: Int): Try[Boolean] = data.get(idx) match {
    case TRUE => Try(true)
    case FALSE => Try(false)
    case _ => Failure(new Exception(s"Boolean not found at $idx."))
  }

  def getOptionalBoolean(field: Int): Try[Option[Boolean]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getBoolean(field).map(Option.apply)
  }

  def getEnum[E <: Enum[E]](field: Int, cls: Class[E]): Try[E] = {
    getString(field).map(n => Enum.valueOf(cls, n))
  }

  def getOptionalEnum[E <: Enum[E]](field: Int, cls: Class[E]): Try[Option[E]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getEnum(field, cls).map(Option.apply)
  }

  def getFloat(field: Int): Try[Double] = getFloatByIndex(map(field))

  protected def getFloatByIndex(idx: Int): Try[Double] = data.get(idx) match {
    case FLOAT_32 => Try(data.getFloat(1 + idx))
    case FLOAT_64 => Try(data.getDouble(1 + idx))
    case _ => Failure(new Exception(s"Float type not found at $idx."))
  }

  def getOptionalFloat(field: Int): Try[Option[Double]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getFloat(field).map(Option.apply)
  }

  def getInt(field: Int): Try[Long] = getIntByIndex(map(field))

  def getIntByIndex(idx: Int): Try[Long] = data.get(idx) match {
    case b if b < POSITIVE_FIXINT_CUTOFF => Try(b & POSITIVE_FIXINT_MASK)
    case b if (b & 0xff) > NEGATIVE_FIXINT_THRESHHOLD => Try(-1 * (b & NEGATIVE_FIXINT_VALUE_MASK))  // TODO: check if we need to do a twos complement
    case INT_8 => Try(data.get(1 + idx))
    case INT_16 => Try(data.getShort(1 + idx))
    case INT_32 => Try(data.getInt(1 + idx))
    case INT_64 => Try(data.getLong(1 + idx))
    case UINT_8 => Try(data.get(1 + idx) & 0xffffffffffffffffL)
    case UINT_16 => Try(data.getShort(1 + idx) & 0xffffffffffffffffL)
    case UINT_32 => Try(data.getInt(1 + idx) & 0xffffffffffffffffL)
    case UINT_64 => Try(data.getLong(1 + idx) & 0xffffffffffffffffL)
    case _ => Failure(new Exception(s"Int type not found at $idx."))
  }

  def getOptionalInt(field: Int): Try[Option[Long]] = data.get(map(field)) match {
    case NIL => Try(None)
    case _ => getInt(field).map(Option.apply)
  }

  def getString(field: Int): Try[String] = getStringByIndex(map(field))

  def getStringByIndex(idx: Int): Try[String] = {
    if (MsgReader.isString(data.get(idx))) {
      val metaLength = sizeOfMetadataAt(idx)
      val length = sizeOfFieldAt(idx) - metaLength
      if (idx + metaLength + length <= data.limit) {
        val buff = Array.ofDim[Byte](length)
        data.position(idx + metaLength)
        data.get(buff, 0, length)
        Success(new String(buff, "UTF-8"))
      } else {
        Failure(new Exception(s"String would exceed the underlying buffer, perhaps a malformed packet?  Index: $idx, MetadataLength: $metaLength, StrLen: $length, Limit: ${data.limit}"))
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
  def getBlob(field: Int): Try[ByteBuffer] = getBlobByIndex(map(field))

  protected def getBlobByIndex(idx: Int): Try[ByteBuffer] = {
    def size = data.get(idx) match {
      case BIN_8 => Try(data.get(idx + 1) & 0xff)
      case BIN_16 => Try(data.getShort(idx + 1) & 0xffff)
      case BIN_32 => Try(data.getInt(idx + 1))  // yes this means blobs top out at 2GB
      case _ => Failure(new Exception(s"Binary format not found at $idx."))
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

  def getSeq(field: Int): Try[Seq[Any]] = getSeqByIndex(map(field))

  def getSeq[T](field: Int, t: T): Try[Seq[T]] = {
    getSeq(field).flatMap(xs => Try(xs.map(_.asInstanceOf[T])))
  }

  protected def getSeqByIndex(idx: Int): Try[Seq[Any]] = {
    val first = data.get(idx)
    if (MsgReader.isArray(first)) {
      val itemCount = first match {
        case ARRAY_32 => data.getInt(idx + 1)
        case ARRAY_16 => data.getShort(idx + 1) & 0xffff
        case _ => first & FIXARRAY_VALUE_MASK
      }
      // val metaLength = sizeOfMetadataAt(idx)
      var elementIdx = idx + sizeOfMetadataAt(idx)
      Try {
        (0 until itemCount).map { i =>
          val element = getAnyByIndex(elementIdx).get
          elementIdx += sizeOfFieldAt(elementIdx)
          element
        }
      }
    } else {
      Failure(new Exception(s"Array not found at $idx"))
    }
  }

  def getMap[K, V](field: Int): Try[Map[K, V]] = getMapByIndex(map(field))

  protected def getMapByIndex[K, V](idx: Int): Try[Map[K, V]] = {
    val first = data.get(idx)
    if (MsgReader.isMap(first)) {
      val itemCount = first match {
        case MAP_32 => data.getInt(idx + 1)
        case MAP_16 => data.getShort(idx + 1) & 0xffff
        case _ => first & FIXMAP_VALUE_MASK
      }
      var elementIdx = idx + sizeOfMetadataAt(idx)
      Try {
        (0 until itemCount).map { i =>
          val key = getAnyByIndex(elementIdx).get.asInstanceOf[K]
          elementIdx += sizeOfFieldAt(elementIdx)
          val value = getAnyByIndex(elementIdx).get.asInstanceOf[V]
          elementIdx += sizeOfFieldAt(elementIdx)
          ((key -> value))
        }.toMap
      }
    } else {
      Failure(new Exception(s"Map not found at $idx"))
    }
  }

  def getBytes: Array[Byte] = data.array

  // TODO: add getters for exts

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
    case ARRAY_16 => 3
    case ARRAY_32 => 5
    case f if isArray(f) => 1
    case BIN_8 => 2
    case BIN_16 => 3
    case BIN_32 => 5
    case MAP_16 => 3
    case MAP_32 => 5
    case f if isMap(f) => 1
    case STR_8 => 2
    case STR_16 => 3
    case STR_32 => 5
    case f if isString(f) => 1
    // TODO: finish me!
  }

  def sizeOfCollectionAt(start: Int, itemCount: Int, data: ByteBuffer): Int = {
    var size = 0
    (0 until itemCount).foreach { i =>
      size += sizeOfFieldAt(start + size, data)
    }
    size
  }

  def isArray(format: Byte): Boolean = {
    format == ARRAY_16 || format == ARRAY_32 || (format & FIXARRAY_MASK) == FIXMAP_CUTOFF
  }

  def isBlob(format: Byte): Boolean = {
    format == BIN_32 || format == BIN_16 || format == BIN_8
  }

  def isBoolean(b: Byte): Boolean = b == TRUE || b == FALSE

  def isFloat(format: Byte): Boolean = format == FLOAT_64 || format == FLOAT_32

  def isInt(format: Byte): Boolean = {
    format == UINT_64 || format == UINT_32 || format == UINT_16 || format == UINT_8 ||
    format == INT_64 || format == INT_32 || format == INT_16 || format == INT_8 ||
    (format & POSITIVE_FIXINT_CUTOFF) == 0 || format >= NEGATIVE_FIXINT_THRESHHOLD
  }

  def isMap(format: Byte): Boolean = {
    format == MAP_32 || format == MAP_16 || (FIXMAP_THRESHHOLD <= format && format < FIXMAP_CUTOFF)
  }

  def isNil(b: Byte): Boolean = b == NIL

  def isString(format: Byte): Boolean = {
    format == STR_8 || format == STR_16 || format == STR_32 || (format & FIXSTR_MASK) == FIXARRAY_CUTOFF
  }
}
