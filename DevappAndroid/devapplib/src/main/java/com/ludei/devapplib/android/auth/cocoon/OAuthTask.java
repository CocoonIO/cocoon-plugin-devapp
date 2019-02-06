package com.ludei.devapplib.android.auth.cocoon;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.ludei.devapplib.android.CommonConsts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class OAuthTask extends AsyncTask<Void, Void, OAuthTaskResponse> {

    public static final String CODE = "code";

	protected String mCode;
	protected OAuthTaskListener mListener;
    protected Gson gson;

	public OAuthTask(String code, OAuthTaskListener listener) {
		mListener = listener;
        mCode = code;
        gson = new Gson();
	}

    @Override
    protected OAuthTaskResponse doInBackground(Void... arg0) {
        HttpURLConnection conn = null;
        OAuthTaskResponse response;

        try {
            String body = String.format("client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code&code=%s",
                    CommonConsts.LAUNCHER_OAUTH_CLIENT_ID,
                    CommonConsts.LAUNCHER_OAUTH_CLIENT_SECRET,
                    CommonConsts.LAUNCHER_OAUTH_REDIRECT_URI,
                    mCode);

            URL url = new URL(CommonConsts.COCOON_OAUTH_ACCESS_TOKEN_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(body.toString());
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseBody = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    responseBody += line;
                }

                response = new OAuthTaskResponse(
                        conn.getResponseCode(),
                        conn.getResponseMessage(),
                        responseBody
                );

            } else {
                response = new OAuthTaskResponse(
                        conn.getResponseCode(),
                        conn.getResponseMessage(),
                        null
                );
            }

        } catch (IOException e) {
            response = new OAuthTaskResponse(
                    500,
                    e.getLocalizedMessage(),
                    null
            );

        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return response;
    }

	@Override
	protected void onPostExecute(OAuthTaskResponse result) {
        if (result.getStatus() != 200) {
            mListener.onOAuthTaskError(this, result.getStatus(), result.getMsg());

        } else {
            mListener.onOAuthTaskSuccess(this, result);
        }
	}

}
