var HomeController =  ['$scope', '$route', '$SocketService', 
    function($scope, $route, $SocketService) {
    $scope.$route = $route;
    var self = this;
    $scope.click1 = function(){
        
        $SocketService.userName = this.loginName;
        location.href = '#!/Room';
        var a = 1;
      
    };
}];