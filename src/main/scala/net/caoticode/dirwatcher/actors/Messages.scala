package net.caoticode.dirwatcher.actors

import java.nio.file.Path

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 * */

object Messages {
  sealed trait FSEvent
  case class Create(path: Path) extends FSEvent
  case class Delete(path: Path) extends FSEvent
  case class Modify(path: Path) extends FSEvent
}