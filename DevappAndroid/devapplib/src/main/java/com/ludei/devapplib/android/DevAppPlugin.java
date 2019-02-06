package com.ludei.devapplib.android;

import org.apache.cordova.CordovaPlugin;

/**
 * Created by imanolmartin on 17/03/15.
 */
public class DevAppPlugin extends CordovaPlugin {

    public Boolean shouldAllowRequest(java.lang.String url) {
        return true;
    }

    public Boolean shouldAllowNavigation(java.lang.String url) {
        return true;
    }

    public Boolean shouldAllowBridgeAccess(java.lang.String url) {
        return true;
    }

    public Boolean shouldOpenExternalUrl(java.lang.String url) {
        return true;
    }

}
