'use strict';

/**
 * Config for the router
 */
angular.module('app')
    .run(
    ['$rootScope', '$state', '$stateParams','security',
      function ($rootScope, $state, $stateParams,security) {
                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
                $rootScope.$on('$stateChangeStart', function (event, toState) {
                    var restrictedState = (toState.name !== 'access.signin');
                    if (restrictedState && !security.isAuthenticated()) {
                        $state.go('access.signin', {}, { reload: true });
                        event.preventDefault();
                    }
      
                });
      }
    ]
    )
    .config(
    ['$stateProvider', '$urlRouterProvider',
      function ($stateProvider, $urlRouterProvider) {

                $urlRouterProvider
                    .otherwise('/app/dashboard-v1');
                $stateProvider
                    .state('app', {
                        abstract: true,
                        url: '/app',
                        templateUrl: 'tpl/app.html'
                    })
                   .state('test', {
                        url: '/test',
                        templateUrl: 'tpl/test/test.html',
                        resolve: {
                            deps: ['$ocLazyLoad',
                        function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['tpl/test/testDemo.js']);
                      }]
                        }
                    })

                    .state('workload', {
                        abstract: true,
                        url: '/workload',
                        templateUrl: 'tpl/workload.html'
                    })
                    .state('workload.list', {
                        url: '/list',
                        templateUrl: 'tpl/jobs/joblist.html',
                        controller: 'JobsCtrl',
                        resolve: {
                            deps: ['$ocLazyLoad',
                            function ($ocLazyLoad) {
                            	return $ocLazyLoad.load('js/controllers/jobs.controller.js');
                                
                            }]
                        }
                    })
                   /*.state('app.hosts', {
                        url: '/hosts',
                        templateUrl: 'tpl/host/hostlist.html'
                    })*/
                   .state('app.dashboard-v1', {
                        url: '/dashboard-v1',
                        templateUrl: 'tpl/jobs/joblist.html',
                        controller: 'JobsCtrl',
                        resolve: {
                            deps: ['$ocLazyLoad',
                            function ($ocLazyLoad) {
                            	return $ocLazyLoad.load('js/controllers/jobs.controller.js');
                                
                            }]
                        }
                        
                    })
 /*
                    .state('app.ui', {
                        url: '/ui',
                        template: '<div ui-view class="fade-in-up"></div>'
                    })

                    .state('app.chart', {
                        url: '/chart',
                        templateUrl: 'tpl/ui_chart.html',
                        resolve: {
                            deps: ['uiLoad',
                        function (uiLoad) {
                                    return uiLoad.load('js/controllers/chart.js');
                      }]
                        }
                    })

                    // pages
                    .state('app.page', {
                        url: '/page',
                        template: '<div ui-view class="fade-in-down"></div>'
                    })
                    */

                    .state('access', {
                        url: '/access',
                        template: '<div ui-view class="fade-in-right-big smooth"></div>'
                    })
                    .state('access.signin', {
                        url: '/signin',
                        templateUrl: 'tpl/login/page_signin.html',
                        resolve: {
                            deps: ['$ocLazyLoad',
                        function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['js/controllers/signin.js']);
                      }]
                        }
                    })
                    .state('access.signup', {
                        url: '/signup',
                        templateUrl: 'tpl/login/page_signin.html',
                        resolve: {
                            deps: ['$ocLazyLoad',
                        function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['js/controllers/signin.js']);
                      }]
                        }
                    })

                    
                    
               /*      .state('app.form', {
                        url: '/form',
                        template: '<div ui-view class="fade-in"></div>',
                        resolve: {
                            deps: ['$ocLazyLoad',
                        function ($ocLazyLoad) {
                                    return $ocLazyLoad.load('js/controllers/form.js');
                      }]
                        }
                    })
                    .state('app.form.fileupload', {
                        url: '/fileupload',
                        templateUrl: 'tpl/form_fileupload.html',
                        resolve: {
                            deps: ['$ocLazyLoad',
                        function ($ocLazyLoad) {
                                    return $ocLazyLoad.load('js/controllers/file-upload.js');
                      }]
                        }
                    })
                    
                .state('layout', {
                        abstract: true,
                        url: '/layout',
                        templateUrl: 'tpl/layout.html'
                    })
                    .state('layout.fullwidth', {
                        url: '/fullwidth',
                        views: {
                            '': {
                                templateUrl: 'tpl/layout_fullwidth.html'
                            },
                            'footer': {
                                templateUrl: 'tpl/layout_footer_fullwidth.html'
                            }
                        },
                        resolve: {
                            deps: ['uiLoad',
                        function (uiLoad) {
                                    return uiLoad.load(['js/controllers/vectormap.js']);
                      }]
                        }
                    })

                    .state('layout.app', {
                        url: '/app',
                        views: {
                            '': {
                                templateUrl: 'tpl/layout_app.html'
                            },
                            'footer': {
                                templateUrl: 'tpl/layout_footer_fullwidth.html'
                            }
                        },
                        resolve: {
                            deps: ['uiLoad',
                        function (uiLoad) {
                                    return uiLoad.load(['js/controllers/tab.js']);
                      }]
                        }
                    })
                    .state('apps', {
                        abstract: true,
                        url: '/apps',
                        templateUrl: 'tpl/layout.html'
                    })

                    .state('app.todo', {
                        url: '/todo',
                        templateUrl: 'tpl/apps_todo.html',
                        resolve: {
                            deps: ['uiLoad',
                        function (uiLoad) {
                                    return uiLoad.load(['js/app/todo/todo.js']);
                      }]
                        }
                    })    */
      }
    ]
    );