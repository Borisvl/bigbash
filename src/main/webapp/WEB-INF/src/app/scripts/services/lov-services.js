'use strict';

var services = angular.module('lovServices', ['ngResource']);

services.factory('RecoContexts', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/reco-contexts', {}, {});
    }]);

services.factory('RecoTypes', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/reco-types', {}, {});
    }]);


