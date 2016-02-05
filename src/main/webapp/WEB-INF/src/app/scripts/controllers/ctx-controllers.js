'use strict';

var ctxControllers = angular.module('ctxControllers', []);

ctxControllers.controller('CtxCtrl', ['$scope', '$http',
    function ($scope, $http) {

        $scope.sql = "CREATE TABLE realtimelogging (actiondate DATE, \n"+
                      "appdomain INT, \n"+
                      "sku TEXT,\n"+
                      "simplesku TEXT,\n"+
                      "customerId TEXT,\n"+
                      "sessioneId TEXT,\n"+
                      "action TEXT,\n"+
                      "salecount INT,\n"+
                      "price INT,\n"+
                      "orderId TEXT,\n"+
                      "cookieId TEXT,\n"+
                      "shobabtest TEXT,\n"+
                      "internalReferer TEXT) ;\n"+
                      "\n"+
                      "Select appdomain, sum(price) from realtimelogging group by appdomain order by sum(price) DESC;";

        $scope.tables = [];
        var table = {}
        table.table = "realtimelogging";
        table.files = "data/*.gz";
        table.format = "GZ";
        table.separator = "\\t";

        $scope.tables.push(table);
        $scope.tables.push({});
        $scope.tables.push({});
        $scope.tables.push({});

        var toParamString = function (tables) {
            var result = "";
            for (var i = 0; i < tables.length; i++) {
                var table = tables[i];
                if (table.table && table.files && table.format && table.separator) {
                    result += table.table + " " + table.files + " " + table.format + " " + table.separator + " ";
                }
            }
            return result
        }

        $scope.compile = function () {

            $http.post('/ws/compile?params=' + toParamString($scope.tables), $scope.sql).
                success(function (data) {
                    $scope.bashCommand = data;
                }).
                error(function () {
                    $scope.showMsg('warning', 'Server error: ' + status);
                });
        };


    }]);

