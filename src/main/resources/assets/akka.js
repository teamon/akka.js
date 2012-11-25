angular.module("akka", []).factory("AkkaSystem", function($rootScope, $q){
  return function(endpoint){
    var sys = {
      replyTo: {},
      ws: new WebSocket("ws://" + window.location.host + endpoint),
      actorFor: function(path){
        return {
          path: path,
          send: function(msg){ return sys.send(this, msg); },
          ask: function(msg){ return sys.ask(this, msg); }
        }
      }
    };

    sys.ws.onopen = function(){
      console.log("ws open");
    };

    sys.ws.onmessage = function(e){
      var data = JSON.parse(e.data)
      var deffered = sys.replyTo[data.replyTo];

      if(deffered) {
        $rootScope.$apply(function(){
          deffered.resolve(data.msg);
        })
      }
    };

    sys.ws.onclose = function(){
      console.log("ws close");
    };

    sys.send = function(actor, msg){
      sys.ws.send(JSON.stringify({
        path: actor.path,
        msg: msg
      }))
    };

    sys.ask = function(actor, msg){
      var id = sys.uuid();
      deffered = $q.defer();
      sys.replyTo[id] = deffered;

      sys.ws.send(JSON.stringify({
        path: actor.path,
        msg: msg,
        replyTo: id
      }))

      return deffered.promise;
    };

    var CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    sys.uuid = function() {
      var chars = CHARS, uuid = new Array(36), rnd=0, r;
      for (var i = 0; i < 36; i++) {
        if (i==8 || i==13 ||  i==18 || i==23) {
          uuid[i] = '-';
        } else if (i==14) {
          uuid[i] = '4';
        } else {
          if (rnd <= 0x02) rnd = 0x2000000 + (Math.random()*0x1000000)|0;
          r = rnd & 0xf;
          rnd = rnd >> 4;
          uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
        }
      }
      return uuid.join('');
    };

    return sys;
  };
})
