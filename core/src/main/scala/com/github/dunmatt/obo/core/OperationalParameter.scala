package com.github.dunmatt.obo.core

class OperationalParameter( name: String
                          , initialValue: String
                          , description: String = "")
      extends TypedOperationalParameter(name, initialValue, description)(OperationalParameterSerializers.StringSerializer) {
}

class OperationalFlag( name: String
                     , initialValue: Boolean
                     , description: String = "")
      extends TypedOperationalParameter(name, initialValue, description)(OperationalParameterSerializers.BooleanSerializer) {
}

class TypedOperationalParameter[T]( val name: String
                                  , initialValue: T
                                  , val description: String = "")
                                  (implicit serializer: ParameterSerializer[T]) {
  private var v = initialValue

  private var listeners = Set.empty[(String, T) => Unit]

  def addChangeListener(pcl: ParameterChangedListener[T]) {
    foreach((s: String, v: T) => pcl.onParameterChanged(s, v))
  }

  def foreach[U >: T](f: (String, U) => Unit): Unit = {
    listeners = listeners + f
    f(name, v)
  }

  def fromString(s: String): Unit = serializer.parse(s).foreach(value = _)

  def value: T = v

  def value_=(nv: T): Unit = {
    v = nv
    listeners.foreach(_(name, nv))
  }

  def valueString: String = serializer.serialize(v)
}

trait ParameterChangedListener[T] {
  def onParameterChanged[U >: T](name: String, newValue: U): Unit
}

trait ParameterSerializer[T] {
  def serialize(v: T): String = v.toString
  def parse(s: String): Option[T]
}

object OperationalParameterSerializers {
  implicit object BooleanSerializer extends ParameterSerializer[Boolean] {
    def parse(s: String): Option[Boolean] = Option(s).map(_.toBoolean)
  }

  implicit object DoubleSerializer extends ParameterSerializer[Double] {
    def parse(s: String): Option[Double] = Option(s).map(_.toDouble)
  }

  implicit object IntSerializer extends ParameterSerializer[Int] {
    def parse(s: String): Option[Int] = Option(s).map(_.toInt)
  }

  implicit object StringSerializer extends ParameterSerializer[String] {
    override def serialize(v: String): String = v
    def parse(s: String): Option[String] = Option(s)
  }

}
