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
    ['$scope', 'RecoContexts', 'RecoTypes', 'RecoEngines', 'FilterKeys', 'ScoringFunctionKeys', 'Layers', 'AppDomains', 'AttributeSets', 'Seasons', 'Genders', 'AgeGroups',

    function ($scope, RecoContexts, RecoTypes, RecoEngines, FilterKeys, ScoringFunctionKeys, Layers, AppDomains, AttributeSets, Seasons, Genders, AgeGroups) {
        $scope.recoContexts = RecoContexts.query();
        $scope.recoTypes = RecoTypes.query();
        $scope.recoEngines = RecoEngines.query();
        $scope.filterKeys = FilterKeys.query();
        $scope.scoringFunctionKeys = ScoringFunctionKeys.query();
        $scope.layers = Layers.query();
        $scope.appDomains = AppDomains.query();
        $scope.attributeSets = AttributeSets.query();
        $scope.seasons = Seasons.query();
        $scope.genders = Genders.query();
        $scope.ageGroups = AgeGroups.query();
    }]);