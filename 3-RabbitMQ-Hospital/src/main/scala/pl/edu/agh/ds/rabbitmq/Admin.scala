package pl.edu.agh.ds.rabbitmq

import com.rabbitmq.client.DeliverCallback
import pl.edu.agh.ds.rabbitmq.Doctor.{examinationExchange, channel, connection, props}

object Admin extends Staff("Admin") {

  channel.queueBind(replyQueueName, logInfoExchange, Log.routingKey)

  println("Type info message: ")
  while(true){
    val input = scala.io.StdIn.readLine()
    val info = Info(input)
    channel.basicPublish(logInfoExchange, info.routingKey, props,Serialization.toByteArray(info))
    println("Sent: " + info)
  }
}
