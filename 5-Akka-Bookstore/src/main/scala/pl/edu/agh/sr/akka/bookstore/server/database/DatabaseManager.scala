package pl.edu.agh.sr.akka.bookstore.server.database

import java.io.FileNotFoundException

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import pl.edu.agh.sr.akka.bookstore.communication.SearchRequest

import scala.concurrent.duration._

class DatabaseManager extends Actor with ActorLogging {
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: FileNotFoundException => Escalate
      case _ => Restart
    }

  override def receive: Receive = {
    case request: SearchRequest =>
      context.actorOf(Props[DatabaseSearcher]).tell(request, sender)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}