package com.github.dunmatt.obo.utils.implicits

import squants.{ Quantity, QuantityRange }

object QuantityRangeImprovements {
  implicit class NearestPointableQR[Q <: Quantity[Q]](range: QuantityRange[Q]) {
    def nearestPointTo(q: Q): Q = {
      if (q < range.lower) {
        range.lower
      } else if (range.upper < q) {
        range.upper
      } else {
        q
      }
    }
  }
}
