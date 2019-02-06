package com.ludei.devapplib.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.ludei.devapplib.android.fragments.BaseFragment;
import com.ludei.devapplib.android.fragments.MainFragment;
import com.ludei.devapplib.android.fragments.YourAppFragment;
import com.ludei.devapplib.android.utils.SystemUtils;
import com.segment.analytics.Analytics;
import com.segment.analytics.internal.Utils;

public class DevAppActivity extends ActionBarActivity {

    public static final String MAIN_TAG = "main";

    private static Analytics mAnalytics = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2cd4d7")));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar, null);

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(v);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment mainFragment = fragmentManager.findFragmentByTag(MAIN_TAG);

        if (mainFragment == null) {
            Fragment fragment = new YourAppFragment();

            String isFullString = Utils.getResourceString(this, "devapp_full");
            if (isFullString != null) {
                boolean isFull = Boolean.parseBoolean(isFullString);
                if (isFull) {
                    fragment = new MainFragment();
                    startActivity(new Intent(this, SplashActivity.class));
                }
            }

            fragmentManager.beginTransaction()
                    .add(R.id.content_frame, fragment, MAIN_TAG)
                    .commit();
        }

        if (DevAppActivity.mAnalytics == null) {
            DevAppActivity.mAnalytics = new Analytics.Builder(this, Consts.ANALYTICS_WRITE_KEY).build();
            Analytics.setSingletonInstance(DevAppActivity.mAnalytics);
            DevAppActivity.mAnalytics.track(CommonConsts.SEG_LAUNCH);
        }
	}

    @Override
    public void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        return(super.onCreateOptionsMenu(menu));
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {       
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public void onBackPressed() {
        BaseFragment fragment = getActiveFragment();
        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed();

        }
	}

    public BaseFragment getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CommonConsts.REQUEST_CODE_ASK_PERMISSIONS_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "READ_EXTERNAL_STORAGE permission denied, documents access will be disabled", Toast.LENGTH_SHORT).show();
            }

            new AlertDialog.Builder(this)
                    .setTitle("Restart")
                    .setMessage("The Developer App needs to be restarted for the new permissions to take effect.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SystemUtils.doRestart(DevAppActivity.this);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
	
}
