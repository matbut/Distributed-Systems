package pl.edu.agh.sr.akka.bookstore.server.streams

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.ByteString
import pl.edu.agh.sr.akka.bookstore.communication._
import pl.edu.agh.sr.akka.bookstore.server.database.DatabaseSearcher

class StreamChecker() extends Actor with ActorLogging {

  override def receive: Receive = {
    case StreamRequest(title) =>
      context.actorOf(Props[DatabaseSearcher]) ! SearchRequest(title)
      context.become(receive(sender))
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(responder: ActorRef): Receive = {
    case SearchResponse(Some(book)) => context.actorOf(Props(new FileStreamer(book.file))).tell(StreamRequest(book.title), responder)
    case SearchResponse(None) => responder ! StreamResponse(ByteString.fromString("None"))
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
