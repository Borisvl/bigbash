'use strict';

var services = angular.module('cascadeServices', ['ngResource']);

services.factory('Cascade', ['$resource',
    function ($resource) {
        return $resource('/ws/config-app/cascade/:cascadeId', {}, {});
    }]);

services.factory('AllCascades', [ '$resource',
    function ($resource) {
        return $resource('/ws/config-app/cascade/find-all/:archived', {archived:false}, {});
    }]);

