package com.regesta.autowatermark.actors

import akka.actor._
import java.io.File
import com.regesta.autowatermark._
import java.nio.file._
import java.nio.file.StandardWatchEventKinds._
import util.control.Breaks._
import scala.collection.JavaConversions._
import java.nio.file.attribute.BasicFileAttributes
import Messages._
import akka.routing.BroadcastRouter

object MonitorActor {
  case object Start
}

class MonitorActor(root: Path, recursive: Boolean, listeners: List[FSListener]) extends Actor {
	private val watcher = FileSystems.getDefault().newWatchService()
	private var keys: Map[WatchKey, Path] = Map.empty
	private val lestenerRouter = {
	  val routees: List[ActorRef] = listeners map { l => context.actorOf(Props(new ListenerActor(l))) }
	  context.actorOf(Props[ListenerActor].withRouter(BroadcastRouter(routees = routees))) 
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
	              case _ => break
	            }

	            keys.get(key) match {
	              case Some(dir) => {
	                for (val event <- key.pollEvents()) {
		                val kind = event.kind();
		
		                // TBD - provide example of how OVERFLOW event is handled
		                if (kind != OVERFLOW) {
		                    // Context for directory entry event is the file name of entry
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
			                      case _ =>
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