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
    'abTestControllers',
    'abTestServices',
    'burpControllers',
    'burpServices',
    'cascadeControllers',
    'cascadeServices',
    'newsletterControllers',
    'newsletterServices'
]);

configApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/ab-test/', {
                controller: 'AbTestListCtrl',
                templateUrl: '/ws/admin/src/config-app/views/ab-test/list.html'
            }).when('/ab-test/edit/:abId', {
                controller: 'AbTestEditCtrl',
                templateUrl: '/ws/admin/src/config-app/views/ab-test/form.html'
            }).when('/ab-test/view/:abId', {
                controller: 'AbTestViewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/ab-test/view.html'
            }).when('/ab-test/new', {
                controller: 'AbTestNewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/ab-test/form.html'
            }).when('/cascade/', {
                controller: 'CascadeListCtrl',
                templateUrl: '/ws/admin/src/config-app/views/cascade/list.html'
            }).when('/cascade/edit/:cascadeId', {
                controller: 'CascadeEditCtrl',
                templateUrl: '/ws/admin/src/config-app/views/cascade/form.html'
            }).when('/cascade/view/:cascadeId', {
                controller: 'CascadeViewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/cascade/view.html'
            }).when('/cascade/new', {
                controller: 'CascadeNewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/cascade/form.html'
            }).when('/burp/', {
                controller: 'BurpListCtrl',
                templateUrl: '/ws/admin/src/config-app/views/burp/list.html'
            }).when('/burp/edit/:burpId', {
                controller: 'BurpEditCtrl',
                templateUrl: '/ws/admin/src/config-app/views/burp/form.html'
            }).when('/burp/view/:burpId', {
                controller: 'BurpViewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/burp/view.html'
            }).when('/burp/new', {
                controller: 'BurpNewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/burp/form.html'
            }).when('/newsletter/', {
                controller: 'NewsletterListCtrl',
                templateUrl: '/ws/admin/src/config-app/views/newsletter/list.html'
            }).when('/newsletter/edit/:newsId', {
                controller: 'NewsletterEditCtrl',
                templateUrl: '/ws/admin/src/config-app/views/newsletter/form.html'
            }).when('/newsletter/view/:newsId', {
                controller: 'NewsletterViewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/newsletter/view.html'
            }).when('/newsletter/new', {
                controller: 'NewsletterNewCtrl',
                templateUrl: '/ws/admin/src/config-app/views/newsletter/form.html'
            }).otherwise({redirectTo: '/ab-test/'});
    }]);
