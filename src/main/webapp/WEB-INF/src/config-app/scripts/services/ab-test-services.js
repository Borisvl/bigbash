'use strict';

var services = angular.module('abTestServices', ['ngResource']);

services.factory('AbTest', ['$resource',
    function ($resource) {
        return $resource('/ws/config-app/ab-test/:abId', {}, {});
    }]);

services.factory('AllAbTests', [ '$resource',
    function ($resource) {
        return $resource('/ws/config-app/ab-test/find-all/:archived', {archived:false}, {});
    }]);

