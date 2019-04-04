package pl.edu.agh.ds.rabbitmq

import java.util.UUID

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._
import pl.edu.agh.ds.rabbitmq.Technician.{channel, exchangeName}

abstract class Staff(val name:String) extends App{

  protected val exchangeName = "ExaminationsExchange"

  protected val factory = new ConnectionFactory
  factory.setHost("localhost")
  protected val connection:Connection = factory.newConnection
  protected val channel:Channel = connection.createChannel
  channel.exchangeDeclare(exchangeName, "topic")

  println(name)

  val replyQueueName: String = channel.queueDeclare.getQueue
  val replyQueueCallback: DeliverCallback = (_, delivery) => {
    val message = Serialization.fromByteArray(delivery.getBody)
    println("Received: " + message)
  }
  channel.basicConsume(replyQueueName, false, replyQueueCallback, _ => { })
  val props = new BasicProperties.Builder().correlationId(UUID.randomUUID().toString)
    .replyTo(replyQueueName)
    .build()

  Injury.values.foreach(injury => {
    channel.queueDeclare(injury.queueName,false,false,false,null)
    channel.queueBind(injury.queueName, exchangeName, injury.routingKey)
    channel.basicQos(1)
  })
}
