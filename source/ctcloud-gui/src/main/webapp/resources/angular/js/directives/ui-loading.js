angular.module('app')
  .directive('loadingBar', ['$rootScope', function($rootScope) {
     return {
      restrict: 'E',
      template: ('tpl/loading/loadingPage.html'),
      link: function(scope, el, attrs) {
    	el.addClass('loading hiddenload');
        scope.$on('upload.show.loading', function(event,progressValue) {
          el.removeClass('hiddenload');
        });
        scope.$on('upload.hidden.loading', function(event,progressValue ) {
          event.targetScope.$watch('$viewContentLoaded', function(){
            el.addClass('hiddenload');
          })
        });
      }
     };
  }]);