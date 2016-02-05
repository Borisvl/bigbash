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

services.factory('RecoEngines', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/reco-engines', {}, {});
    }]);

services.factory('FilterKeys', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/filter-keys', {}, {});
    }]);

services.factory('ScoringFunctionKeys', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/scoring-keys', {}, {});
    }]);

services.factory('Layers', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/layers', {}, {});
    }]);

services.factory('LayersName', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/layers-name', {}, {});
    }]);

// https://orders.zalando.net/appdomains/json
services.factory('AppDomains', ['$resource',
    function ($resource) {
        return $resource('/ws/admin/src/config-app/static/app-domains.json', {}, {});
    }]);

services.factory('AttributeSets', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/attribute-sets', {}, {});
    }]);

services.factory('Seasons', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/season-codes', {}, {});
    }]);

services.factory('Genders', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/genders', {}, {});
    }]);

services.factory('AgeGroups', ['$resource',
    function ($resource) {
        return $resource('/ws/lov/age-groups', {}, {});
    }]);


