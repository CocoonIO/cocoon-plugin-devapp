package com.ludei.devapplib.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.auth.cocoon.AddAccountResponse;
import com.ludei.devapplib.android.auth.cocoon.AddAccountTask;
import com.ludei.devapplib.android.auth.cocoon.AddAccountTaskListener;
import com.ludei.devapplib.android.auth.cocoon.OAuthTask;
import com.ludei.devapplib.android.auth.cocoon.OAuthTaskListener;
import com.ludei.devapplib.android.auth.cocoon.OAuthTaskResponse;
import com.ludei.devapplib.android.utils.SystemUtils;

import java.util.HashMap;

/**
 * Activity for account creation and login.
 */
public class CocoonAccountAuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "account_type";
    public final static String ARG_AUTH_TYPE = "auth_type";
    public final static String ARG_ACCOUNT_NAME = "account_name";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "is_adding_account";

	private AccountManager mAccountManager;
    private TextView mTitle;
    private WebView mWebview;
    private ProgressBar mAccountProgress;
    
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar, null);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2cd4d7")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(v);

        View actionBar = getSupportActionBar().getCustomView();
        ImageView icon = (ImageView)actionBar.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_main);
        mTitle = (TextView)actionBar.findViewById(R.id.title);
        mTitle.setText(getString(R.string.title_account_login));
		
		mAccountManager = AccountManager.get(this);
		setTitle(getString(R.string.accountTitle));

        setContentView(R.layout.activity_authenticator);

        mWebview = (WebView) findViewById(R.id.webView);
        mWebview.setWebViewClient(webviewClient);
        mWebview.loadUrl(CommonConsts.COCOON_OAUTH_LOGIN_URL);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        mAccountProgress = (ProgressBar)findViewById(R.id.account_progress);
        mAccountProgress.setVisibility(View.GONE);
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mAccountProgress.setVisibility(View.GONE);
    }

    private WebViewClient webviewClient = new WebViewClient() {

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mAccountProgress.setVisibility(View.VISIBLE);
        }

        public void onPageFinished(WebView view, String url) {
            mAccountProgress.setVisibility(View.GONE);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            CocoonAccountAuthenticatorActivity.this.mWebview.setVisibility(View.INVISIBLE);
            SystemUtils.showAlert(
                    CocoonAccountAuthenticatorActivity.this,
                    "Authentication error",
                    "Sorry there was an authentication error. Please check your internet connection and tray again later.",
                    true);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView  view, String  url){
            if( url.startsWith("http://cocoon.io") ||
                    url.startsWith("https://cocoon.io")) {
                return true;

            } else if(url.startsWith(CommonConsts.LAUNCHER_OAUTH_REDIRECT_URI)) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }

                mAccountProgress.setVisibility(View.VISIBLE);

                Uri uri = Uri.parse(url);
                String code = uri.getQueryParameter("code");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(OAuthTask.CODE, code);
                new OAuthTask(code, oauthListener).execute();

                return true;
            }

            return false;
        }
    };

    private OAuthTaskListener oauthListener = new OAuthTaskListener() {

        @Override
        public void onOAuthTaskSuccess(OAuthTask task, OAuthTaskResponse response) {
            new AddAccountTask(response.getAccessToken(), accountTaskListener).execute();
        }

        @Override
        public void onOAuthTaskError(OAuthTask task, int status, String msg) {
            mAccountProgress.setVisibility(View.GONE);
            SystemUtils.showAlert(CocoonAccountAuthenticatorActivity.this, "Authentication error", "OAuth error: " + msg, false);
        }
    };

    private AddAccountTaskListener accountTaskListener = new AddAccountTaskListener() {

        @Override
        public void AddCocoonAccountSuccess(AddAccountTask task, AddAccountResponse response) {
            mAccountProgress.setVisibility(View.GONE);

            final Account account = new Account(response.getEmail(), CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE);

            if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                final Bundle extraData = new Bundle();
                extraData.putString(AddAccountResponse.ID, response.getId());
                extraData.putString(AddAccountResponse.USERNAME, response.getUsername());
                extraData.putString(AddAccountResponse.EMAIL, response.getEmail());
                extraData.putString(AddAccountResponse.NAME, response.getName());
                extraData.putString(AddAccountResponse.LASTNAME, response.getLastname());
                mAccountManager.addAccountExplicitly(account, null, extraData);
                mAccountManager.setAuthToken(account, CocoonAccountAuthenticatorService.COCOON_AUTHTOKEN_TYPE, response.getAccessToken());

            } else {
                mAccountManager.setPassword(account, null);
            }

            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, CocoonAccountAuthenticatorService.COCOON_ACCOUNT_TYPE);
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void AddCocoonAccountError(AddAccountTask task, int status, String msg) {
            mAccountProgress.setVisibility(View.GONE);
            SystemUtils.showAlert(CocoonAccountAuthenticatorActivity.this, "Authentication error", "OAuth error: " + msg, false);
        }
    };

}
