package todo

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util._
import akka.util.duration._
import akka.js._

import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver._
import org.mashupbots.socko.events._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.handlers._


object TodoApp extends Logger {
  val system = ActorSystem("TodoApp")
  val static = system.actorOf(Props(new StaticContentHandler(StaticContentHandlerConfig())))
  val clients = system.actorOf(Props[WebSocketClientStore])

  val echo = system.actorOf(Props[EchoActor], name = "echo")
  val ping = system.actorOf(Props[PingActor], name = "ping")

  val routes = Routes {
    case HttpRequest(req) => req match {
      case GET(Path("/")) =>
        static ! new StaticResourceRequest(req, "index.html")

      case GET(PathSegments("assets" :: path)) =>
        static ! new StaticResourceRequest(req, path.mkString("assets/", "/", ""))

      case GET(Path("/favicon.ico")) => {
        req.response.write(HttpResponseStatus.NOT_FOUND)
      }
    }

    case WebSocketHandshake(ws) => ws match {
      case Path("/api") =>
        ws.authorize(onComplete = Some((event: WebSocketHandshakeEvent) => {
          clients ! WebSocketClientRegister(event.channel)
        }))
    }

    case WebSocketFrame(ws) =>
      system.actorOf(Props[WebSocketClientHandler]) ! ws
  }

  def main(args: Array[String]){
    val server = new WebServer(WebServerConfig(), routes, system)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { server.stop() }
    })
    server.start()
  }

}

class HttpHandler extends Actor with Logger {
  def receive = {
    case request: HttpRequestEvent =>
      request.response.write("Hello from TodoApp (" + new java.util.Date().toString + ")", "text/html; charset=UTF-8")
      context.stop(self)
  }
}


