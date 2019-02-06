package com.ludei.devapplib.android;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;

import com.ludei.devapplib.android.utils.LauncherUtils;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by imanolmartin on 10/03/15.
 */
public class CocoonCordovaActivity extends CordovaActivity {

    public static final String CANVASPLUS_CLASS = "com.ludei.canvasplus.CanvasPlusEngine";
    public static final String WEBVIEWPLUS_CLASS = "org.crosswalk.engine.XWalkWebViewEngine";
    public static final String SYSTEM_WEBVIEW_CLASS = "org.apache.cordova.engine.SystemWebViewEngine";

    public static final String URL = "URL";
    public static final String WEBVIEW = "WEBVIEW";
    public static final String ORIENTATION = "ORIENTATION";

    private LauncherUtils.Orientation orientation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getIntent().getStringExtra(URL) != null)
            launchUrl = getIntent().getStringExtra(URL);

        // Set the launch webview in the preferences
        if (getIntent().getStringExtra(WEBVIEW) != null)
            preferences.set("webview", getIntent().getStringExtra(WEBVIEW));
        else
            preferences.set("webview", SYSTEM_WEBVIEW_CLASS);

        // Set the Canvas+ settings
        if (getIntent().getStringExtra(WEBVIEW).equalsIgnoreCase(CANVASPLUS_CLASS)) {
            Set<Map.Entry<String, Integer>> settings =  LauncherUtils.getCanvasPlusSettings(this).entrySet();
            for (Map.Entry entry : settings) {
                preferences.set(entry.getKey().toString(), (Integer)entry.getValue());
            }
            preferences.set(LauncherUtils.SETTING_CANVASPLUS_USE_FULL_LIFECYCLE, 0);

        } else if (LauncherUtils.isEngineAvailable(LauncherUtils.Engines.CANVAS_PLUS) &&
                getIntent().getStringExtra(WEBVIEW).equalsIgnoreCase(SYSTEM_WEBVIEW_CLASS)) {
            // We use the internal webview if Canvas+ is available so we can support both Cordova and OpenSDK plugins
            preferences.set("webview", CANVASPLUS_CLASS);
            Set<Map.Entry<String, Integer>> settings = LauncherUtils.getCanvasPlusSettings(this).entrySet();
            for (Map.Entry entry : settings) {
                preferences.set(entry.getKey().toString(), (Integer) entry.getValue());
            }
            preferences.set(LauncherUtils.SETTING_CANVASPLUS_USE_FULL_LIFECYCLE, 0);
            preferences.set(LauncherUtils.SETTING_LAUNCH_IN_WEBVIEW, 1);
            preferences.set(LauncherUtils.SETTING_ACCELERATED_WEBVIEW, 1);
        }

        Set<Map.Entry<String, Integer>> settings =  LauncherUtils.getGeneralSettings(this).entrySet();
        for (Map.Entry entry : settings) {
            preferences.set(entry.getKey().toString(), (Integer)entry.getValue());
        }

        int preferredOrientation = preferences.getInteger(LauncherUtils.SETTING_ORIENTATION, LauncherUtils.Orientation.SCREEN_ORIENTATION_BOTH.ordinal());
        String forcedOrientation = getIntent().getStringExtra(ORIENTATION);
        if (forcedOrientation != null) {
            if (forcedOrientation.equalsIgnoreCase("landscape"))
                orientation = LauncherUtils.Orientation.SCREEN_ORIENTATION_LANDSCAPE;
            else if (forcedOrientation.equalsIgnoreCase("portrait"))
                orientation = LauncherUtils.Orientation.SCREEN_ORIENTATION_PORTRAIT;
            else
                orientation = LauncherUtils.Orientation.SCREEN_ORIENTATION_BOTH;
        } else {
            orientation = LauncherUtils.Orientation.values()[preferredOrientation];
        }
        setOrientation(orientation);

        loadUrl(URLUtil.guessUrl(launchUrl));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setOrientation(orientation);
    }

    private void setOrientation(LauncherUtils.Orientation orientation) {
        switch (orientation) {
            case SCREEN_ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;

            case SCREEN_ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;

            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
        }
    }
}
