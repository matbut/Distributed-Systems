package pl.edu.agh.sr.akka.bookstore.server.database

import java.io.FileNotFoundException

import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.duration._

class DatabaseSearcher extends Actor with ActorLogging {
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: FileNotFoundException => Escalate
      case _ => Restart
    }

  override def postStop(): Unit = {
    context.children.foreach(context.stop)
  }

  val databases = List("database/foreignBooks.json", "database/polishBooks.json")

  override def receive: Receive = {
    case request: SearchRequest =>
      context.become(receive(sender))
      databases.foreach(db => context.actorOf(Props(new FileSearcher(db))) ! request)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(responder: ActorRef): Receive = {
    case SearchResponse(Some(book)) =>
      responder ! SearchResponse(Some(book))
      context.stop(self)
    case SearchResponse(None) if context.children.size > 1 =>
      responder ! SearchResponse(None)
    case SearchResponse(None) =>
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}