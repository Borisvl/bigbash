'use strict';

var newsletterControllers = angular.module('newsletterControllers', ['bootstrap-tagsinput']);

newsletterControllers.controller('NewsletterListCtrl', ['$scope', '$location', 'AllNewsletters',
    function ($scope, $location, AllNewsletters) {
        $scope.archivedNewsletters = false;

        $scope.refreshTable = function () {
            $scope.newsletters = AllNewsletters.query({archived: $scope.archivedNewsletters});
        }

        $scope.newNewsletter = function () {
            $location.path('/newsletter/new');
        }

        $scope.refreshTable();
    }]);

newsletterControllers.controller('NewsletterViewCtrl', ['$scope', '$http', '$location', '$routeParams', 'Newsletter', 'Cascade',
    function ($scope, $http, $location, $routeParams, Newsletter, Cascade) {
        $scope.newsletter = Newsletter.get({newsId: $routeParams.newsId});

        $scope.cascadeName = [];

        $scope.newsletter.$promise.then(function () {
            for (var i = 0; i < $scope.newsletter.groupConfigs.length; i++) {
                var cascadeId = $scope.newsletter.groupConfigs[i].cascadeId;
                $scope.cascadeName[cascadeId] = Cascade.get({cascadeId:cascadeId});
            }
        });


        $scope.approve = function () {
            $http.post('/ws/config-app/newsletter/approve', $scope.newsletter).
                success(function (data, status, headers, config) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.newsletter = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status, headers, config) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        }

        $scope.archive = function (archived) {
            $http.post('/ws/config-app/newsletter/archive/' + archived, $scope.newsletter).
                success(function (data, status, headers, config) {
                    if (data.success) {
                        $scope.showMsg('success', data.msg);
                        $scope.newsletter = data.object;
                    } else {
                        $scope.showMsg('warning', data.msg);
                    }
                }).
                error(function (data, status, headers, config) {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        }

        $scope.edit = function () {
            $location.path('/newsletter/edit/' + $scope.newsletter.id);
        };

        $scope.cancel = function () {
            $location.path('/newsletter/');
        }
    }]);

newsletterControllers.controller('NewsletterEditCtrl', ['$scope', '$location', '$routeParams', 'Newsletter',
    function ($scope, $location, $routeParams, Newsletter) {

        $scope.newsletter = Newsletter.get({newsId: $routeParams.newsId}, function () {
            if (!$scope.newsletter.id) {
                $location.path('/newsletter/new');
            }
        });

        $scope.title = "Edit newsletter config";

        $scope.cancel = function () {
            $location.path('/newsletter/view/' + $routeParams.newsId);
        }
    }]);

newsletterControllers.controller('NewsletterNewCtrl', ['$scope', '$location', 'Newsletter',
    function ($scope, $location, Newsletter) {
        $scope.newsletter = new Newsletter({
            groupConfigs: [
                {}
            ]
        });

        $scope.title = "New newsletter config";

        $scope.cancel = function () {
            $location.path('/newsletter/');
        }
    }]);

newsletterControllers.controller('NewsletterGroupConfigsCtrl', ['$scope',
    function ($scope) {
        $scope.addGroupConfig = function () {
            var groupConfigs = $scope.newsletter.groupConfigs;
            groupConfigs[groupConfigs.length] = {};
        };
        $scope.removeGroupConfig = function (index) {
            $scope.newsletter.groupConfigs.splice(index, 1);
        };
    }]);

newsletterControllers.controller('NewsletterFormCtrl', ['$scope', '$location', 'AllCascades',
    function ($scope, $location, AllCascades) {
        $scope.cascades = AllCascades.query();

        $scope.save = function () {
            var newsletterCopy = angular.copy($scope.newsletter);
            $scope.newsletter.$save(function (response) {
                if (response.success) {
                    $scope.showMsg('success', response.msg);
                    $location.path('/newsletter/view/' + response.object);
                } else {
                    $scope.showMsg('warning', response.msg);
                    $scope.newsletter = newsletterCopy;
                }
            });
        };
    }]);