package net.caoticode.dirwatcher

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Paths

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 * */

object ConfDSL {
  type EventListener = (Path, Path) => Unit
  type EventMapping = Map[EventKind, EventListener]

  sealed trait EventKind
  case object CREATED extends EventKind
  case object DELETED extends EventKind
  case object MODIFIED extends EventKind
  
  case class Config(path: String, events: EventMapping)
  
  object create {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (CREATED, block)
    }
  }

  object delete {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (DELETED, block)
    }
  }

  object modify {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (MODIFIED, block)
    }
  }

  class WatchFor(path: String) {
    def listen(events: EventMapping): Config = Config(path, events)
  }
  
  object Watchers {
    def apply(cnf: Config*) = cnf.toList
  }
  
  object watch {
    def apply(path: String): WatchFor = new WatchFor(path)
  }

  object Events {
    def apply(listeners: (EventKind, EventListener)*): EventMapping = listeners.toMap
  }

  implicit class RichPath(pathRef: Path) {
    def isDir = Files.isDirectory(pathRef, LinkOption.NOFOLLOW_LINKS)
    def path = pathRef.toString
    def exists = pathRef.toFile().exists()
    def parent = pathRef.getParent()
    def / (pathStr: String) = Paths.get(pathRef.toString(), pathStr)
  }
  
  implicit class PathedString(pathPart: String) {
    def / (path: Path) = Option(path) match {
      case Some(path) => Paths.get(pathPart, path.toString())
      case None => Paths.get(pathPart)
    }
  }
  
}