## Screw HTTP, use Akka! ##

(Screencast)[http://s.teamon.eu/akka%20-%20Broadband.m4v]

## Run the example ##

```bash
$ sbt run
```

## WTF? ##

Make you JS feel like akka ;)

### Akka ###

```scala
class EchoActor extends Actor {
  def receive = {
    case msg => sender ! ("echo: " + msg)
  }
}
// ...
val echo = system.actorOf(Props[EchoActor], name = "echo")
```

### JavaScript ###

```js
var system = AkkaSystemP("/api")
var EchoActor = system.actorFor("/echo")
EchoActor.send("hello")

var promise = EchoActor.ask("hello") // will be filled when akka actor replies
```

### How? ###

```
Angular.js -> Websocket -> Socko -> Akka and back,
```
  
