package net.caoticode.dirwatcher

import scala.io.Source

import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.LinkOption

import com.twitter.util.Eval

import com.sun.org.apache.xml.internal.serializer.ToSAXHandler

import net.caoticode.dirwatcher.ConfDSL._
import net.caoticode.dirwatcher.actors.MonitorActor

/**
 * @author Daniel Camarda (0xcaos@gmail.com)
 */

class EngineType

object Main extends App {
//  val engine = new ScriptEngineManager().getEngineByName("scala")
//  val settings = engine.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
//  settings.embeddedDefaults[EngineType]
//  settings.usejavacp.value = true
  
  val confPath = if(args.isEmpty) "watcher.conf" else args(0)
  val confScript = List(
      "import net.caoticode.dirwatcher.ConfDSL._",
      "import scala.sys.process._",
      Source.fromFile(confPath)("UTF-8").mkString ).mkString("\n")
  

  print("checking and parsing configuration... ")
  val confList: List[Config] = new Eval()(confScript)
  
  println("OK ")
  print("initializing watchers... ")
  
  val watcher = DirWatcher()
  
  for (config <- confList) {
    val rootPath = Paths.get(config.path)
    val listeners = config.events.map {
      case (eventType, action) => {
        val defaultAction: (Path => Unit) = (path: Path) => {
          try{
        	  action(rootPath, path)
          } catch {
            case e : Throwable => println(e.getMessage())
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

