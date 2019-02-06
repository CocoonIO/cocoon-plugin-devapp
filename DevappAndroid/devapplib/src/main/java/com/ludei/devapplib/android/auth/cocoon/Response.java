package com.ludei.devapplib.android.auth.cocoon;

/**
 * Created by imanolmartin on 04/11/15.
 */
public abstract class Response {

    Response(int status, String msg) {
        mStatus = status;
        mMsg = msg;
    }

    private int mStatus;
    private String mMsg;

    public int getStatus() {
        return mStatus;
    }

    public String getMsg() {
        return mMsg;
    }
}
