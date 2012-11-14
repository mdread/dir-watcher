package net.caoticode.dirwatcher

import java.nio.file.Path

object Listener {
  	type Listener = Path => Unit
  	val noop: Listener = ref => {}
  	
  	def apply(create: Listener = noop, delete: Listener = noop, modify: Listener = noop): FSListener = {
  		new FSListener {
  			override def onCreate(ref: Path) = create(ref)
  			override def onDelete(ref: Path) = delete(ref)
  			override def onModify(ref: Path) = modify(ref)
  		}
  	}
  	
  	def create(listener: Listener) = apply(create = listener)
  	def delete(listener: Listener) = apply(delete = listener)
  	def modify(listener: Listener) = apply(modify = listener)
  	val empty = apply()
}

trait FSListener {
	def onCreate(ref: Path): Unit
	def onDelete(ref: Path): Unit
	def onModify(ref: Path): Unit
}

class FSListenerAdapter extends FSListener {
	override def onCreate(ref: Path) = Listener.noop
	override def onDelete(ref: Path) = Listener.noop
	override def onModify(ref: Path) = Listener.noop
}