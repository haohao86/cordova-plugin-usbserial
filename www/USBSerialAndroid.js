var exec = require('cordova/exec');

exports.helloWorld = function (arg0, success, error) {
    exec(success, error, 'USBSerialAndroid', 'helloWorld', [arg0]);
};

exports.sendMasterCommand = function (arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, 'USBSerialAndroid', 'sendMasterCommand', [arg0, arg1, arg2, arg3]);
};

exports.laser = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'laser', []);
};

exports.laserSendCommand = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'laserSendCommand', []);
};

exports.laserClose = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'laserClose', []);
};

exports.laserScan = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'laserScan', []);
};

exports.status = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'status', []);
};

exports.dongleOpen = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'dongleOpen', []);
};

exports.dongleClose = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'dongleClose', []);
};

exports.laserScanV2 = function (success, error) {
    exec(success, error, 'USBSerialAndroid', 'laserScanV2', []);
};
