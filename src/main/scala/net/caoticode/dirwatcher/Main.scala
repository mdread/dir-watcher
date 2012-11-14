package net.caoticode.dirwatcher

import akka.actor._
import java.nio.file.Paths
import java.nio.file.Path
import net.caoticode.dirwatcher.actors.MonitorActor
import com.twitter.util.Eval
import net.caoticode.dirwatcher.ConfDSL._
import java.nio.file.Files
import java.nio.file.LinkOption
import com.sun.org.apache.xml.internal.serializer.ToSAXHandler
import scala.io.Source

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 */

object Main extends App {
  val confPath = if(args.isEmpty) "watcher.conf" else args(0)
  val confScript = "import net.caoticode.dirwatcher.ConfDSL._\n" + Source.fromFile(confPath)("UTF-8").mkString
  
  print("checking and parsing configuration... ")
  
  val confList: List[Config] = new Eval()(confScript)
  
  println("OK")
  print("initializing watchers... ")
  
  val watcher = DirWatcher()
  for (config <- confList) {
    val rootPath = Paths.get(config.path)
    val listeners = config.events.map {
      case (eventType, action) => {
        val defaultAction: (Path => Unit) = (path: Path) => {
          val res = try {
            action(!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS), path.getFileName().toString())
          } catch {
            case e: MatchError => None 
          }
          
          res match {
            case Some(execTemplateSeq) => {
              import scala.sys.process._
              val execSeq = execTemplateSeq.map {e => 
                e.replaceAllLiterally("${file}", path.toString)
                .replaceAllLiterally("${root}", rootPath.toString)
                .replaceAllLiterally("${relativeToRoot}", rootPath.relativize(path).toString)
              }
              try{
            	  execSeq !
              } catch {
                case _ =>
              }
            }
            case None => println("none")
          }
        }
        
        eventType match {
          case CREATE => Listener.create(defaultAction)
          case DELETE => Listener.delete(defaultAction)
          case MODIFY => Listener.modify(defaultAction)
        }
      }
    }.toList

    watcher.watchFor(config.path, listeners)
  }

  watcher.start()
  
  println("OK")
  println("monitor running!")
}

