/// <reference path="../../../../typings/tsd.d.ts" />

(function () {
    'use strict';
    
    angular
        .module('app.security')
        .factory('security', securityFactory);

    securityFactory.$inject = ['$http','$window'];
    
    function securityFactory($http,$window) {
        var service = {
            authenticate: authenticate,
            isAuthenticated: isAuthenticated,
            getCurrentUser: getCurrentUser,
            clean: clean
        };
        return service;
        
        function authenticate(authVModel, successCallback, failCallback) {}
        
        function isAuthenticated() {
        	var loginUser = $window.sessionStorage.ctUsername;
        	return !(loginUser == null || loginUser =='' || loginUser=='null' )
        }
        
        function  getCurrentUser() {
            
        }
        
        function clean() {
            
        }
    }
})();