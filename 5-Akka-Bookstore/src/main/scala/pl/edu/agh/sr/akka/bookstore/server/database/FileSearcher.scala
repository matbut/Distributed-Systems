package pl.edu.agh.sr.akka.bookstore.server.database

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import net.liftweb.json._
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.io.{BufferedSource, Source}

class FileSearcher(val filePath:String) extends Actor with ActorLogging {
    implicit val formats: DefaultFormats.type = DefaultFormats

    val source: BufferedSource = Source.fromFile(filePath)

    val database: List[Book] = parse(try source.mkString finally source.close()).extract[List[Book]]

    def receive:Receive = LoggingReceive {
        case SearchRequest(title) => {
            sender ! SearchResponse(database.find(_.title == title))
            context.stop(self)
        }
        case unsupported => log.warning(s"Received unsupported message [$unsupported]")
    }
}
