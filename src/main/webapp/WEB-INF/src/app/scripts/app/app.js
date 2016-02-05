/**
 * Created by htorbasinovic on 15.04.14.
 */

var configApp = angular.module('configApp', [
    'ngRoute',
    'appControllers',
    'appFilters',
    'appConstants',
    'checklistDirective',
    'localytics.directives',
    'lovServices',
    'ctxControllers',
    'ctxServices',
]);

configApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/ctx/', {
                controller: 'CtxCtrl',
                templateUrl: '/ws/admin/src/app/views/ctx.html'
            }).otherwise({redirectTo: '/ctx/'});
    }]);
