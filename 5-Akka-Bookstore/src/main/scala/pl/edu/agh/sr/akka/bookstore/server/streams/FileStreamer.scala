package pl.edu.agh.sr.akka.bookstore.server.streams

import java.io.FileNotFoundException
import java.nio.file.Paths

import akka.Done
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, OneForOneStrategy}
import akka.stream.{ActorMaterializer, IOResult, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class FileStreamer(val filePath:String) extends Actor with ActorLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor:ExecutionContext = context.system.dispatcher

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: FileNotFoundException => Escalate
      case _ => Restart
    }

  override def receive: Receive = {
    case _:StreamRequest =>
      FileIO.fromPath(Paths.get(filePath))
        .via(Framing.delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 512, allowTruncation = true))
        .throttle(1, 1.second, 1, ThrottleMode.shaping)
        .map(StreamResponse)
        .to(Sink.actorRef(sender, StreamResponse(ByteString.fromString("Done"))))
        .run()
    case unsupported => log.warning(s"Received unsupported message [$unsupported]")
  }
}
