#!/usr/bin/env node

var path = require('path');
var fs 	= require('fs');


function installPlugin(context, cmd) {
	var project_path = context.opts.projectRoot;
    var platform_path = path.join(project_path, "platforms", "ios");
    var plugins_path = path.join(project_path, "plugins", "com.ludei.ios.webview.plus");
    var folderContent = fs.readdirSync(platform_path);
   	var xcodeproj_path = folderContent.filter(function(item){ return item.indexOf("xcodeproj") !== -1; })[0];
    var index = xcodeproj_path.lastIndexOf(".");
    var xcodeproj_name = xcodeproj_path.substring(0,index);

    if (!xcodeproj_path) {
    	throw new Error("Cannot find a valid 'xcodeproj' inside " + platform_path);
    }

    var main_path = path.join(platform_path, xcodeproj_name, "main.m");
    if (!fs.existsSync(main_path))
        main_path = path.join(platform_path, xcodeproj_name, "main.mm");
    var main_content = fs.readFileSync(main_path, "utf8");
    main_content = main_content.replace(new RegExp('AppDelegate', 'g'), 'LauncherAppDelegate')
	fs.writeFileSync(main_path, main_content, "utf8");
}

module.exports = installPlugin;
