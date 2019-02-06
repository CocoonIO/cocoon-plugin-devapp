package com.ludei.devapplib.android;

/**
 * Created by imanolmartin on 03/11/15.
 */
public class CommonConsts {

    public static final String LAUNCHER_OAUTH_CLIENT_ID = "cgx6ZXNLQxH8nM37a8yD";
    public static final String LAUNCHER_OAUTH_CLIENT_SECRET = "Y{63xB2w?*k8j8z^$#~N";
    public static final String LAUNCHER_OAUTH_REDIRECT_URI = "cocoon.devapp://callback";

    public static final String COCOON_OAUTH_LOGIN_URL = Consts.COCOON_BACKEND + "oauth/authorization?client_id=" + LAUNCHER_OAUTH_CLIENT_ID + "&redirect_uri=" + LAUNCHER_OAUTH_REDIRECT_URI + "&response_type=code";
    public static final String COCOON_OAUTH_ACCESS_TOKEN_URL = Consts.COCOON_BACKEND + "oauth/access_token";

    public static final String COCOON_ME_API_URL = Consts.COCOON_API + "me";
    public static final String COCOON_TEMPLATES_API_URL = Consts.COCOON_API + "cocoon/templates";

    public static int REQUEST_CODE_ASK_PERMISSIONS_EXTERNAL_STORAGE = 1;

    /**
     * Segment
     */

    //Launch
    public static final String SEG_LAUNCH = "FirstAppLaunch";

    //Home
    public static final String SEG_HOME_VIEW = "HomeView";
    public static final String SEG_DEMOS = "Demos";
    public static final String SEG_YOUR_APP = "YourApp";
    public static final String SEG_LOGIN = "LogIn";
    public static final String SEG_SIGNUP = "SignUp";

    //Demos
    public static final String SEG_DEMOS_VIEW = "DemosView";
    public static final String SEG_DEMOS_DETAIL_VIEW = "DemosDetailView";
    public static final String SEG_DEMOS_ENTER = "EnterDemo";
    public static final String SEG_DEMOS_SEARCH_ENTER = "UseSearchToEnterDemo";
    public static final String SEG_DEMOS_SEARCH = "SearchInDemos";
    public static final String SEG_DEMOS_SEARCH_CANCEL = "CancelSearchInDemos";
    public static final String SEG_DEMOS_BACK = "BackFromDemos";

    //Start
    public static final String SEG_CANVASPLUS_START = "CanvasPlusStart";
    public static final String SEG_WEBVIEWPLUS_START = "WebviewPlusStart";
    public static final String SEG_WEBVIEW_START = "WebviewStart";

    //YourApp
    public static final String SEG_URL_VIEW = "UrlView";
    public static final String SEG_URL_SEARCH = "SearchFromUrl";
    public static final String SEG_URL_SWIPE = "SwipeFromUrl";
    public static final String SEG_URL_SWIPE_FAV = "FavoriteFromSwipeFromUrl";
    public static final String SEG_URL_SWIPE_DEL = "DeleteFromSwipeFromUrl";
    public static final String SEG_URL_SEARCH_HISTORY = "HistoryFromSearchFromUrl";
    public static final String SEG_URL_SEARCH_FAV = "FavoritesFromSearchFromUrl";
    public static final String SEG_URL_SEARCH_DEMO = "DemosFromSearchFromUrl";
    public static final String SEG_URL_SEARCH_DOC = "DocumentsFromSearchFromUrl";
    public static final String SEG_URL_SEARCH_URL = "UrlFromSearchFromUrl ";
    public static final String SEG_URL_SCAN_QR = "ScanQRFromSearchFromUrl";
    public static final String SEG_URL_SEARCH_CANCEL = "CancelFromSearchFromUrl";
    public static final String SEG_URL_BACK_TO_MENU = "MenuFromUrl";
    public static final String SEG_URL_FAV = "FavoriteFromUrl";
    public static final String SEG_URL_CANVASPLUS = "CanvasPlusFromUrl";
    public static final String SEG_URL_WEBVIEWPLUS = "WebviewPlusFromUrl";
    public static final String SEG_URL_WEBVIEW = "WebviewFromUrl";
    public static final String SEG_URL_ACTIONS = "ActionsFromUrl";
    public static final String SEG_URL_ACTION_CLEAR = "ClearHistoryFromActionsFromUrl";

    //Documents
    public static final String SEG_DOC_VIEW = "DocumentsView";
    public static final String SEG_DOC_SELECT = "SelectFromDocuments";
    public static final String SEG_DOC_ACTIONS = "ActionsFromDocumentsInYourApp";
    public static final String SEG_DOC_ACTION_MULTI = "MultipleSelectionFromDocumentsInYourApp";
    public static final String SEG_DOC_ACTION_SORT_DATE = "SortByDateFromDocumentsInYourApp";
    public static final String SEG_DOC_ACTION_SORT_NAME = "SortByNameFromDocumentsInYourApp";
    public static final String SEG_DOC_ACTION_FAV = "FavoriteFromActionsFromDocumentsInYourApp";
    public static final String SEG_DOC_SWIPE = "SwipeFromDocuments";
    public static final String SEG_DOC_SWIPE_DEL = "DeleteFromSwipeInDocuments";
    public static final String SEG_DOC_SWIPE_FAV = "FavoriteFromSwipeInDocuments";
    public static final String SEG_DOC_BACK_TO_MENU = "MenuFromDocuments";
    public static final String SEG_DOC_CANVASPLUS = "CanvasPlusFromDocuments";
    public static final String SEG_DOC_WEBVIEWPLUS = "WebviewPlusFromDocuments";
    public static final String SEG_DOC_WEBVIEW = "WebviewFromDocuments";

    //Favorites
    public static final String SEG_FAV_VIEW = "FavoritesView";
    public static final String SEG_FAV_SELECT = "SelectFromFavorites";
    public static final String SEG_FAV_ACTIONS = "ActionsFromFavoritesInYourApp";
    public static final String SEG_FAV_ACTION_MULTI = "MultipleSelectionFromFavoritesInYourApp";
    public static final String SEG_FAV_ACTION_SORT_DATE = "SortByDateFromFavoritesInYourApp";
    public static final String SEG_FAV_ACTION_SORT_NAME = "SortByNameFromFavoritesInYourApp";
    public static final String SEG_FAV_ACTION_FAV = "RemoveFavoriteFromActionsFromFavoritesInYourApp";
    public static final String SEG_FAV_SWIPE = "SwipeFromDocuments";
    public static final String SEG_FAV_SWIPE_DEL = "DeleteFromSwipeInFavorites";
    public static final String SEG_FAV_SWIPE_FAV = "FavoriteFromSwipeInFavorites";
    public static final String SEG_FAV_BACK_TO_MENU = "MenuFromFavorites";
    public static final String SEG_FAV_CANVASPLUS = "CanvasPlusFromFavorites";
    public static final String SEG_FAV_WEBVIEWPLUS = "WebviewPlusFromFavorites";
    public static final String SEG_FAV_WEBVIEW = "WebviewFromFavorites";

    //Settings
    public static final String SEG_SETTINGS_VIEW = "SettingsView";
    public static final String SEG_SETTINGS_DETAIL_VIEW = "Settings%@View";
    public static final String SEG_SETTINGS_BACK_TO_MENU = "MenuFromSettings";
    public static final String SEG_SETTINGS_DETAIL_ENTER = "%@FromSettings";
    public static final String SEG_SETTINGS_DETAIL_BACK = "BackFrom%@";
    public static final String SEG_SETTINGS_SETVALUE = "%@From%@";
    public static final String SEG_SETTINGS_ACTIONS = "ActionsFromSettings";
    public static final String SEG_SETTINGS_ACTIONS_RESET = "ResetToDefaultsFromActionsFromSettings";

    public static final String SEG_SETTINGS_ORIENTATION = "OrientationModeFromSettings";
    public static final String SEG_SETTINGS_ORIENTATION_LANDSCAPE = "LandscapeFromOrientationModeFromSettings";
    public static final String SEG_SETTINGS_ORIENTATION_PORTRAIT = "PortraitFromOrientationModeFromSettings";
    public static final String SEG_SETTINGS_ORIENTATION_BOTH = "BothFromOrientationModeFromSettings";
    public static final String SEG_SETTINGS_ORIENTATION_BACK = "BackFromOrientationModeFromSettings";

    public static final String SEG_SETTINGS_DEBUG_POSITION = "DebugPositionFromSettings";
    public static final String SEG_SETTINGS_DEBUG_POSITION_TOP_LEFT = "TopLeftFromDebugPosition";
    public static final String SEG_SETTINGS_DEBUG_POSITION_TOP_RIGHT = "TopRightFromDebugPosition";
    public static final String SEG_SETTINGS_DEBUG_POSITION_BOTTOM_LEFT = "BottomLeftFromDebugPosition";
    public static final String SEG_SETTINGS_DEBUG_POSITION_BOTTOM_RIGHT = "BottomRightFromDebugPosition";
    public static final String SEG_SETTINGS_DEBUG_POSITION_BACK = "BackFromDebugPosition";

    public static final String SEG_SETTINGS_DEBUG_ENABLED = "DebugEnabledEnabledFromSettings";
    public static final String SEG_SETTINGS_DEBUG_DISABLED = "DebugEnabledDisabledFromSettings";

    public static final String SEG_SETTINGS_FPSTYPE = "FPSTypeFromSettings";
    public static final String SEG_SETTINGS_FPSTYPE_TIME = "FrameTimeFromFPSTType";
    public static final String SEG_SETTINGS_FPSTYPE_FPS = "FramesPerSecondFromFPSTType";
    public static final String SEG_SETTINGS_FPSTYPE_BACK = "BackFromFPSTType";

    public static final String SEG_SETTINGS_WEBGL_ENABLED = "WebGLEnabledEnabledFromSettings";
    public static final String SEG_SETTINGS_WEBGL_DISABLED= "WebGlEnabledDisabledFromSettings";

    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS = "WebGLScreencanvasFromSettings";
    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS_ENABLED = "EnabledByDefaultFromWebGLScreencanvas";
    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS_DISABLED = "DisabledByDefaultFromWebGLScreencanvas";
    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS_FORCE_ENABLED = "ForceEnabledFromWebGLScreencanvas";
    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS_FORCE_DISABLED = "ForceDisabledFromWebGLScreencanvas";
    public static final String SEG_SETTINGS_WEBGL_SCREENCANVAS_BACK = "BackFromWebGLScreencanvas";

    public static final String SEG_SETTINGS_WEBGL_CANVAS_ENABLED = "Canvas2DEnabledFromSettings";
    public static final String SEG_SETTINGS_WEBGL_CANVAS_DISABLED = "Canvas2DDisabledFromSettings";

    public static final String SEG_SETTINGS_TEXTUREREDUCER = "TextureReducerFromSettings";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_DISABLED = "DisabledFromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_64 = "Above64FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_128 = "Above128FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_256 = "Above256FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_512 = "Above512FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_1024 = "Above1024FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_2048 = "Above2048FromTextureReducer";
    public static final String SEG_SETTINGS_TEXTUREREDUCER_BACK = "BackFromTextureReducer";

    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL = "SuperSamplingLevelFromSettings";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_DISABLED = "DisabledFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_015 = "0.15xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_150 = "1.5xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_200 = "2.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_250 = "2.5xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_300 = "3.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_400 = "4.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_500 = "5.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_600 = "6.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_700 = "7.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_800 = "8.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_900 = "9.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_100 = "10.0xFromSuperSamplingLevel";
    public static final String SEG_SETTINGS_SUPERSAMPLING_LEVEL_BACK = "BackFromSuperSamplingLevel";

    public static final String SEG_SETTINGS_PATHRENDER_QUALITY = "PathRenderQualityFromSettings";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_FASTEST = "FastestFromPathRenderQuality";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_FAST = "FastFromPathRenderQuality";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_DEFAULT = "DefaultFromPathRenderQuality";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_PREFER = "PreferQualityFromPathRenderQuality";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_BEST = "BestQualityFromPathRenderQuality";
    public static final String SEG_SETTINGS_PATHRENDER_QUALITY_BACK = "BackFromPathRenderQuality";

    public static final String SEG_SETTINGS_ACCOUNT = "AccountFromSettings";
    public static final String SEG_SETTINGS_ACCOUNT_LOGOUT = "LogoutFromSettings";

}
