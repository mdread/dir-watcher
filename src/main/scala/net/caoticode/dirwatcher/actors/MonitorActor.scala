package net.caoticode.dirwatcher.actors

import akka.actor._
import java.io.File
import net.caoticode.dirwatcher._
import net.caoticode.dirwatcher.actors.Messages._
import java.nio.file._
import java.nio.file.StandardWatchEventKinds._
import util.control.Breaks._
import scala.collection.JavaConversions._
import java.nio.file.attribute.BasicFileAttributes
import akka.routing.BroadcastGroup

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 * */

object MonitorActor {
  case object Start
}

class MonitorActor(root: Path, recursive: Boolean, listeners: List[FSListener]) extends Actor {
  private val watcher = FileSystems.getDefault().newWatchService()
  private var keys: Map[WatchKey, Path] = Map.empty
  private val lestenerRouter = {
    val routees: List[String] = listeners map { l => context.actorOf(Props(classOf[ListenerActor], l)).path.toStringWithoutAddress }
    context.actorOf(BroadcastGroup(routees).props(), "monitorRouter")
  }

  if (recursive)
    registerAll(root)
  else
    register(root)

  def receive = {
    case MonitorActor.Start => breakable {
      while (true) {
        // wait for key to be signalled
        var key: WatchKey = null
        try {
          key = watcher.take();
        } catch {
          case _ : Throwable => break
        }

        keys.get(key) match {
          case Some(dir) => {
            for (event <- key.pollEvents()) {
              val kind = event.kind();

              if (kind != OVERFLOW) {
                val ev = event.asInstanceOf[WatchEvent[Path]]
                val name = ev.context()
                val child = dir.resolve(name)

                // broadcast event to all listeners
                if (kind == ENTRY_CREATE) lestenerRouter ! Create(child)
                else if (kind == ENTRY_DELETE) lestenerRouter ! Delete(child)
                else if (kind == ENTRY_MODIFY) lestenerRouter ! Modify(child)

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                  try {
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
                      registerAll(child);
                  } catch {
                    case _ : Throwable =>
                  }
                }
              }
            }

            // reset key and remove from set if directory no longer accessible
            if (!key.reset()) {
              keys = keys - key

              // all directories are inaccessible
              if (keys.isEmpty) break
            }
          }
          case None =>
        }
      }

      context.children.foreach { context.stop(_) }
      context.stop(self)
    }
    case _ =>
  }

  private def register(path: Path) {
    keys += path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY) -> path
  }

  private def registerAll(path: Path) {
    Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes) = {
        register(dir);
        FileVisitResult.CONTINUE;
      }
    });
  }
}