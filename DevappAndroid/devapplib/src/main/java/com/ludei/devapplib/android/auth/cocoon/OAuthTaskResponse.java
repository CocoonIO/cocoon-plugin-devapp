package com.ludei.devapplib.android.auth.cocoon;

import com.google.gson.Gson;

/**
 * Created by imanolmartin on 21/03/14.
 */
public class OAuthTaskResponse extends Response {

    public class Response {
        public String token_type;
        public int expires_in;
        public String refresh_token;
        public String access_token;
    }

    protected Response mResponse;

    OAuthTaskResponse(int status, String msg, String response) {
        super(status, msg);

        if (response != null)
            mResponse = new Gson().fromJson(response, Response.class);
    }

    public String getTokenType() {
        return mResponse.token_type;
    }

    public int getExpiresIn() {
        return mResponse.expires_in;
    }

    public String getRefreshToken() {
        return mResponse.refresh_token;
    }
    public String getAccessToken() {
        return mResponse.access_token;
    }

}
