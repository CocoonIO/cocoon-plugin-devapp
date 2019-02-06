package com.ludei.devapplib.android.auth.cocoon;

import com.google.gson.Gson;

/**
 * Created by imanolmartin on 04/11/15.
 */
public class AddAccountResponse extends Response {

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String LASTNAME = "lastname";

    public class Response {
        public String id;
        public String username;
        public String email;
        public String name;
        public String lastname;
    }

    private Response mResponse;
    private String mAccessToken;

    AddAccountResponse(int status, String msg, String response, String accessToken) {
        super(status, msg);

        mAccessToken = accessToken;

        if (response != null)
            mResponse = new Gson().fromJson(response, Response.class);
    }

    public String getId() {
        return mResponse.id;
    }

    public String getUsername() {
        return mResponse.username;
    }

    public String getEmail() {
        return mResponse.email;
    }

    public String getName() {
        return mResponse.name;
    }

    public String getLastname() {
        return mResponse.lastname;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

}
