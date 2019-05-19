package pl.edu.agh.sr.akka.bookstore.server.orders

import akka.actor.{Actor, ActorLogging}
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.reflect.io.File

class OrderWriter(val filePath:String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case OrderRequest(title) =>
      File(filePath).appendAll(title, System.lineSeparator)
      sender ! OrderResponse(true)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}