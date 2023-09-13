var RoomController =  ['$scope', '$route', '$SocketService',
 function($scope, $route, $SocketService) {
    $scope.$route = $route;
    var self = this;  
   
    // if(typeof($scope.data) == 'undefined')
    //     $scope.data = [{name: 'test'}];
    self.userData = {
        senderIdx: 0,
        receiverIdx: 0
    };
    var fnc1 = (data) => {
        // Array.prototype.push.apply($scope.data, data);  
        $scope.data = data;
        self.userData.senderIdx = $scope.data.length-1;   
    } ;
    var fnc2 =  (data) => $scope.data.push(data);

    var fnc3 = (leftIdx) => {
        $scope.data.splice(leftIdx, 1);
        if(data.leftIdx < self.userData.senderIdx)
            self.userData.senderIdx -= 1;
    };
    var fncApply = (fnc, data) =>{
        // fnc(data);
        // $scope.$apply();
        $scope.$apply(() => fnc(data));
    };
    $SocketService.initSocket(fnc1, fnc2, fnc3, fncApply);
    // $scope.data = [];

    // $SocketService.setScope($scope);
    

$scope.selectName = function(index){
    
    $scope.data.push({name: 'lee'});
    // $scope.$applyAsync(() => $scope.data.push({name: 'lee'}));
    return;
    var sendMessage = {
        kind: 2,
        data: 'hello world',
        senderIdx: self.userData.senderIdx,
        receiverIdx: index
    };
    self.socket.send(JSON.stringify(sendMessage));
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
            Array.prototype.push.apply($scope.data, data);  
            self.userData.senderIdx = $scope.data.length-1;   
        } 
    }
    else if(object.kind == 1){
        fnc = () => $scope.data.push(data);
    }
    else if(object.kind == 2){
        console.log('receive message from ' + $scope.data[data.senderIdx].name);
        console.log('message is ' + data.data);

    }
    else if(object.kind == 3){
        fnc = () => {
            $scope.data.splice(data.leftIdx, 1);
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