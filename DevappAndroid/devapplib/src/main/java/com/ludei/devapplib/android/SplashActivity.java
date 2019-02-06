package com.ludei.devapplib.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

	private Handler mHandler = new Handler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splash);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				SplashActivity.this.finish();
			}
			
		}, 1500);
		
	}
	
}
