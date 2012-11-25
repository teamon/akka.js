package todo

import akka.actor._
import akka.js._
import org.mashupbots.socko.infrastructure.Logger


class EchoActor extends Actor with Logger {
  def receive = {
    case msg =>
      log.info("EchoActor Got: " + msg)
      sender ! msg
  }
}

class PingActor extends Actor with Logger {
  private var cnt = 0

  def receive = {
    case msg =>
      log.info("PingActor Got: " + msg)
      cnt += 1
      sender ! ("pong " + cnt)
  }
}
