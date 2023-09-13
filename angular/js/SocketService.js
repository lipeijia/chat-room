var SocketService =  function(){
    this.socket = null;
    this.userData = {
        senderIdx: 0,
        receiverIdx: 0
    };
    this.userName = '';
    var self = this;
  
    self.fncApply = null;
    self.data = [];
    self.init = false;
    //fnc0, fnc1, fnc3, fncApply
    this.initSocket = function(...args){
        if(self.init)
            return;
        args['0'].forEach(function(v, idx){
            self[v.name] = v;
        });
        this.socket  = new WebSocket('ws://localhost:8080' + '/ws?name='+this.userName);
        this.socket.onopen = socket_onopen;
        this.socket.onmessage = socket_onmessage;
        this.socket.onclose = socket_onclose;
        // self.fnc0 = fnc0;
        // self.fnc1 = fnc1;
        // self.fnc3 = fnc3;
        
        // self.fncApply = fncApply;
        self.init = true;
    }
    // this.setScope = function(scope){
    //     self.scope = scope;
    // }

    this.send = function(json)
    {
        this.socket.send(json);
    };
    var socket_onopen = function (){
        console.log('WebSocket connection is open for business, bienvenidos!');
    };
    var socket_onmessage = function (message){
        let object = JSON.parse(message.data);  
        let data = null;
        if(typeof(object) =="object")
            data = JSON.parse(object.data);
        let fnc = null;
        if(object.kind == 0){  
            fnc = self.fnc0;
        }
        else if(object.kind == 1){
            fnc = self.fnc1;
        }
        else if(object.kind == 2){
            fnc = self.fnc2;
            
            data = {senderIdx: data.senderIdx, message: data.data};
        }
        else if(object.kind == 3){
            
            fnc = self.fnc3;
            data = data.leftIdx;
        }
        if(fnc)
        {
            self.fncApply(object.kind, fnc, data);
        }
            
        // fnc(data);
        var k = 1;
    }
    var socket_onclose = function (){
        console.log('WebSocket connection closed');
    }

};

SocketService.a