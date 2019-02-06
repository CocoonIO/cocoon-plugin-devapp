#!/usr/bin/env node

var fs = require('fs');
var path = require('path');

module.exports = function(context) {
    if (context.opts.cordova.platforms.indexOf('android') <= -1)
        return;

    var libs_path = path.join(context.opts.projectRoot, 'platforms', 'android', 'app', 'src', 'main', 'libs');
	var jar_match = fs.readdirSync(libs_path).filter(
		function(element){ 
			return (element.match(/^android-support-v4.jar$/g) !== null); 
		});

	jar_match.forEach(function(file_name){
		fs.unlinkSync(path.join(libs_path, file_name));
	});

	// Remove the cordova MainActivity LAUNCHER flag so it doesn't appear in the desktop.
    var et = context.requireCordovaModule('elementtree');

    var manifest_xml = path.join(context.opts.projectRoot, 'platforms', 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
    var data = fs.readFileSync(manifest_xml).toString();
    var etree = et.parse(data);

    var activities = etree.findall('./application/activity');
    for (var i=0; i<activities.length; i++) {
        if (activities[i].get('android:name').indexOf("MainActivity") + "MainActivity".length === activities[i].get('android:name').length) {
            for (var j=0; j<activities[i].len(); j++) {
                var item = activities[i].getItem(j);
                if (item.tag === "intent-filter") {
                    for (var k=0; k<item.len(); k++) {
                        var element = item.getItem(k);
                        if (element.tag === 'category') {
                            var category_name = element.get('android:name');
                            if (category_name === "android.intent.category.LAUNCHER") {
                                console.log("Removed LAUNCHER flag")
                                item.delItem(k);
                            }
                        }
                    }
                }
            }
        }
    }

    data = etree.write({'indent': 4});
    fs.writeFileSync(manifest_xml, data);
}