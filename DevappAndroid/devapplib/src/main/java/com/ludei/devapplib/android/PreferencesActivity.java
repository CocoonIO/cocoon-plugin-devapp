package com.ludei.devapplib.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.segment.analytics.Analytics;

public class PreferencesActivity extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar, null);
        TextView title = (TextView)v.findViewById(R.id.title);
        title.setText(getString(R.string.title_settings).toUpperCase());
        ImageView icon = (ImageView)v.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_settings);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2cd4d7")));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(v);
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Analytics.with(this).track(CommonConsts.SEG_SETTINGS_BACK_TO_MENU);
    }
	
}

