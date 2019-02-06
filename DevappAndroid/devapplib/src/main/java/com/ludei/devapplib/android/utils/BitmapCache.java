package com.ludei.devapplib.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by imanolmartin on 25/07/14.
 */
public class BitmapCache extends DiskBasedCache implements ImageLoader.ImageCache {

    public BitmapCache(File rootDirectory) {
        super(rootDirectory);
    }

    private static final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(100);

    private class BitmapCacheEntry {

        private String url;
        private Bitmap bitmap;

        public BitmapCacheEntry(String url, Bitmap bitmap) {
            this.url = url;
            this.bitmap = bitmap;
        }
    }

    @Override
    public Bitmap getBitmap(String url) {
        url = filterUrl(url);

        // First we check the LRU cache so we can handle the query in a fast way
        Bitmap bitmap = mCache.get(url);
        if (bitmap != null)
            return bitmap;

        Entry a = get(url);
        if (a != null)
            return BitmapFactory.decodeByteArray(a.data, 0, a.data.length);

        return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        url = filterUrl(url);

        mCache.put(url, bitmap);

        new AsyncTask<BitmapCacheEntry, Void, Void>() {

            @Override
            protected Void doInBackground(BitmapCacheEntry... params) {
                BitmapCacheEntry entry = params[0];
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    entry.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] data = stream.toByteArray();

                    Entry cacheEntry = new Entry();
                    cacheEntry.data = data;
                    put(entry.url, cacheEntry);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute(new BitmapCacheEntry(url, bitmap));
    }

    private String filterUrl(String url) {
        int start = url.indexOf("http");

        return url.substring(start);
    }

}
