'use strict';

var services = angular.module('burpServices', ['ngResource']);

services.factory('Burp', ['$resource',
    function ($resource) {
        return $resource('/ws/config-app/burp/:burpId', {}, {});
    }]);

services.factory('AllBurps', [ '$resource',
    function ($resource) {
        return $resource('/ws/config-app/burp/find-all/:archived', {archived: false}, {});
    }]);

services.factory('FiltersUtil', ['FilterKey', 'ScoringFunctionKey',
    function (FilterKey, ScoringFunctionKey) {
        return {

            supportedScoringFunctions: function (key) {
                switch (key.value) {
                    case ScoringFunctionKey.UPSELLING:
                    case ScoringFunctionKey.AB_UPSELLING:
                        return true;
                    default:
                        return false;
                }
            },

            getSelectedScoringFunction: function (filterConfig) {
                if (filterConfig.$resolved) {
                    var dbScoringFunction = filterConfig.scoringFunction;
                    if (dbScoringFunction) {
                        return dbScoringFunction.type;
                    } else {
                        return "";
                    }
                }
            },

            emptyScoringFunction: function () {
                var jsScoringFunctions = {};
                jsScoringFunctions.upsell = {};
                jsScoringFunctions.abUpsell = {};
                return jsScoringFunctions;
            },

            scoringFunctionData: function (filterConfig) {
                var jsScoringFunctions = this.emptyScoringFunction();
                if (filterConfig.$resolved) {

                    var dbScoringFunction = filterConfig.scoringFunction;
                    if (dbScoringFunction) {
                        switch (dbScoringFunction.type) {
                            case ScoringFunctionKey.UPSELLING:
                                jsScoringFunctions.upsell.buyProbPower = dbScoringFunction.buyProbPower;
                                jsScoringFunctions.upsell.recoClickProbPower = dbScoringFunction.recoClickProbPower;
                                jsScoringFunctions.upsell.priceContributionPower = dbScoringFunction.priceContributionPower;
                                jsScoringFunctions.upsell.useTestBuyProb = dbScoringFunction.useTestBuyProb;
                                break;
                            case ScoringFunctionKey.AB_UPSELLING:
                                jsScoringFunctions.abUpsell.min = dbScoringFunction.min;
                                jsScoringFunctions.abUpsell.max = dbScoringFunction.max;
                                jsScoringFunctions.abUpsell.step = dbScoringFunction.step;
                                jsScoringFunctions.abUpsell.useTestBuyProb = dbScoringFunction.useTestBuyProb;
                                break;
                            default:
                                break;
                        }
                    }
                }
                return jsScoringFunctions;
            },

            toScoringFunction: function (selected, jsScoringFunctions) {
                var dbScoringFunction = {};
                switch (selected) {
                    case ScoringFunctionKey.UPSELLING:
                        dbScoringFunction.type = ScoringFunctionKey.UPSELLING;
                        dbScoringFunction.buyProbPower = jsScoringFunctions.upsell.buyProbPower;
                        dbScoringFunction.recoClickProbPower = jsScoringFunctions.upsell.recoClickProbPower;
                        dbScoringFunction.priceContributionPower = jsScoringFunctions.upsell.priceContributionPower;
                        dbScoringFunction.useTestBuyProb = jsScoringFunctions.upsell.useTestBuyProb;
                        break;
                    case ScoringFunctionKey.AB_UPSELLING:
                        dbScoringFunction.type = ScoringFunctionKey.AB_UPSELLING;
                        dbScoringFunction.min = jsScoringFunctions.abUpsell.min;
                        dbScoringFunction.max = jsScoringFunctions.abUpsell.max;
                        dbScoringFunction.step = jsScoringFunctions.abUpsell.step;
                        dbScoringFunction.useTestBuyProb = jsScoringFunctions.abUpsell.useTestBuyProb;
                        break;
                    default:
                        dbScoringFunction = null;
                        break;
                }
                return dbScoringFunction;
            },


            supportedFilters: function (filterKey) {
                switch (filterKey.value) {
                    case FilterKey.SEASON:
                    case FilterKey.BRAND:
                    case FilterKey.TAG:
                    case FilterKey.PRICE:
                    case FilterKey.LAYER:
                    case FilterKey.BASKET_VARIANTS:
                    case FilterKey.RECO_VARIANTS:
                    case FilterKey.REMOVE_ALL:
                    case FilterKey.TARGET_GROUP_SET:
                    case FilterKey.ATTRIBUTE_SET_CODE:
                    case FilterKey.SEASON_CODE:
                    case FilterKey.AGE_GROUP_CODE:
                    case FilterKey.SALE_CODE:
                    case FilterKey.GENDER_CODE:
                        return true;
                    default:
                        return false;
                }
            },

            getSelectedFilters: function (filterConfig) {
                if (filterConfig.$resolved) {
                    var result = [];
                    var filters = filterConfig.filters;
                    for (var i = 0; i < filters.length; i++) {
                        result.push(filters[i].type);
                    }
                    return result;
                }
            },

            emptyFilter: function () {
                var filters = {};
                filters.priceFilter = {};
                filters.layerFilter = [];
                filters.removeAllFilter = [];
                filters.attributeSetCodeFilter = {};
                filters.attributeSetCodeFilter.attributeSets = [];
                filters.seasonCodeFilter = {};
                filters.seasonCodeFilter.seasonCodes = []
                filters.ageGroupCodeFilter = {};
                filters.ageGroupCodeFilter.ageGroups = [];
                filters.saleCodeFilter = {};
                filters.genderCodeFilter = {};
                return filters;
            },


            filtersData: function (filterConfig) {
                var filters = this.emptyFilter();
                if (filterConfig.$resolved) {
                    filters = this.fromFilterConfig(filterConfig.filters);
                }
                return filters;
            },

            fromFilterConfig: function (dbFilters) {
                var jsFilters = this.emptyFilter();
                for (var i = 0; i < dbFilters.length; i++) {
                    switch (dbFilters[i].type) {
                        case FilterKey.PRICE:
                            jsFilters.priceFilter = this.fromPriceFilter(dbFilters[i]);
                            break;
                        case FilterKey.LAYER:
                            jsFilters.layerFilter = this.fromLayerFilter(dbFilters[i]);
                            break;
                        case FilterKey.REMOVE_ALL:
                            jsFilters.removeAllFilter = this.fromRemoveAllFilter(dbFilters[i]);
                            break;
                        case FilterKey.ATTRIBUTE_SET_CODE:
                            jsFilters.attributeSetCodeFilter = this.fromAttributeSetCodeFilter(dbFilters[i]);
                            break;
                        case FilterKey.SEASON_CODE:
                            jsFilters.seasonCodeFilter = this.fromSeasonCodeFilter(dbFilters[i]);
                            break;
                        case FilterKey.AGE_GROUP_CODE:
                            jsFilters.ageGroupCodeFilter = this.fromAgeGroupCodeFilter(dbFilters[i]);
                            break;
                        case FilterKey.SALE_CODE:
                            jsFilters.saleCodeFilter = this.fromSaleCodeFilter(dbFilters[i]);
                            break;
                        case FilterKey.GENDER_CODE:
                            jsFilters.genderCodeFilter = this.fromGenderCodeFilter(dbFilters[i]);
                            break;
                        default:
                            break;
                    }
                }
                return jsFilters;
            },

            toFilterConfig: function (selected, jsFilters) {
                var dbFilters = [];
                for (var i = 0; i < selected.length; i++) {
                    switch (selected[i]) {
                        case FilterKey.PRICE:
                            dbFilters.push(this.toPriceFilter(jsFilters.priceFilter));
                            break;
                        case FilterKey.LAYER:
                            dbFilters.push(this.toLayerFilter(jsFilters.layerFilter));
                            break;
                        case FilterKey.REMOVE_ALL:
                            dbFilters.push(this.toRemoveAllFilter(jsFilters.removeAllFilter));
                            break;
                        case FilterKey.ATTRIBUTE_SET_CODE:
                            dbFilters.push(this.toAttributeSetCodeFilter(jsFilters.attributeSetCodeFilter));
                            break;
                        case FilterKey.SEASON_CODE:
                            dbFilters.push(this.toSeasonCodeFilter(jsFilters.seasonCodeFilter));
                            break;
                        case FilterKey.AGE_GROUP_CODE:
                            dbFilters.push(this.toAgeGroupCodeFilter(jsFilters.ageGroupCodeFilter));
                            break;
                        case FilterKey.SALE_CODE:
                            dbFilters.push(this.toSaleCodeFilter(jsFilters.saleCodeFilter));
                            break;
                        case FilterKey.GENDER_CODE:
                            dbFilters.push(this.toGenderCodeFilter(jsFilters.genderCodeFilter));
                            break;
                        default:
                            var filter = {};
                            filter.type = selected[i];
                            dbFilters.push(filter);
                    }
                }
                return dbFilters;
            },

            fromAttributeSetCodeFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.attributeSets = dbFilter.attributeSets;
                    jsFilter.exclude = dbFilter.exclude;
                }
                return jsFilter;
            },

            toAttributeSetCodeFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.ATTRIBUTE_SET_CODE;
                dbFilter.attributeSets = jsFilter.attributeSets;
                dbFilter.exclude = jsFilter.exclude;
                return dbFilter;
            },

            fromSeasonCodeFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.seasonCodes = dbFilter.seasonCodes;
                    jsFilter.exclude = dbFilter.exclude;
                }
                return jsFilter;
            },

            toSeasonCodeFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.SEASON_CODE;
                dbFilter.seasonCodes = jsFilter.seasonCodes;
                dbFilter.exclude = jsFilter.exclude;
                return dbFilter;
            },

            fromAgeGroupCodeFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.ageGroups = dbFilter.ageGroups;
                    jsFilter.exclude = dbFilter.exclude;
                }
                return jsFilter;
            },

            toAgeGroupCodeFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.AGE_GROUP_CODE;
                dbFilter.ageGroups = jsFilter.ageGroups;
                dbFilter.exclude = jsFilter.exclude;
                return dbFilter;
            },

            fromSaleCodeFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.isSale = dbFilter.isSale;
                }
                return jsFilter;
            },

            toSaleCodeFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.SALE_CODE;
                dbFilter.isSale = jsFilter.isSale;
                return dbFilter;
            },

            fromGenderCodeFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.gender = dbFilter.gender;
                    jsFilter.exclude = dbFilter.exclude;
                }
                return jsFilter;
            },

            toGenderCodeFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.GENDER_CODE;
                dbFilter.gender = jsFilter.gender;
                dbFilter.exclude = jsFilter.exclude;
                return dbFilter;
            },

            fromPriceFilter: function (dbFilter) {
                var jsFilter = {};
                if (dbFilter) {
                    jsFilter.limit = dbFilter.price_limit;
                    jsFilter.mandatory = dbFilter.mandatory;
                }
                return jsFilter;
            },

            toPriceFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.PRICE;
                dbFilter.price_limit = jsFilter.limit;
                dbFilter.mandatory = jsFilter.mandatory;
                return dbFilter;
            },


            fromLayerFilter: function (dbFilter) {
                var jsFilter = [];
                if (dbFilter) {
                    for (var key in dbFilter.filters) {
                        var tmp = {};
                        tmp.from = key;
                        tmp.to = dbFilter.filters[key];
                        jsFilter.push(tmp);
                    }
                }
                return jsFilter;
            },

            toLayerFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.LAYER;
                dbFilter.filters = {};
                for (var i = 0; i < jsFilter.length; i++) {
                    dbFilter.filters[jsFilter[i].from] = jsFilter[i].to;
                }
                return dbFilter;
            },

            fromRemoveAllFilter: function (dbFilter) {
                var jsFilter = [];
                if (dbFilter) {
                    jsFilter = dbFilter.removeAllForLayers;
                }
                return jsFilter;
            },

            toRemoveAllFilter: function (jsFilter) {
                var dbFilter = {};
                dbFilter.type = FilterKey.REMOVE_ALL;
                dbFilter.removeAllForLayers = jsFilter;
                return dbFilter;
            }
        }
    }]);

