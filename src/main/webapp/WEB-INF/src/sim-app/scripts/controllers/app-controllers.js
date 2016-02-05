'use strict';

var appControllers = angular.module('appControllers', ['ui.bootstrap']);

appControllers.controller('AppCtrl',
    ['$scope', '$timeout', '$location', function ($scope, $timeout, $location) {
        $scope.navClass = function (page) {
            var path = $location.path();
            var endIndex = path.indexOf('/', 1);
            var currentRoute = path.substring(1, endIndex);
            return currentRoute == page ? 'active' : '';
        };

        $scope.clearAlert = function () {
            $scope.alert = null;
        };

        $scope.showMsg = function (type, msg) {
            $timeout.cancel($scope.timeoutPromise);
            $scope.alert = {};
            $scope.alert.type = type;
            $scope.alert.msg = msg;
            $scope.timeoutPromise = $timeout($scope.clearAlert, 7000);
        };

        $scope.checkmark = function (input) {
            return input ? 'fa-check' : 'fa-times';
        }
    }]);

appControllers.controller('LovCtrl',
    ['$scope', 'AppDomains', 'Genders', 'AgeGroups', 'Tags', 'RecoTypes', 'RecoContexts',

        function ($scope, AppDomains, Genders, AgeGroups, Tags, RecoTypes, RecoContexts) {
            $scope.appDomains = AppDomains.query();
            $scope.appDomainsMap = {};
            $scope.appDomains.$promise.then(function () {
                angular.forEach($scope.appDomains, function(appDomain){
                    this[appDomain.ad_id] = appDomain.ad_app_url;
                }, $scope.appDomainsMap);
            });

            $scope.genders = Genders.query();
            $scope.gendersMap = {};
            $scope.genders.$promise.then(function () {
                            angular.forEach($scope.genders, function(item){
                                this[item.value] = item.name;
                            }, $scope.gendersMap);
                        });

            $scope.ageGroups = AgeGroups.query();
            $scope.ageGroupsMap = {};
            $scope.ageGroups.$promise.then(function () {
                            angular.forEach($scope.ageGroups, function(item){
                                this[item.value] = item.name;
                            }, $scope.ageGroupsMap);
                        });

            $scope.tags = Tags.query();
            $scope.tagsMap = {};
            $scope.tags.$promise.then(function () {
                            angular.forEach($scope.tags, function(item){
                                this[item.value] = item.name;
                            }, $scope.tagsMap);
                        });

            $scope.recoContexts = RecoContexts.query();
            $scope.recoTypes = RecoTypes.query();
        }]);
