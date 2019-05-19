package pl.edu.agh.sr.akka.bookstore.communication

sealed trait Request extends Serializable{ def title: String }

final case class SearchRequest(title: String) extends Request

final case class OrderRequest(title: String) extends Request

final case class CheckedOrderRequest(title: String) extends Request

final case class StreamRequest(title: String) extends Request