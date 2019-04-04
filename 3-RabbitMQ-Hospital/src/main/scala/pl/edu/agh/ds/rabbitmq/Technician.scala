package pl.edu.agh.ds.rabbitmq

import com.rabbitmq.client._

object Technician extends Staff("Technician"){

  private val specializationsNumber = 2

  channel.queueBind(replyQueueName, exchangeName, Info.routingKey)

  try{
    1.to(specializationsNumber).foreach(i => {
      val input = scala.io.StdIn.readLine("Specify " + i + " specialization: ")
      val injury = Injury.valueOf(input)
      channel.basicConsume(injury.queueName, false, deliverCallback, _ => { })
    })
  }catch {
    case iae: IllegalArgumentException => println("Ej, " + iae.getMessage)
  }

  def deliverCallback: DeliverCallback = (_, delivery) => {
    val message = Serialization.fromByteArray(delivery.getBody)
    println("Received: " + message)
    message match{
      case ExaminationRequest(surname,injury) =>
        Thread.sleep((Math.random * 10000).toLong)
        val results = ExaminationResult(surname,injury," is broken")
        val log = Log(results)
        channel.basicPublish("", delivery.getProperties.getReplyTo, props, Serialization.toByteArray(results))
        channel.basicPublish(exchangeName, log.routingKey, props, Serialization.toByteArray(log))
        println("Sent: " + results)
    }
    channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }
}
