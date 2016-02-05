'use strict';

var services = angular.module('newsletterServices', ['ngResource']);

services.factory('Newsletter', ['$resource',
    function ($resource) {
        return $resource('/ws/config-app/newsletter/:newsId', {}, {});
    }]);

services.factory('AllNewsletters', [ '$resource',
    function ($resource) {
        return $resource('/ws/config-app/newsletter/find-all/:archived', {archived:false}, {});
    }]);

