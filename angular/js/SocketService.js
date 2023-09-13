var SocketService =  function(){
    this.socket = null;
    this.userData = {
        senderIdx: 0,
        receiverIdx: 0
    };
    this.userName = '';
    var self = this;
    self.fnc1 = null;
    self.fnc2 = null;
    self.fnc3 = null;
    self.fncApply = null;
    self.data = [];
    self.init = false;
  
    this.initSocket = function(fnc1, fnc2, fnc3, fncApply){
        if(self.init)
            return;
        this.socket  = new WebSocket('ws://localhost:8080' + '/ws?name='+this.userName);
        this.socket.onopen = socket_onopen;
        this.socket.onmessage = socket_onmessage;
        this.socket.onclose = socket_onclose;
        self.fnc1 = fnc1;
        self.fnc2 = fnc2;
        self.fnc3 = fnc3;
        self.fncApply = fncApply;
        self.init = true;
    }
    // this.setScope = function(scope){
    //     self.scope = scope;
    // }

    this.selectName = function(index){
        var sendMessage = {
            kind: 2,
            data: 'hello world',
            senderIdx: this.userData.senderIdx,
            receiverIdx: index
        };
        this.socket.send(JSON.stringify(sendMessage));
    };
    var socket_onopen = function (){
        console.log('WebSocket connection is open for business, bienvenidos!');
    }
    var socket_onmessage = function (message){
        let object = JSON.parse(message.data);  
        let data = null;
        if(typeof(object) =="object")
            data = JSON.parse(object.data);
        let fnc = null;
        if(object.kind == 0){  
            fnc = self.fnc1;
        }
        else if(object.kind == 1){
            fnc = self.fnc2;
        }
        else if(object.kind == 2){
            console.log('receive message from ' + self.scope.data[data.senderIdx].name);
            console.log('message is ' + data.data);

        }
        else if(object.kind == 3){
            
            fnc = self.fnc3;
        }
        if(fnc)
        {
            if(object.kind == 3)
                data = data.leftIdx;
            self.fncApply(fnc, data);
        }
            
        // fnc(data);
        var k = 1;
    }
    var socket_onclose = function (){
        console.log('WebSocket connection closed');
    }

};

SocketService.a