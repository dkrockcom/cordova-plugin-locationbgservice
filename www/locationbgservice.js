/* global cordova:false */
/* globals window */

var exec = cordova.require('cordova/exec'),
    utils = cordova.require('cordova/utils');

var template = {
    start: function (successCallback, errorCallback, args) {
        var clientId = args.clientId || 0;
        var url = args.url || '';
        var interval = args.interval || 10000;

        var options = [
            clientId,
            url,
            interval
        ];
        
        exec(successCallback, errorCallback, 'LocationBGServicePlugin', 'start', options);
    },
    stop: function () {
        exec(null, null, 'LocationBGServicePlugin', 'stop', null);
    },
    isRunning: function (successCallback) {
        exec(successCallback, null, 'LocationBGServicePlugin', 'isRunning', null);
    }
};

module.exports = template;
