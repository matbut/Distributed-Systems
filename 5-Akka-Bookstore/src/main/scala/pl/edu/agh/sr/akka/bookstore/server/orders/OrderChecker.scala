package pl.edu.agh.sr.akka.bookstore.server.orders

import java.io.FileNotFoundException

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorRef, OneForOneStrategy, Props}
import pl.edu.agh.sr.akka.bookstore.communication._
import pl.edu.agh.sr.akka.bookstore.server.database.DatabaseSearcher

import scala.concurrent.duration._

class OrderChecker(val orderWriter:ActorRef) extends Actor with ActorLogging {
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorInitializationException => Escalate
      case _ => Restart
    }

  override def receive: Receive = {
    case OrderRequest(title) =>
      context.actorOf(Props[DatabaseSearcher]) ! SearchRequest(title)
      context.become(receive(sender))
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }

  def receive(responder: ActorRef): Receive = {
    case SearchResponse(Some(book)) =>
      orderWriter.tell(OrderRequest(book.title), responder)
      context.stop(self)
    case SearchResponse(None) =>
      responder ! OrderResponse(false)
      context.stop(self)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
