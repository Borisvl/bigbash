'use strict';

var filters = angular.module('appFilters', ['ngResource']);

filters.filter('configName', function () {
    return function (input) {
        return input.name + ' (' + input.id + ')';
    };
});

filters.filter('appDomainName', function () {
    return function (input) {
        return input.ad_app_url + ' (' + input.ad_id + ')';
    };
});