package com.ludei.devapplib.android.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to expose the {@link com.ludei.devapplib.android.auth.CocoonAccountAuthenticator} functionality.
 */
public class CocoonAccountAuthenticatorService extends Service {
	
	public static final String COCOON_AUTHTOKEN_TYPE = "code";
	public static final String COCOON_ACCOUNT_TYPE = "cocoon";

    private CocoonAccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new CocoonAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
