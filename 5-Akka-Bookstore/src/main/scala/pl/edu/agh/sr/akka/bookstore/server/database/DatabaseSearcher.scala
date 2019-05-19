package pl.edu.agh.sr.akka.bookstore.server.database

import java.io.FileNotFoundException

import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorRef, Kill, OneForOneStrategy, Props}
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.event.LoggingReceive
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.duration._

class DatabaseSearcher extends Actor with ActorLogging {
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorInitializationException => Escalate
      case _ => Restart
    }

  override def postStop(): Unit = {
    context.children.foreach(context.stop)
  }

  val databases = List("database/foreignBooks.json", "database/polishBooks.json")

  override def receive: Receive = LoggingReceive {
    case request: SearchRequest =>
      context.become(receive(databases.size, sender))
      databases.foreach(db => context.actorOf(Props(new FileSearcher(db))) ! request)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(size:Int, responder: ActorRef): Receive = LoggingReceive {
    case SearchResponse(None) if size > 1 =>
      context.become(receive(size-1,responder))
    case response:SearchResponse =>
      responder ! response
      context.stop(self)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}