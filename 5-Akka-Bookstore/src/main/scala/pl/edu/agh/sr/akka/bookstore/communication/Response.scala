package pl.edu.agh.sr.akka.bookstore.communication

import akka.util.ByteString

sealed trait Response extends Serializable

final case class SearchResponse(book: Option[Book]) extends Response

final case class OrderResponse(status: Boolean) extends Response

final case class StreamResponse(stream: ByteString) extends Response