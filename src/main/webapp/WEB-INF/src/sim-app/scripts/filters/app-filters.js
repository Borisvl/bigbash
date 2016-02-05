'use strict';

var filters = angular.module('appFilters', ['ngResource']);

filters.filter('configName', function () {
    return function (input) {
        if (input.id) {
            return input.name + ' (' + input.id + ')';
        } else {
            return "Unknown config";
        }
    };
});

filters.filter('appDomainName', function () {
    return function (input) {
        return input.ad_app_url + ' (' + input.ad_id + ')';
    };
});

filters.filter('lovName', function () {
    return function (input, lovMap) {
        var result = [];
        angular.forEach(input, function(value){
            this.push(lovMap[value] + ' (' + value+ ')');
        }, result);
        return result.join(', ');
    };
});

filters.filter('imageUrl', [ 'Constants',
    function (Constants) {
        return function (input) {
            return Constants.IMAGES_HOST + input;
        };
}]);

filters.filter('imageUrlFromSku', [ 'Constants',
    function (Constants) {
        return function (sku) {
            var tmp = sku.replace(/-/g, "");
            var length = tmp.length;
            var url = "";
            for ( var i = 0; i < length; i += 2) {
                if (i < length - 1) {
                    url += "/" + tmp[i] + tmp[i + 1];
                } else {
                    url += "/" + tmp[i];
                }
            }
            url = url.toUpperCase();
            return Constants.IMAGES_HOST + url + "/" + sku
                    + "@1.jpg";
        };
}]);

filters.filter('pretty', function () {
    return function (input) {
        return JSON.stringify(input, undefined, 2);
    };
});