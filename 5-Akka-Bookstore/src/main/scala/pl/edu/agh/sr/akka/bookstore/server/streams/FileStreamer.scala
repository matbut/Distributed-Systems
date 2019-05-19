package pl.edu.agh.sr.akka.bookstore.server.streams

import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import pl.edu.agh.sr.akka.bookstore.communication._

import scala.concurrent.duration._

class FileStreamer(val filePath:String) extends Actor with ActorLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

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
