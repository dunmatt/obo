package com.github.dunmatt.obo.core

import squants.time.Time

object SquantsParameterSerializers {
  implicit object SquantsParameterSerializers extends ParameterSerializer[Time] {
    def parse(s: String): Option[Time] = Time(s).toOption
  }
}
