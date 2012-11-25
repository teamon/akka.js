function Main($scope, Akka){
  var EchoActor = Akka.actorFor("/echo")
  var PingActor = Akka.actorFor("/ping")

  $scope.resA = $scope.resB = "";
  $scope.a = $scope.b = 1;

  $scope.echoA = function(){
    $scope.resA = EchoActor.ask("hello A " + $scope.a);
    $scope.a++;
  }

  $scope.echoB = function(){
    $scope.resB = EchoActor.ask("hello B " + $scope.b);
    $scope.b++;
  }

  $scope.ping = function(){
    PingActor.ask("ping").then(function(res){
      alert("Response: " + res)
    })
  }
}


TodoApp = angular.module("TodoApp", ['akka'])
  .factory("Akka", function(AkkaSystem){
    return AkkaSystem("/api");
  })
