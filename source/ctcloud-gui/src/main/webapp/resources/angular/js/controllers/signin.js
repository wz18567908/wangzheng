'use strict';

/* Controllers */
  // signin controller
app.controller('SigninFormController', ['$scope','$rootScope', '$http', '$state','$window', function($scope,$rootScope, $http, $state,$window) {
    $scope.user = {};
    $scope.authError = null;
    $scope.login = function() {
      $scope.authError = null;
      var curUser = $scope.user.name;
      $window.sessionStorage.ctUsername = curUser;
      $rootScope.ctUserName = curUser;

      $state.go('app.dashboard-v1', {}, { reload: true });
    };
  }])
;