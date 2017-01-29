package com.github.dunmatt.obo.core

import java.util.UUID

case class RuntimeResourceName(name: String) {
  import RuntimeResourceName._
  require(isRoot || !name.endsWith("/"), "Only root can end in slash.")
  require(name.forall(isValidNameCharacter), s"Resource names must match the regex [/_a-zA-Z0-9]+, $name does not meet that requirement.")
  require(!name.contains("//"), "Names cannot contain double slashes.")

  def -(prefix: String): RuntimeResourceName = {
    if (name.startsWith(prefix)) {
      if (prefix.endsWith("/")) {
        RuntimeResourceName(name.drop(prefix.length))
      } else {
        RuntimeResourceName(name.drop(prefix.length + 1))
      }
    } else {
      this
    }
  }

  def -(prefix: RuntimeResourceName): RuntimeResourceName = this - prefix.name

  def /(relative: String): RuntimeResourceName = {
    require(isRelativeString(relative), "Cannot scope a global path; call '/' with a relative resource name!")
    if (name.endsWith("/")) {
      RuntimeResourceName(s"$name${relative}")
    } else {
      RuntimeResourceName(s"$name/${relative}")
    }
  }

  def /(relative: RuntimeResourceName): RuntimeResourceName = this / relative.name

  def parent: RuntimeResourceName = name match {
    case "/" => this
    case _ => RuntimeResourceName(name.take(name.lastIndexOf('/')))
  }

  def parts: Seq[String] = name.split('/')

  def isGlobal: Boolean = isGlobalString(name)
  def isRelative: Boolean = !isGlobal
  def isRoot: Boolean = name == "/"
  def startsWith(other: RuntimeResourceName): Boolean = name.startsWith(other.name)
}

object RuntimeResourceName {
  val ROOT = RuntimeResourceName("/")

  def isAlphaNumeric(c: Char): Boolean = 'a' <= c && c <= 'z' ||
                                         'A' <= c && c <= 'Z' ||
                                         '0' <= c && c <= '9'
  def isGlobalString(s: String): Boolean = s.startsWith("/")
  def isRelativeString(s: String): Boolean = !isGlobalString(s)
  def isValidNameCharacter(c: Char): Boolean = c == '/' || c == '_' || isAlphaNumeric(c)

  def apply(id: UUID): RuntimeResourceName = RuntimeResourceName(id.toString.replace('-', '_'))
}
