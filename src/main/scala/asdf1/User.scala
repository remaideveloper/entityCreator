package asdf1

import asdf.TestCls

class User {
  private var name: String = ""
  private val id: Long = 1L
  private var dec: Double = 1.3D
  private var testCls: List[TestCls] = _ // Для теста зацикленности

  override def toString: String = s"name: $name id: $id dec: $dec"
}
