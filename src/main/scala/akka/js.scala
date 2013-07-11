package akka.js

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util._
import scala.concurrent._
import scala.concurrent.duration._
import org.mashupbots.socko.events._
import org.mashupbots.socko.infrastructure.Logger
import java.io.File
import org.jboss.netty.channel.Channel
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import scala.util.parsing.json._
import ExecutionContext.Implicits.global

case class WebSocketClientRegister(channel: Channel)

class WebSocketClientActor(channel: Channel) extends Actor with Logger {
  def receive = {
    case in @ JsonMsg(path, msg, replyTo) =>
      log.debug(self.path + " got " + in)

      val target = context.actorFor("/user" + path)
      replyTo.map { rt =>
        implicit val timeout = Timeout(5 seconds)
        (target ? msg).map { res => (replyTo, res) } pipeTo self
      } getOrElse {
        target ! msg
      }

    case (Some(replyTo), res) =>
      log.debug(self.path + " replying to " + replyTo)

      val msg = JSONObject(Map("msg" -> res, "replyTo" -> replyTo))

      channel.write(new TextWebSocketFrame(msg.toString()))

  }
}

object WebSocketClientStore {
  val channels = scala.collection.mutable.Map[Channel, ActorRef]()
}

class WebSocketClientStore extends Actor with Logger {

  def receive = {
    case WebSocketClientRegister(channel) =>
      log.info("WebSocketClientActor registered for channel " + channel)
      WebSocketClientStore.channels(channel) = context.actorOf(Props(new WebSocketClientActor(channel)))
  }
}


case class JsonMsg(path: String, msg: Any, replyTo: Option[String])

class WebSocketClientHandler extends Actor with Logger {
  def receive = {
    case frame: WebSocketFrameEvent =>
      log.info("WebSocketClientHandler got msg")
      for {
        in    <- parseIncomming(frame.readText())
        actor <- WebSocketClientStore.channels.get(frame.channel)
      } yield {
        log.debug("input: " + in)
        log.debug("actor: " + actor)

        actor ! in
      }

      context.stop(self)
  }

  protected def parseIncomming(data: String): Option[JsonMsg] = {
    JSON.parseRaw(data).collect {
      case JSONObject(map) => for {
        path <- map.get("path")
        msg <- map.get("msg")
      } yield JsonMsg(path.toString, msg, map.get("replyTo").map(_.toString))
    }.flatMap(identity)
  }
}
