/**
 * Created by htorbasinovic on 15.04.14.
 */

var configApp = angular.module('simApp', [
    'ngRoute',
    'appControllers',
    'simControllers',
    'simServices',
    'appFilters',
    'appConstants',
    'appDirectives',
    'appServices',
    'checklistDirective',
    'localytics.directives',
    'lovServices',
]);

configApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/simulation/', {
                controller: 'SimNewSearchCtrl',
                templateUrl: '/ws/admin/src/sim-app/views/simulation.html'
            }).when('/simulation/:appDomain/:recoContext/:nrRecos/:skus', {
                controller: 'SimSearchCtrl',
                templateUrl: '/ws/admin/src/sim-app/views/simulation.html'
            }).otherwise({redirectTo: '/simulation/'});
    }]);
