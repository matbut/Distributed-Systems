package pl.edu.agh.ds.rabbitmq

import com.rabbitmq.client.DeliverCallback
import pl.edu.agh.ds.rabbitmq.Doctor.{exchangeName, channel, connection, props}

object Admin extends Staff("Admin") {

  channel.queueBind(replyQueueName, exchangeName, Log.routingKey)

  while(true){
    val input = scala.io.StdIn.readLine("Type info message: ")
    val info = Info(input)
    channel.basicPublish(exchangeName, info.routingKey, props,Serialization.toByteArray(info))
    println("Sent: " + info)
  }
}
