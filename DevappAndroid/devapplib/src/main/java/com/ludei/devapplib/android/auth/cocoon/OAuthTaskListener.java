package com.ludei.devapplib.android.auth.cocoon;

/**
 * Created by imanolmartin on 21/03/14.
 */
public interface OAuthTaskListener {

    void onOAuthTaskSuccess(OAuthTask task, OAuthTaskResponse response);
    void onOAuthTaskError(OAuthTask task, int status, String msg);
}
