dir-watcher
===========

Directory watcher let you set a watcher for a directory and listen to create, delete, and modify events, then execute the appropriate command.

This project can be used both as a stand-alone application, and as a **Scala** or **Java** library embedded into another project.

Usage as a library give more control over the implementation of listeners, but the stand-alone version is pretty flexible thanks to the use of a *Scala DSL* for configuration.

## Build and install
There are three main options:

1. download the compiled jar and include it in your project, or use it as a stand-alone application. it can be downloaded from the [dist](https://github.com/mdread/dir-watcher/tree/master/dist) folder.
2. clone the project and build the stand-alone jar with [sbt](http://www.scala-sbt.org/)
```bash
git clone https://github.com/mdread/dir-watcher.git
cd dir-watcher
sbt assembly
```
compiled jar will be in **./target/scala-2.11** directory
3. clone the project and install it in local repository to use it as a library
```bash
git clone https://github.com/mdread/dir-watcher.git
cd dir-watcher
sbt publishLocal
```
then include it as a dependency in build.sbt
```
libraryDependencies += "net.caoticode.dirwatcher" %% "dir-watcher" % "0.1.0"
```

## Usage as stand-alone application

### Configuration

First thing is to create a configuration file to instruct *dir-watcher* which folders to watch for, and the actions to take when an event is fired. Next an example configuration file:

`watcher.conf`
```
Watchers(
  watch("/home/mdread/Documents/test") listen Events(
    create { (root, file) =>
      println("created - " + file.path)
    },
    delete { (root, file) =>
      println("deleted - " + file.path)
    },
    modify { (root, file) =>
      println("modified - " + file.path)
    }
  )
)
```

Configuration is actually done in Scala itself, with a small DSL for declaring directories to watch, and implement event listeners.

* `Watchers` takes a list of `watch`
* `watch` takes a path to the folder to watch for events (it is recursive)
* `Events` takes a list of events, possible options are: `create`, `delete` and `modify`. Each of those events is a function with two input parameters, **root** and **file**, representing the parent directory where the file lives, and the actual file for which the event has been fired.
* **root** and **file** are both *java.nio.file.Path objects*, with some shortcut methods added:
  * `isDir` returns true if the path refers to a directory.
  * `path` returns the string version of the full path.
  * `exists` returns true if the file exists.
  * `parent` returns a *nio.file.Path* object referencing the parent directory.
  * `/` useful to build a new path from the previous one, for example `root / "myfile.jpg"` returns a *nio.file.Path* object referencing a *myfile.jpg* file in the **root** folder.

Also the `scala.sys.process` package gets imported automatically in the configuration file, this makes easy to call external processes on events, for more information on how to use it refer to the [scaladoc](http://www.scala-lang.org/api/current/index.html#scala.sys.process.package)

### Execution

Run it as any executable file, with the configuration file path as parameter. Ex:

```bash
java -jar dir-watcher-assembly-0.1.0.jar watcher.conf
```

## Usage as library

### Scala

From Scala you can choose to implement `FSListener` trait, as shown in the following example:

```scala
import net.caoticode.dirwatcher.DirWatcher
import net.caoticode.dirwatcher.FSListener

class LogListener extends FSListener {
  import java.nio.file.Path

  override def onCreate(ref: Path): Unit = println(s"created $ref")
  override def onDelete(ref: Path): Unit = println(s"deleted $ref")
  override def onModify(ref: Path): Unit = println(s"modified $ref")
}

object Main extends App {
  val directoryPath = "/home/mdread/Documents/test"

  val watcher = DirWatcher()
  watcher.watchFor(directoryPath, new LogListener())
  watcher.start()

  // somewhere later ...
  DirWatcher.shutdown()
}
```

or to use the helper methods on `Listener` object:

```scala
import net.caoticode.dirwatcher.DirWatcher
import net.caoticode.dirwatcher.Listener

object Main extends App {
  val directoryPath = "/home/mdread/Documents/test"

  val watcher = DirWatcher()

  watcher.watchFor(directoryPath,
      Listener.create{ file => println(s"created $file") },
      Listener.modify{ file => println(s"modified $file") },
      Listener.delete{ file => println(s"deleted $file") })

  watcher.start()

  // somewhere later ...
  DirWatcher.shutdown()
}
```

internally it uses [file change notification](https://docs.oracle.com/javase/tutorial/essential/io/notification.html) implemented in **java.nio.file** package, and [akka](http://akka.io/) to asyncronusly manage listeners. If you already use akka in your project, it is possible to pass the actor system object to DirWatcher class, just change the line
```
val watcher = DirWatcher()
```
with
```
val watcher = new DirWatcher(system)
```

because the watcher system manages a separate thread-pool (with akka) you need to `DirWatcher.shutdown()` it when you no longer need it.

### Java

Implement FSListener interface (alternatively extend FSListenerAdapter to override only the event you need)

```java
import java.nio.file.Path;
import net.caoticode.dirwatcher.FSListener;

public class LogListener implements FSListener {

	@Override
	public void onCreate(Path ref) {
		System.out.println("created " + ref);
	}

	@Override
	public void onDelete(Path ref) {
		System.out.println("deleted " + ref);
	}

	@Override
	public void onModify(Path ref) {
		System.out.println("modified " + ref);
	}
}
```

then use it with `DirWatcher` class

```java
import net.caoticode.dirwatcher.DirWatcher;

public class JavaTest {

	public static void main(String[] args) {
		String directoryPath = "/home/mdread/Documents/test";

		DirWatcher watcher = DirWatcher.apply();
	    watcher.watchFor(directoryPath, new LogListener());
	    watcher.start();

    	// somewhere later ...
	    DirWatcher.shutdown();
	}
}
```
