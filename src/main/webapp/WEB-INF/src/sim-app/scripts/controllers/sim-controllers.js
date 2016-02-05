'use strict';

var newsletterControllers = angular.module('simControllers', []);

newsletterControllers.controller('SimNewSearchCtrl', ['$scope', 'SimUrlService',
    function ($scope, SimUrlService) {
        $scope.queryParams = SimUrlService.defaultQueryParams();
    }]);

newsletterControllers.controller('SimSearchCtrl', ['$q', '$scope', '$http', '$routeParams', 'SimUrlService', 'UtilService',
    function ($q, $scope, $http, $routeParams, SimUrlService, UtilService) {
        $scope.queryParams = SimUrlService.urlToParams($routeParams);
        $scope.skusList = UtilService.stringToList($scope.queryParams.skus);
        $scope.queryParams.skus = $scope.skusList.join();

        $scope.recosMap = []
        $scope.recoUrl = SimUrlService.recoUrl($scope.queryParams);
        $http.get($scope.recoUrl).
            success(function (data) {
                if (data.success) {
                    $scope.recosMap = data.map;
                } else {
                    $scope.showMsg('warning', data.msg);
                }
            }).
            error(function (data, status) {
                $scope.showMsg('warning', 'Server error: ' + status);
            });

        var cascadeMap = {};
        $scope.cascadeName = function(cascadeId){
            if (!cascadeMap[cascadeId]){
                cascadeMap[cascadeId] = {};
                cascadeMap[cascadeId].id = "?";
                cascadeMap[cascadeId].name = "???";
                $http.get("/ws/config-app/cascade/" + cascadeId).
                    success(function (data) {
                        cascadeMap[cascadeId] = data;
                    });
            }
            return cascadeMap[cascadeId];
        }

        var abTestMap = {};
        $scope.abTestName = function(abId){
            if (!abTestMap[abId]){
                abTestMap[abId] = {};
                abTestMap[abId].id = "?";
                abTestMap[abId].name = "???";
                $http.get("/ws/config-app/ab-test/" + abId).
                    success(function (data) {
                        abTestMap[abId] = data;
                    });
            }
            return abTestMap[abId];
        }
    }]);


newsletterControllers.controller('SimFormCtrl', ['$scope', '$location', 'CascadeConfigs', 'SimUrlService',
    function ($scope, $location, CascadeConfigs, SimUrlService) {
        $scope.cascades = CascadeConfigs.query();

        $scope.getRecos = function () {
            var path = '/simulation';
            path += '/' + $scope.queryParams.appDomain;
            path += '/' + $scope.queryParams.recoContext;
            path += '/' + $scope.queryParams.nrRecos;
            path += '/' + $scope.queryParams.skus;
            $location.search('enrich', $scope.queryParams.enrich);
            if ($scope.queryParams.forceCascade) {
                $location.search('cascadeId', $scope.queryParams.cascadeId);
                $location.search('recoTypes', null);
            } else {
                $location.search('recoTypes', $scope.queryParams.recoTypes.join());
                $location.search('cascadeId', null);
            }
            $location.path(path);
        }

        $scope.getSkuRecos = function (sku) {
            $scope.queryParams.skus = sku;
            $scope.getRecos();
        }
    }]);
