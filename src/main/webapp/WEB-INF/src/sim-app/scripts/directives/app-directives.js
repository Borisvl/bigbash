'use strict';

var directives = angular.module('appDirectives', []);

directives.directive('focus',
    function () {
        return {
            link: function (scope, element, attrs) {
                element[0].focus();
            }
        };
    });