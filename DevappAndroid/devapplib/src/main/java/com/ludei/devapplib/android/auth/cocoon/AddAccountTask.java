package com.ludei.devapplib.android.auth.cocoon;

import android.os.AsyncTask;

import com.ludei.devapplib.android.CommonConsts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by imanolmartin on 04/11/15.
 */
public class AddAccountTask extends AsyncTask<Void, Void, AddAccountResponse> {

    private String mAccessToken;
    private AddAccountTaskListener mCallback;

    public AddAccountTask(String accessToken, AddAccountTaskListener callback) {
        mAccessToken = accessToken;
        mCallback = callback;
    }

    @Override
    protected AddAccountResponse doInBackground(Void... arg0) {
        HttpURLConnection conn = null;
        AddAccountResponse response;

        try {
            URL url = new URL(CommonConsts.COCOON_ME_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", String.format("Bearer %s", mAccessToken));
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseBody = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    responseBody += line;
                }

                response = new AddAccountResponse(
                        conn.getResponseCode(),
                        conn.getResponseMessage(),
                        responseBody,
                        mAccessToken);

            } else {
                response = new AddAccountResponse(
                        conn.getResponseCode(),
                        conn.getResponseMessage(),
                        null,
                        null);
            }

        } catch (IOException e) {
            response = new AddAccountResponse(
                    500,
                    e.getLocalizedMessage(),
                    null,
                    null);

        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return response;
    }

    @Override
    protected void onPostExecute(AddAccountResponse result) {
        if (result.getStatus() != 200) {
            mCallback.AddCocoonAccountError(this, result.getStatus(), result.getMsg());

        } else {
            mCallback.AddCocoonAccountSuccess(this, result);
        }
    }

}
