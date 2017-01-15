package com.github.dunmatt.obo.utils.implicits

object MapImprovements {
  implicit class DoubleMapalbeMap[K, V](m: collection.Map[K, V]) {
    def doubleMap[A, B](f: ((K, V)) => ((A, B))): Map[A, B] = m.toSeq.map(f).toMap
  }
}
