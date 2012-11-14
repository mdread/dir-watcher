package net.caoticode.dirwatcher

import akka.actor._
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout
import net.caoticode.dirwatcher.actors._
import java.nio.file.Paths
import java.nio.file.Path

object DirWatcher {
  private lazy val system = ActorSystem("dir-watcher")
  
  def apply() = new DirWatcher(system)
  def shutdown() = system.shutdown()
}

class DirWatcher(system: ActorSystem) {
  private var watchers: List[ActorRef] = Nil
  private var listenersRef: List[(Path, List[FSListener])] = Nil
  
  def watchFor(dir: String, listeners: FSListener*): DirWatcher = {
    watchFor(dir, listeners.toList)
    this
  }
  
  def watchFor(dir: String, listeners: List[FSListener]): DirWatcher = {
    listenersRef = (Paths.get(dir), listeners ) :: listenersRef
    this
  }
  
  def start(): DirWatcher = {
    watchers = listenersRef.map { case (dir, listeners) => system.actorOf( Props(new MonitorActor(dir, true, listeners ))) }
    watchers.foreach { _ ! MonitorActor.Start }
    this
  }
  
  def stop(): DirWatcher = {
    watchers.foreach { system.stop(_) }
    watchers = Nil
    this
  }
}
