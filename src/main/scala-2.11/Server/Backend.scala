package Server

import POJOs.Page

import scala.collection.mutable

object Backend {

  var pages = new mutable.MutableList[Page]
  var index = 0

  def registerNewUser(name: String): Int = {
    pages += new Page(name)
    index = index + 1
    return index - 1
  }

}
