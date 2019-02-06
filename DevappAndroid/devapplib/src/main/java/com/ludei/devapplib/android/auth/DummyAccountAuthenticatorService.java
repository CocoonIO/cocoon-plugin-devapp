/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ludei.devapplib.android.auth;


import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DummyAccountAuthenticatorService extends Service {
	
    private static final String TAG = DummyAccountAuthenticatorService.class.getSimpleName();
    
    private static final String DUMMY_ACCOUNT_TYPE = "com.ludei.devapplib.android.dummy";
    public static final String DUMMY_ACCOUNT_NAME = "sync";
    
    private DummyAccountAuthenticator mAuthenticator;

    /**
     * Obtain a handle to the {@link android.accounts.Account} used for sync in this application.
     *
     * @return Handle to application's account (not guaranteed to resolve unless CreateSyncAccount()
     *         has been called)
     */
    public static Account GetAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        final String accountName = DUMMY_ACCOUNT_NAME;
        return new Account(accountName, DUMMY_ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        mAuthenticator = new DummyAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}

