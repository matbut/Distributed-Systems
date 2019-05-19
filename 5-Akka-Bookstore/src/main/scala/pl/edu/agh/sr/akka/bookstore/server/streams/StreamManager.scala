package pl.edu.agh.sr.akka.bookstore.server.streams

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorInitializationException, ActorLogging, OneForOneStrategy, Props}
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.duration._

class StreamManager extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorInitializationException => Escalate
      case _ => Restart

    }
  override def receive: Receive = {
    case request: StreamRequest =>
      context.actorOf(Props[StreamChecker]).tell(request, sender)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
