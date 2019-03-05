var exec = require('cordova/exec');

exports.helloWorld = function (arg0, success, error) {
    exec(success, error, 'USBSerialAndroid', 'helloWorld', [arg0]);
};

exports.sendMasterCommand = function (arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, 'USBSerialAndroid', 'sendMasterCommand', [arg0, arg1, arg2, arg3]);
};