package pl.edu.agh.sr.akka.bookstore.client

import akka.actor.ActorRef.noSender
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.io.StdIn

class Client(serverPath: String) extends Actor with ActorLogging {
  private val searchCommand = "search (.+)".r
  private val orderCommand = "order (.+)".r
  private val streamCommmand = "stream (.+)".r

  override def receive: Receive = {
    case command:String =>
      command match {
        case searchCommand(instr) =>
          context.actorSelection(serverPath) ! SearchRequest(instr)
        case orderCommand(instr) =>
          context.actorSelection(serverPath) ! OrderRequest(instr)
        case streamCommmand(instr) =>
          context.actorSelection(serverPath) ! StreamRequest(instr)
        case instr =>
          println(s"Unsupported command: '$instr'")
      }
    case SearchResponse(book) => println(book.getOrElse(None))
    case OrderResponse(status) => println(s"Order status: [$status]")
    case StreamResponse(stream) => println(stream.utf8String)
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}

object Client extends App {
  val serverPath = "akka.tcp://server@127.0.0.1:5544/user/server"

  val config: Config = ConfigFactory.load("client")
  val system = ActorSystem("client", config)
  val client = system.actorOf(Props(new Client(serverPath)), "client")

  Iterator.continually(StdIn.readLine()).takeWhile(_ != "q").foreach(client.tell(_, noSender))
  system.terminate()
}
