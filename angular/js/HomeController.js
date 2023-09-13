var HomeController =  ['$scope', '$route', '$SocketService', 
    function($scope, $route, $SocketService) {
    $scope.$route = $route;
    let self = this;
    $scope.click1 = function(){
        
        let name = document.getElementById('input').value;
        // $SocketService.initSocket(name);
        $SocketService.userName = name;
        location.href = '#!/Room';
        var a = 1;
      
    };
}];