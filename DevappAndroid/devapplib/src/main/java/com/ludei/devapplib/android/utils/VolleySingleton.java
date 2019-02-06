package com.ludei.devapplib.android.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.File;

public class VolleySingleton {

	private static VolleySingleton mInstance = null;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private VolleySingleton(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
        File cacheDir = new File(context.getFilesDir() + File.pathSeparator + "cache");
        BitmapCache cache = new BitmapCache(cacheDir);
		mImageLoader = new ImageLoader(this.mRequestQueue, cache);
	}

	public static VolleySingleton getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VolleySingleton(context);
		}
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		return this.mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		return this.mImageLoader;
	}

}