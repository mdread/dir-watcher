package net.caoticode.dirwatcher

import java.nio.file.Path

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 * */

object ConfDSL {
  type EventListener = (Boolean, String) => Option[Seq[String]]
  type EventMapping = Map[EventKind, EventListener]

  sealed trait EventKind
  case object CREATE extends EventKind
  case object DELETE extends EventKind
  case object MODIFY extends EventKind
  
  case class Config(path: String, events: EventMapping)
  
  object create {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (CREATE, block)
    }
  }

  object delete {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (DELETE, block)
    }
  }

  object modify {
    def apply(block: EventListener): (EventKind, EventListener) = {
      (MODIFY, block)
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

  object Exec {
    def apply(exec: String*): Some[Seq[String]] = Some(exec)
  }
}