package pl.edu.agh.ds.rabbitmq

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

sealed trait Injury {
  def routingKey: String
  def queueName:String = "Queue" + toString
}
case object Hip extends Injury{ def routingKey = "hip"}
case object Knee extends Injury{ def routingKey = "knee"}
case object Elbow extends Injury{ def routingKey = "elbow"}
object Injury{
  def valueOf(string: String): Injury =
    values.find(_.toString.toLowerCase == string.toLowerCase).
      getOrElse(throw new IllegalArgumentException(string + " isn't valid Injury"))
  implicit def string2Injury(string: String): Injury = valueOf(string)
  def values:Vector[Injury] = Vector(Hip, Knee, Elbow)
}

sealed trait Message {def routingKey: String}
case class ExaminationRequest(surname:String,injury:Injury) extends Message{ def routingKey:String = injury.routingKey }
case class ExaminationResult(surname:String, injury:Injury, result:String) extends Message{ def routingKey:String = injury.routingKey }
case class Info(message:String) extends Message{ def routingKey:String = "info" }
case class Log(message:Message) extends Message{ def routingKey:String = "log" }
object Log{ def routingKey:String = "log" }
object Info{ def routingKey:String = "info" }

object Serialization{
  def toByteArray(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }
  def fromByteArray(bytes: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value
  }
}
