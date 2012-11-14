package com.regesta.autowatermark.actors

import akka.actor.Actor
import com.regesta.autowatermark.FSListener

class ListenerActor(listener: FSListener) extends Actor {
	def receive = {
	    case Messages.Create(path) => listener.onCreate(path)
	    case Messages.Delete(path) => listener.onDelete(path)
	    case Messages.Modify(path) => listener.onModify(path)
	    case _ => 
	}
}