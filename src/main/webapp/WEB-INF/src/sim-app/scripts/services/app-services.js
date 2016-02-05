'use strict';

var services = angular.module('appServices', ['ngResource']);

services.factory('UtilService', [ 'Constants',
    function (Constants) {
        return {
            stringToList: function(string){
                if (!string){
                    return [];
                }
                var list = string.split(',');
                for (var i=0; i<list.length; i++){
                    list[i] = list[i].trim();
                }
                return list;
            }
        }
}]);
