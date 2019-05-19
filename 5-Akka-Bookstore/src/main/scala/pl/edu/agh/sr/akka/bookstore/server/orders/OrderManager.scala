package pl.edu.agh.sr.akka.bookstore.server.orders

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.duration._

class OrderManager extends Actor with ActorLogging {
  
  val filePath = "database/orders.txt"
  private val path = Paths.get(filePath)
  if (!Files.exists(path)) Files.createFile(path)

  val orderWriter:ActorRef = context.actorOf(Props(new OrderWriter(filePath)), "orderWriter")

  override def receive: Receive = {
    case request: OrderRequest =>
      context.actorOf(Props(new OrderChecker(orderWriter))).tell(request, sender)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
