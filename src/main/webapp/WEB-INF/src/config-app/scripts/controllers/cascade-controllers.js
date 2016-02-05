'use strict';

var cascadeControllers = angular.module('cascadeControllers', []);

cascadeControllers.controller('CascadeListCtrl', ['$scope', '$location', 'AllCascades',
    function ($scope, $location, AllCascades) {
        $scope.archivedCascades = false;

        $scope.refreshTable = function () {
            $scope.cascades = AllCascades.query({archived: $scope.archivedCascades});
        }

        $scope.newCascade = function () {
            $location.path('/cascade/new');
        }

        $scope.refreshTable();
    }]);

cascadeControllers.controller('CascadeViewCtrl', ['$scope', '$http', '$location', '$routeParams', 'Cascade',
    function ($scope, $http, $location, $routeParams, Cascade) {
        $scope.cascade = Cascade.get({cascadeId: $routeParams.cascadeId});

        $scope.burpName = [];

        $scope.cascade.$promise.then(function () {
            for (var i = 0; i < $scope.cascade.pipelines.length; i++) {
                $http.get("/ws/config-app/burp/" + $scope.cascade.pipelines[i].burpId).success(function (data) {
                    $scope.burpName[data.id] = data;
                });
            }
        });

        $scope.approve = function () {
            $http.post('/ws/config-app/cascade/approve', $scope.cascade).
                success(function (data, status, headers, config) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.cascade = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status, headers, config) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        }

        $scope.archive = function (archived) {
            $http.post('/ws/config-app/cascade/archive/' + archived, $scope.cascade).
                success(function (data, status, headers, config) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.cascade = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status, headers, config) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        }

        $scope.edit = function () {
            $location.path('/cascade/edit/' + $scope.cascade.id);
        };

        $scope.cancel = function () {
            $location.path('/cascade/');
        }
    }]);

cascadeControllers.controller('CascadeEditCtrl', ['$scope', '$location', '$routeParams', 'Cascade',
    function ($scope, $location, $routeParams, Cascade) {

        $scope.cascade = Cascade.get({cascadeId: $routeParams.cascadeId}, function () {
            if (!$scope.cascade.id) {
                $location.path('/cascade/new');
            }
        });

        $scope.title = "Edit cascade";

        $scope.cancel = function () {
            $location.path('/cascade/view/' + $routeParams.cascadeId);
        }
    }]);

cascadeControllers.controller('CascadeNewCtrl', ['$scope', '$location', '$routeParams', 'Cascade',
    function ($scope, $location, $routeParams, Cascade) {
        $scope.cascade = new Cascade({
            pipelines: [
                {}
            ]
        });

        $scope.title = "New cascade";

        $scope.cancel = function () {
            $location.path('/cascade/');
        }
    }]);

cascadeControllers.controller('CascadePipelinesCtrl', ['$scope',
    function ($scope) {
        $scope.addPipeline = function () {
            var pipelines = $scope.cascade.pipelines;
            pipelines[pipelines.length] = {};
        };
        $scope.removePipeline = function (index) {
            $scope.cascade.pipelines.splice(index, 1);
        };
    }]);

cascadeControllers.controller('CascadeFormCtrl', ['$scope', '$location', 'AllBurps',
    function ($scope, $location, AllBurps) {
        $scope.burps = AllBurps.query();

        $scope.save = function () {
            var cascadeCopy = angular.copy($scope.cascade);
            $scope.cascade.$save(function (response) {
                if (response.success) {
                    $scope.showMsg('success', response.msg);
                    $location.path('/cascade/view/' + response.object);
                } else {
                    $scope.showMsg('warning', response.msg);
                    $scope.cascade = cascadeCopy;
                }
            });
        };
    }]);


