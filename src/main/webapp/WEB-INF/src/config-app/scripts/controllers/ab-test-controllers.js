'use strict';

var abTestControllers = angular.module('abTestControllers', []);

abTestControllers.controller('AbTestListCtrl', ['$scope', '$location', 'AllAbTests',
    function ($scope, $location, AllAbTests) {
        $scope.archivedAbTests = false;
        $scope.activeAbTests = false;
        $scope.defaultAbTests = false;
        $scope.refreshTable = function () {
            $scope.abTests = AllAbTests.query({archived: $scope.archivedAbTests});
        };

        $scope.newAbTest = function () {
            $location.path('/ab-test/new');
        };

        $scope.refreshTable();
    }]);

abTestControllers.controller('AbTestViewCtrl', ['$scope', '$http', '$location', '$routeParams', 'AbTest', 'Cascade',
    function ($scope, $http, $location, $routeParams, AbTest, Cascade) {
        $scope.abTest = AbTest.get({abId: $routeParams.abId});

        $scope.cascadeName = [];

        $scope.abTest.$promise.then(function () {
            for (var i = 0; i < $scope.abTest.distribution.length; i++) {
                var cascadeId = $scope.abTest.distribution[i].id;
                $scope.cascadeName[cascadeId] = Cascade.get({cascadeId:cascadeId});
            }
        });

        $scope.approve = function () {
            $http.post('/ws/config-app/ab-test/approve', $scope.abTest).
                success(function (data) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.abTest = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };

        $scope.archive = function (archived) {
            $http.post('/ws/config-app/ab-test/archive/' + archived, $scope.abTest).
                success(function (data) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.abTest = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };

        $scope.activate = function (active) {
            $http.post('/ws/config-app/ab-test/activate/' + active, $scope.abTest).
                success(function (data) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.abTest = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };

        $scope.makeDefault = function (dflt) {
                $http.post('/ws/config-app/ab-test/default/' + dflt, $scope.abTest).
                    success(function (data) {
                        if (data.success) {
                            $scope.showMsg('success', data.msg);
                            $scope.abTest = data.object;
                        } else {
                            $scope.showMsg('warning', data.msg);
                        }
                    }).
                    error(function (data, status) {
                        $scope.showMsg('warning', 'Server error: ' + status);
                    });
            };


        $scope.edit = function () {
            $location.path('/ab-test/edit/' + $scope.abTest.id);
        };

        $scope.cancel = function () {
            $location.path('/ab-test/');
        };
    }]);

abTestControllers.controller('AbTestEditCtrl', ['$scope', '$location', '$routeParams', 'AbTest',
    function ($scope, $location, $routeParams, AbTest) {

        $scope.abTest = AbTest.get({abId: $routeParams.abId}, function () {
            if (!$scope.abTest.id) {
                $location.path('/ab-test/new');
            }
        });

        $scope.title = "Edit AB-Test config";

        $scope.cancel = function () {
            $location.path('/ab-test/view/' + $routeParams.abId);
        }
    }]);

abTestControllers.controller('AbTestNewCtrl', ['$scope', '$location', 'AbTest',
    function ($scope, $location, AbTest) {
        $scope.abTest = new AbTest({
            distribution: [
                {}
            ]
        });

        $scope.title = "New AB-Test config";

        $scope.cancel = function () {
            $location.path('/ab-test/');
        }
    }]);

abTestControllers.controller('AbTestDistributionCtrl', ['$scope',
    function ($scope) {
        $scope.addCascade = function () {
            $scope.abTest.distribution.push({});
        };
        $scope.removeCascade = function (index) {
            $scope.abTest.distribution.splice(index, 1);
        };
    }]);


abTestControllers.controller('AbTestFormCtrl', ['$scope', '$location', 'AllCascades',
    function ($scope, $location, AllCascades) {
        $scope.cascades = AllCascades.query();

        $scope.save = function () {
            var abTestCopy = angular.copy($scope.abTest);
            $scope.abTest.$save(function (response) {
                if (response.success) {
                    $scope.showMsg('success', response.msg);
                    $location.path('/ab-test/view/' + response.object);
                } else {
                    $scope.showMsg('warning', response.msg);
                    $scope.abTest = abTestCopy;
                }
            });
        };
    }]);