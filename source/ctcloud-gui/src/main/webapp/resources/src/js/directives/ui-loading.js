angular.module('app')
  .directive('loadingBar', ['$rootScope', function($rootScope) {
     return {
      restrict: 'E',
      templateUrl: ('tpl/loading/loadingPage.html'),
      link: function(scope, el, attrs) {
    	el.addClass('loading hiddenload');
        scope.$on('upload.show.loading', function(event,progressValue) {
          el.removeClass('hiddenload');
        });
        scope.$on('upload.hidden.loading', function(event,progressValue ) {
        	 el.addClass('hiddenload');
        });
      }
     };
  }]);