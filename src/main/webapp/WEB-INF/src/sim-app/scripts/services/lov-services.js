'use strict';

var services = angular.module('lovServices', ['ngResource']);

// https://orders.zalando.net/appdomains/json
services.factory('AppDomains', ['$resource',
    function ($resource) {
        return $resource('/ws/admin/src/config-app/static/app-domains.json', {}, {});
    }]);

services.factory('RecoContexts', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/reco-contexts', {}, {});
    }]);

services.factory('RecoTypes', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/reco-types', {}, {});
    }]);

services.factory('Genders', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/genders', {}, {});
    }]);

services.factory('AgeGroups', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/age-groups', {}, {});
    }]);

services.factory('Tags', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/tags', {}, {});
    }]);


