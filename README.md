dir-watcher
===========

Directory watcher library and app, easily watch a directory and execute some action on file create, delete, and modify events

You can use this project either as a library from scala/java or as a stand alone application.

using it as a library you have more control over the implementation
of listeners but the stand-anone version is pretty flexible too
because it uses scala itself as a configuration file, with the combination
of a simple DSL to implement listeners and watch for events
