package pl.edu.agh.sr.akka.bookstore.server.orders

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pl.edu.agh.sr.akka.bookstore.communication._
import pl.edu.agh.sr.akka.bookstore.server.database.DatabaseSearcher

class OrderChecker(val orderWriter:ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case OrderRequest(title) =>
      context.actorOf(Props[DatabaseSearcher]) ! SearchRequest(title)
      context.become(receive(sender))
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(responder: ActorRef): Receive = {
    case SearchResponse(Some(book)) => orderWriter.tell(OrderRequest(book.title), responder)
    case SearchResponse(None) => responder ! OrderResponse(false)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
