var exec = require('cordova/exec');

exports.helloWorld = function (arg0, success, error) {
    exec(success, error, 'USBSerialAndroid', 'helloWorld', [arg0]);
};
