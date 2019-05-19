package pl.edu.agh.sr.akka.bookstore.server.streams

import akka.actor.{Actor, ActorLogging, Props}
import pl.edu.agh.sr.akka.bookstore.communication._

class StreamManager extends Actor with ActorLogging {
  override def receive: Receive = {
    case request: StreamRequest =>
      context.actorOf(Props[StreamChecker]).tell(request, sender)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
