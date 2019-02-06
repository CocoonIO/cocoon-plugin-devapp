#!/usr/bin/env node

var fs = require('fs');
var path = require('path');
var _ = require('./node_modules/lodash');
var et = require('./node_modules/elementtree');
var plist = require('./node_modules/plist');

var rootdir;
var plugindir;

var platformConfig = (function(){

    var configXmlData, pluginXmlData, preferencesData;

    return {
        parseElementtreeSync: function (filename) {
            var contents = fs.readFileSync(filename, 'utf-8');
            if(contents) {
                //Windows is the BOM. Skip the Byte Order Mark.
                contents = contents.substring(contents.indexOf('<'));
            }
            return new et.ElementTree(et.XML(contents));
        },

        eltreeToXmlString: function (data) {
            var tag = data.tag;
            var el = '<' + tag + '>';

            if(data.text && data.text.trim()) {
                el += data.text.trim();
            } else {
                _.each(data.getchildren(), function (child) {
                    el += platformConfig.eltreeToXmlString(child);
                });
            }

            el += '</' + tag + '>';
            return el;
        },

        getConfigXml: function () {
            if(!configXmlData) {
                configXmlData = this.parseElementtreeSync(path.join(rootdir, 'config.xml'));
            }

            return configXmlData;
        },

        getPluginXml: function () {
            if(!pluginXmlData) {
                pluginXmlData = this.parseElementtreeSync(path.join(plugindir, 'plugin.xml'));
            }

            return pluginXmlData;
        },

        getConfigFilesByTargetAndParent: function (platform) {
            var configFileData = this.getPluginXml().findall('platform[@name=\'' + platform + '\']/config-file');

            return  _.indexBy(configFileData, function(item) {
                var parent = item.attrib.parent;
                //if parent attribute is undefined /* or */, set parent to top level elementree selector
                if(!parent || parent === '/*' || parent === '*/') {
                    parent = './';
                }
                return item.attrib.target + '|' + parent;
            });
        },

        // Parses the config.xml's preferences and config-file elements for a given platform
        parseConfigXml: function (platform) {
            var configData = {};
            this.parseConfigFiles(configData, platform);

            return configData;
        },

        parseConfigFiles: function (configData, platform) {
            var configFiles = this.getConfigFilesByTargetAndParent(platform),
                type = 'configFile';

            _.each(configFiles, function (configFile, key) {
                var keyParts = key.split('|');
                var target = keyParts[0];
                var parent = keyParts[1];
                var items = configData[target] || [];

                _.each(configFile.getchildren(), function (element) {
                    items.push({
                        parent: parent,
                        type: type,
                        destination: element.tag,
                        data: element
                    });
                });

                configData[target] = items;
            });
        },

        updatePlatformConfig: function (platform) {
            var configData = this.parseConfigXml(platform),
                platformPath = path.join(rootdir, 'platforms', platform);

            _.each(configData, function (configItems, targetFileName) {
                var projectName, targetFile;

                if (platform === 'ios' && targetFileName.indexOf("Info.plist") > -1) {
                    projectName = platformConfig.getConfigXml().findtext('name');
                    targetFile = path.join(platformPath, projectName, projectName + '-Info.plist');
                    platformConfig.updateIosPlist(targetFile, configItems);
                }
            });
        },

        updateIosPlist: function (targetFile, configItems) {
            var infoPlist = plist.parse(fs.readFileSync(targetFile, 'utf-8')),
                tempInfoPlist;

            _.each(configItems, function (item) {
                var key = item.parent;
                var plistXml = '<plist><dict><key>' + key + '</key>';
                plistXml += platformConfig.eltreeToXmlString(item.data) + '</dict></plist>';

                var configPlistObj = plist.parse(plistXml);
                infoPlist[key] = configPlistObj[key];
            });

            tempInfoPlist = plist.build(infoPlist);
            tempInfoPlist = tempInfoPlist.replace(/<string>[\s\r\n]*<\/string>/g,'<string></string>');
            fs.writeFileSync(targetFile, tempInfoPlist, 'utf-8');
        }
    };
})();

module.exports = function(context) {
    rootdir = context.opts.projectRoot;
    plugindir = context.opts.plugin.dir;

    var platforms = _.filter(fs.readdirSync('platforms'), function (file) {
        return fs.statSync(path.resolve('platforms', file)).isDirectory();
    });

    _.each(platforms, function (platform) {
        try {
            platform = platform.trim().toLowerCase();
            platformConfig.updatePlatformConfig(platform);
        } catch (e) {
            process.stdout.write(e);
        }
    });
}