'use strict';

var services = angular.module('simServices', ['ngResource']);

services.factory('CascadeConfigs', [ '$resource',
    function ($resource) {
        return $resource('/ws/config-app/cascade/find-all/:archived', {archived:false}, {});
    }]);

services.factory('SimUrlService', [ 'Constants',
    function (Constants) {
        return {
            defaultQueryParams: function(){
                var queryParams = {};
                queryParams.appDomain = 1;
                queryParams.recoContext = 'PDS';
                queryParams.nrRecos = 12;
                queryParams.enrich = true;
                queryParams.recoTypes = ['COLL_ITEM_BASED'];
                return queryParams;
            },

            urlToParams: function(route){
                var queryParams = {};
                queryParams.appDomain = parseInt(route.appDomain);
                queryParams.recoContext = route.recoContext;
                queryParams.nrRecos = route.nrRecos;
                queryParams.enrich = route.enrich;
                queryParams.skus = route.skus;
                if (route.cascadeId){
                    queryParams.cascadeId = parseInt(route.cascadeId);
                    queryParams.forceCascade = true;
                } else {
                    if (route.recoTypes){
                        queryParams.recoTypes = route.recoTypes.split(',');
                    }
                    queryParams.forceCascade = false;
                }
                return queryParams;
            },

            recoUrl: function(queryParams){
                var url = '/ws/recommendations/itemBased';
                url += '/' + queryParams.appDomain;
                url += '/' + queryParams.recoContext;
                url += '/' + queryParams.skus;
                url += '/' + queryParams.nrRecos;
                url += '/sim-app-session';
                url += '/sim-app-permCookieId';
                if (queryParams.forceCascade){
                    url+='?shopABTestVersion=reco.business.module:' + queryParams.cascadeId;
                } else {
                    url+='?recoTypes=[' + queryParams.recoTypes + ']';
                }
                url += '&fields=' + Constants.SIM_FIELDS;
                url += '&useContentService=' + queryParams.enrich ? 'true' : 'false';
                return url;
            }

        }
    }]);