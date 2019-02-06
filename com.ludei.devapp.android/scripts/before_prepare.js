#!/usr/bin/env node

var fs = require('fs');
var path = require('path');

rm = function(dirPath, deleteCurrent) {
	var files = [];
	try { 
		files = fs.readdirSync(dirPath); 

	} catch(e) { 
		return; 
	}
	
	if (files.length > 0) {
		for (var i = 0; i < files.length; i++) {
			var filePath = dirPath + '/' + files[i];
			if (fs.statSync(filePath).isFile())
				fs.unlinkSync(filePath);
			else
				rm(filePath, true);
		}
	}

	if (deleteCurrent)
		fs.rmdirSync(dirPath);
}

module.exports = function(context) {
    if (context.opts.cordova.platforms.indexOf('android') <= -1)
        return;

    var www_path = path.join(context.opts.projectRoot, 'www');
	rm(www_path, false);
}
