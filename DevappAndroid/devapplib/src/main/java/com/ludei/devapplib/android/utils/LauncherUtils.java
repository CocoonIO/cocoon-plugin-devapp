package com.ludei.devapplib.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ludei.devapplib.android.CocoonCordovaActivity;
import com.ludei.devapplib.android.R;

import java.util.HashMap;

/**
 * Created by imanolmartin on 24/04/14.
 */
public class LauncherUtils {

    /**
     * Webviews
     */
    public enum Engines {
        CANVAS_PLUS,
        WEBVIEW_PLUS,
        SYSTEM_WEBVIEW
    }

    public static final String CANVAS_PLUS = "Canvas";
    public static final String WEBVIEW = "Webview";

    public enum Orientation {
        SCREEN_ORIENTATION_LANDSCAPE,
        SCREEN_ORIENTATION_PORTRAIT,
        SCREEN_ORIENTATION_BOTH
    }

    /**
     * Canvas+ settings
     */
    public static final String SETTING_ORIENTATION = "orientation";
    public static final String SETTING_CANVASPLUS_DEBUG_ENABLED = "debug_enabled";
    public static final String SETTING_CANVASPLUS_DEBUG_POSITION = "debug_position";
    public static final String SETTING_CANVASPLUS_REMOTE_DEBUG_ENABLED = "remote_debug_enabled";
    public static final String SETTING_CANVASPLUS_FPS_TYPE = "fps_type";
    public static final String SETTING_CANVASPLUS_WEBGL_ENABLED = "webgl_enabled";
    public static final String SETTING_CANVASPLUS_SCREENCANVAS_MODE = "screencanvas_mode";
    public static final String SETTING_CANVASPLUS_TEXTUREREDUCER_LEVEL = "texturereducer_level";
    public static final String SETTING_CANVASPLUS_SUPERSAMPLING_LEVEL = "supersampling_level";
    public static final String SETTING_CANVASPLUS_SCALE_MODE = "scale_mode";
    public static final String SETTING_CANVASPLUS_RENDERPATH_QUALITY = "renderpath_quality";
    public static final String SETTING_CANVASPLUS_NPOT_ALLOWED = "npot_allowed";
    public static final String SETTING_CANVASPLUS_USE_FULL_LIFECYCLE = "full_lifecycle";
    public static final String SETTING_LAUNCH_IN_WEBVIEW = "launch_in_webview";
    public static final String SETTING_ACCELERATED_WEBVIEW = "accelerated_webview";

    public static void launchWebView(Context context, String url, String orientation) {
        Intent launchIntent = new Intent(context, CocoonCordovaActivity.class);
        launchIntent.putExtra(CocoonCordovaActivity.ORIENTATION, orientation);
        launchIntent.putExtra(CocoonCordovaActivity.URL, url);
        launchIntent.putExtra(CocoonCordovaActivity.WEBVIEW, CocoonCordovaActivity.SYSTEM_WEBVIEW_CLASS);
        context.startActivity(launchIntent);
    }

    public static void launchWebViewPlus(Context context, String url, String orientation) {
        Intent launchIntent = new Intent(context, CocoonCordovaActivity.class);
        launchIntent.putExtra(CocoonCordovaActivity.ORIENTATION, orientation);
        launchIntent.putExtra(CocoonCordovaActivity.URL, url);
        launchIntent.putExtra(CocoonCordovaActivity.WEBVIEW, CocoonCordovaActivity.WEBVIEWPLUS_CLASS);
        context.startActivity(launchIntent);
    }

    public static void launchCanvasPlus(Context context, String url, String orientation) {
        Intent launchIntent = new Intent(context, CocoonCordovaActivity.class);
        launchIntent.putExtra(CocoonCordovaActivity.ORIENTATION, orientation);
        launchIntent.putExtra(CocoonCordovaActivity.URL, url);
        launchIntent.putExtra(CocoonCordovaActivity.WEBVIEW, CocoonCordovaActivity.CANVASPLUS_CLASS);
        context.startActivity(launchIntent);
    }

    public static boolean isEngineAvailable(Engines webview) {
        switch (webview) {
            case CANVAS_PLUS:
                try {
                    Class.forName(CocoonCordovaActivity.CANVASPLUS_CLASS);
                    return true;

                } catch( ClassNotFoundException e ) {
                    return false;
                }

            case WEBVIEW_PLUS:
                try {
                    Class.forName(CocoonCordovaActivity.WEBVIEWPLUS_CLASS);
                    return true;

                } catch( ClassNotFoundException e ) {
                    return false;
                }

            default:
                return true;
        }
    }

    public static HashMap<String, Integer> getCanvasPlusSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        HashMap<String, Integer> settings = new HashMap<>();
        boolean webGLEnabled = prefs.getBoolean(context.getString(R.string.pref_webgl_enabled_key), Boolean.parseBoolean(context.getString(R.string.pref_webgl_enabled_default)));
        settings.put(SETTING_CANVASPLUS_WEBGL_ENABLED, webGLEnabled == true ? 1 : 0);
        int scaleMode = Integer.parseInt(prefs.getString(context.getString(R.string.pref_scale_mode_key), context.getString(R.string.pref_scale_mode_default)));
        settings.put(SETTING_CANVASPLUS_SCALE_MODE, scaleMode);
        int webglScreencanvas = Integer.parseInt(prefs.getString(context.getString(R.string.pref_webgl_sreencanvas_key), context.getString(R.string.pref_webgl_sreencanvas_default)));
        settings.put(SETTING_CANVASPLUS_SCREENCANVAS_MODE, webglScreencanvas);
        boolean debugEnabled = prefs.getBoolean(context.getString(R.string.pref_debug_enable_key), Boolean.parseBoolean(context.getString(R.string.pref_debug_enable_default)));
        settings.put(SETTING_CANVASPLUS_DEBUG_ENABLED, debugEnabled == true ? 1 : 0);
        boolean remoteDebugEnabled = prefs.getBoolean(context.getString(R.string.pref_remote_debug_enable_key), Boolean.parseBoolean(context.getString(R.string.pref_remote_debug_enable_default)));
        settings.put(SETTING_CANVASPLUS_REMOTE_DEBUG_ENABLED, remoteDebugEnabled == true ? 1 : 0);
        int debugPosition = Integer.parseInt(prefs.getString(context.getString(R.string.pref_debug_position_key), context.getString(R.string.pref_debug_position_default)));
        settings.put(SETTING_CANVASPLUS_DEBUG_POSITION, debugPosition);
        int fpsType = Integer.parseInt(prefs.getString(context.getString(R.string.pref_fps_type_key), context.getString(R.string.pref_fps_type_default)));
        settings.put(SETTING_CANVASPLUS_FPS_TYPE, fpsType);
        int textureReducer = Integer.parseInt(prefs.getString(context.getString(R.string.pref_texture_reducer_key), context.getString(R.string.pref_texture_reducer_default)));
        settings.put(SETTING_CANVASPLUS_TEXTUREREDUCER_LEVEL, textureReducer);
        int superSamplingLevel = Integer.parseInt(prefs.getString(context.getString(R.string.pref_supersampling_level_key), context.getString(R.string.pref_supersampling_level_default)));
        settings.put(SETTING_CANVASPLUS_SUPERSAMPLING_LEVEL, superSamplingLevel);
        int pathRenderQuality = Integer.parseInt(prefs.getString(context.getString(R.string.pref_path_render_quality_key), context.getString(R.string.pref_path_render_quality_default)));
        settings.put(SETTING_CANVASPLUS_RENDERPATH_QUALITY, pathRenderQuality);
        boolean npotAllowed = prefs.getBoolean(context.getString(R.string.pref_npot_allowed_key), Boolean.parseBoolean(context.getString(R.string.pref_npot_allowed_default)));
        settings.put(SETTING_CANVASPLUS_NPOT_ALLOWED, npotAllowed == true ? 1 : 0);

        return settings;
    }

    public static HashMap<String, Integer> getGeneralSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        HashMap<String, Integer> settings = new HashMap<>();
        int orientation = Integer.parseInt(prefs.getString(context.getString(R.string.pref_orientation_mode_key), context.getString(R.string.pref_orientation_mode_default)));
        settings.put(SETTING_ORIENTATION, orientation);

        return settings;
    }

}
