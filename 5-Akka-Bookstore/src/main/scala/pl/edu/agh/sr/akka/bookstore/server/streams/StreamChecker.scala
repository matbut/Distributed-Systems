package pl.edu.agh.sr.akka.bookstore.server.streams

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorRef, OneForOneStrategy, Props}
import akka.util.ByteString
import pl.edu.agh.sr.akka.bookstore.communication._
import pl.edu.agh.sr.akka.bookstore.server.database.DatabaseSearcher

import scala.concurrent.duration._

class StreamChecker() extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorInitializationException => Escalate
      case _ => Restart
    }

  override def receive: Receive = {
    case StreamRequest(title) =>
      context.actorOf(Props[DatabaseSearcher]) ! SearchRequest(title)
      context.become(receive(sender))
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(responder: ActorRef): Receive = {
    case SearchResponse(Some(book)) =>
      context.actorOf(Props(new FileStreamer(book.file))).tell(StreamRequest(book.title), responder)
    case SearchResponse(None) =>
      responder ! StreamResponse(ByteString.fromString("None"))
      context.stop(self)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
