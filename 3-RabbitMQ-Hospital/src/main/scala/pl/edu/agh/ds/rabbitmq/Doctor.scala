package pl.edu.agh.ds.rabbitmq

import com.rabbitmq.client._

object Doctor extends Staff("Doctor"){

  channel.queueBind(replyQueueName, exchangeName, Info.routingKey)

  while(true){
    try{
      val input = scala.io.StdIn.readLine("Type surname and injury: ").split("\\s+")
      if (input.length != 2) throw new IllegalArgumentException("Please, type exactly two arguments.")

      val request = ExaminationRequest(input(0),input(1))
      val log = Log(request)
      channel.basicPublish(exchangeName, request.routingKey, props,Serialization.toByteArray(request))
      channel.basicPublish(exchangeName, log.routingKey, props, Serialization.toByteArray(log))
      println("Sent: " + request)
    }catch{
      case iae: IllegalArgumentException => println(iae.getMessage)
    }
  }
}
