package com.ludei.devapplib.android.models;

import android.content.ContentValues;

import com.ludei.devapplib.android.providers.DemosContentProvider;

public class Demo {
	
	public String name;
	public String description;
	public String image_url;
	public String github_url;
    public boolean web;
    public String orientation;
    public String environment;

	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DemosContentProvider.DEMO_NAME_COLUMN, name);
		cv.put(DemosContentProvider.DEMO_DESCRIPTION_COLUMN, description);
		cv.put(DemosContentProvider.DEMO_IMAGE_URL_COLUMN, image_url);
		cv.put(DemosContentProvider.DEMO_GITHUB_URL_COLUMN, github_url);
		cv.put(DemosContentProvider.DEMO_WEB_COLUMN, web);
		cv.put(DemosContentProvider.DEMO_ORIENTATION_COLUMN, orientation);
		cv.put(DemosContentProvider.DEMO_ENVIRONMENT_COLUMN, environment);
		
        return cv;
	}
	
}
