'use strict';

var burpControllers = angular.module('burpControllers', []);

burpControllers.controller('BurpListCtrl', ['$scope', '$location', 'AllBurps',
    function ($scope, $location, AllBurps) {
        $scope.archivedBurps = false;

        $scope.refreshTable = function () {
            $scope.burps = AllBurps.query({archived: $scope.archivedBurps});
        };

        $scope.newBurp = function () {
            $location.path('/burp/new');
        };

        $scope.refreshTable();
    }]);

burpControllers.controller('BurpViewCtrl', ['$scope', '$http', '$location', '$routeParams', 'FiltersUtil', 'Burp', 'FilterKey', 'ScoringFunctionKey',
    function ($scope, $http, $location, $routeParams, FiltersUtil, Burp, FilterKey, ScoringFunctionKey) {
        $scope.burp = Burp.get({burpId: $routeParams.burpId});
        $scope.FilterKey = FilterKey;
        $scope.ScoringFunctionKey = ScoringFunctionKey;

        $scope.burp.$promise.then(function () {
            $scope.selectedFilters = FiltersUtil.getSelectedFilters($scope.burp);
            $scope.filters = FiltersUtil.filtersData($scope.burp);
            $scope.selectedScoringFunction = FiltersUtil.getSelectedScoringFunction($scope.burp);
            $scope.scoringFunctions = FiltersUtil.scoringFunctionData($scope.burp);
        });

        $scope.supportedFilters = function (input) {
            return FiltersUtil.supportedFilters(input);
        };

        $scope.approve = function () {
            $http.post('/ws/config-app/burp/approve', $scope.burp).
                success(function (data) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.burp = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function () {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };

        $scope.archive = function (archived) {
            $http.post('/ws/config-app/burp/archive/' + archived, $scope.burp).
                success(function (data) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.burp = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function () {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };

        $scope.edit = function () {
            $location.path('/burp/edit/' + $scope.burp.id);
        };

        $scope.cancel = function () {
            $location.path('/burp/');
        }
    }]);

burpControllers.controller('BurpEditCtrl', ['$scope', '$location', '$routeParams', 'Burp', 'FiltersUtil',
    function ($scope, $location, $routeParams, Burp, FiltersUtil) {

        $scope.burp = Burp.get({burpId: $routeParams.burpId}, function () {
            if (!$scope.burp.id) {
                $location.path('/burp/new');
            }
        });
        $scope.selectedFilters = [];
        $scope.filters = FiltersUtil.emptyFilter();
        $scope.scoringFunctions = FiltersUtil.emptyScoringFunction();

        $scope.burp.$promise.then(function () {
            $scope.selectedFilters = FiltersUtil.getSelectedFilters($scope.burp);
            $scope.filters = FiltersUtil.filtersData($scope.burp);
            $scope.selectedScoringFunction = FiltersUtil.getSelectedScoringFunction($scope.burp);
            $scope.scoringFunctions = FiltersUtil.scoringFunctionData($scope.burp);
        });

        $scope.title = "Edit business rules";

        $scope.cancel = function () {
            $location.path('/burp/view/' + $routeParams.burpId);
        }
    }]);

burpControllers.controller('BurpNewCtrl', ['$scope', '$location', 'Burp', 'FiltersUtil',
    function ($scope, $location, Burp, FiltersUtil) {
        $scope.burp = new Burp();

        $scope.selectedFilters = [];
        $scope.filters = FiltersUtil.emptyFilter();
        $scope.scoringFunctions = FiltersUtil.emptyScoringFunction();

        $scope.title = "New business rule";

        $scope.cancel = function () {
            $location.path('/burp/');
        }
    }]);

burpControllers.controller('BurpFormCtrl', ['$scope', '$location', 'ScoringFunctionKey', 'FiltersUtil', 'FilterKey',
    function ($scope, $location, ScoringFunctionKey, FiltersUtil, FilterKey) {
        $scope.FilterKey = FilterKey;
        $scope.ScoringFunctionKey = ScoringFunctionKey;

        $scope.supportedFilters = function (input) {
            return FiltersUtil.supportedFilters(input);
        };

        $scope.supportedScoringFunctions = function (input) {
            return FiltersUtil.supportedScoringFunctions(input);
        };

        $scope.save = function () {
            var burpCopy = angular.copy($scope.burp);
            $scope.burp.filters = FiltersUtil.toFilterConfig($scope.selectedFilters, $scope.filters);
            $scope.burp.scoringFunction = FiltersUtil.toScoringFunction($scope.selectedScoringFunction, $scope.scoringFunctions);
            $scope.burp.$save(function (response) {
                if (response.success) {
                    $scope.showMsg('success', response.msg);
                    $location.path('/burp/view/' + response.object);
                } else {
                    $scope.showMsg('warning', response.msg);
                    $scope.burp = burpCopy;
                }
            });
        };
    }]);

cascadeControllers.controller('LayerFilterCtrl', ['$scope',
    function ($scope) {
        $scope.addLayer = function () {
            var layers = $scope.filters.layerFilter;
            layers[layers.length] = {};
        };
        $scope.removeLayer = function (index) {
            $scope.filters.layerFilter.splice(index, 1);
        };
    }]);