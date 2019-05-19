package pl.edu.agh.sr.akka.bookstore.server

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pl.edu.agh.sr.akka.bookstore.communication._
import pl.edu.agh.sr.akka.bookstore.server.database.DatabaseManager
import pl.edu.agh.sr.akka.bookstore.server.orders.OrderManager
import pl.edu.agh.sr.akka.bookstore.server.streams.StreamManager

import scala.concurrent.duration._
import scala.io.StdIn

class Server extends Actor with ActorLogging {
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _ => Stop
    }

  private val databaseManager = context.actorOf(Props[DatabaseManager], "databaseManager")
  private val orderManager = context.actorOf(Props[OrderManager], "orderManager")
  private val streamManager = context.actorOf(Props[StreamManager], "streamManager")

  override def receive: Receive = {
    case request: SearchRequest =>
      databaseManager.tell(request, sender)
    case request: OrderRequest =>
      orderManager.tell(request, sender)
    case request: StreamRequest =>
      streamManager.tell(request, sender)
    case unsupported =>
      log.warning(s"Received unsupported message [$unsupported]")
  }
}

object Server extends App{
  val config: Config = ConfigFactory.load("server")
  val system = ActorSystem("server", config)
  system.actorOf(Props[Server], "server")

  Iterator.continually(StdIn.readLine()).takeWhile(_ != "q").foreach(_ => {})
  system.terminate()
}