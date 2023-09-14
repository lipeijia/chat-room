var RoomController =  ['$scope', '$route', '$SocketService',
 function($scope, $route, $SocketService) {
    $scope.$route = $route;
    var self = this;  
    $scope.messages = []
    // if(typeof($scope.users) == 'undefined')
    //     $scope.users = [{name: 'test'}];
    self.userData = {
        senderIdx: 0,
        receiverIdx: 0
    };
    var fnc0 = (data) => {
        // Array.prototype.push.apply($scope.users, data);  
        $scope.users = data;
        self.userData.senderIdx = $scope.users.length-1;   
    } ;
    var fnc1 =  (data) => $scope.users.push(data);

    var fnc2 =  (data) => {
        $scope.messages.push(data.message);
        let name = $scope.users[data.senderIdx].name;
        console.log('receive message from ' + name);
        console.log('message is ' + data.message);
     
    };

    var fnc3 = (leftIdx) => {
        $scope.users.splice(leftIdx, 1);
        if(data.leftIdx < self.userData.senderIdx)
            self.userData.senderIdx -= 1;
    };
    var fncApply = (kind, fnc, data) =>{
        // fnc(data);
        // $scope.$apply();
        var tempFnc = null;
        if(kind == 0 || kind == 1 || kind == 3){
            tempFnc = () => fnc(data);
        }
        else{
            tempFnc = () => fnc(data);
        }
        $scope.$apply(() => fnc(data));
    };
    $SocketService.initSocket([fnc0, fnc1, fnc2, fnc3, fncApply]);
    // $scope.users = [];

    // $SocketService.setScope($scope);
    

$scope.selectName = function(index){
    var sendMessage = {
        kind: 2,
        data: this.message,
        senderIdx: self.userData.senderIdx,
        receiverIdx: index
    };
    $SocketService.send(JSON.stringify(sendMessage));
};
self.socket_onopen = function (){
    console.log('WebSocket connection is open for business, bienvenidos!');
}
self.socket_onmessage = function (message){
    let object = JSON.parse(message.data);  
    let data = null;
    if(typeof(object) =="object")
        data = JSON.parse(object.data);
    let fnc = null;
    if(object.kind == 0){  
        fnc = () => {
            Array.prototype.push.apply($scope.users, data);  
            self.userData.senderIdx = $scope.users.length-1;   
        } 
    }
    else if(object.kind == 1){
        fnc = () => $scope.users.push(data);
    }
    else if(object.kind == 2){
        console.log('receive message from ' + $scope.users[data.senderIdx].name);
        console.log('message is ' + data.data);

    }
    else if(object.kind == 3){
        fnc = () => {
            $scope.users.splice(data.leftIdx, 1);
            if(data.leftIdx < self.userData.senderIdx)
                self.userData.senderIdx -= 1;
        }
    }
    if(fnc)
        $scope.$apply(fnc);
    var k = 1;
}
self.socket_onclose = function (){
    console.log('WebSocket connection closed');
}

}]