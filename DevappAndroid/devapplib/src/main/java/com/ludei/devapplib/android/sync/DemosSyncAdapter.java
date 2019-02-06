package com.ludei.devapplib.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * {@link android.content.AbstractThreadedSyncAdapter} simple implementation. Used for training.
 */
public class DemosSyncAdapter extends AbstractThreadedSyncAdapter {
	
	private DemosSyncHelper mSyncHelper;
	
	public DemosSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        if(mSyncHelper == null){
        	mSyncHelper = new DemosSyncHelper(context);
        }
    }
	
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		mSyncHelper.performSync(syncResult, account);
	}
}
