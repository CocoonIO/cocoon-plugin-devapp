package com.ludei.devapplib.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to expose the {@link com.ludei.devapplib.android.sync.DemosSyncAdapter} functionality.
 */
public class DemosSyncService extends Service {

	private static final Object sSyncAdapterLock = new Object();
	private static DemosSyncAdapter sSyncAdapter = null;

	@Override
	public void onCreate() {
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new DemosSyncAdapter(getApplicationContext(), false);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sSyncAdapter.getSyncAdapterBinder();
	}
}
