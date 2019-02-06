package com.ludei.devapplib.android.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ludei.devapplib.android.providers.FavoritesContentProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by imanolmartin on 15/09/14.
 */
public class FavoritesCache {

    private static FavoritesCache mInstance;

    private Set<Uri> mCache;
    private Context mContext;

    public static FavoritesCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FavoritesCache(context);
        }

        return mInstance;
    }

    private FavoritesCache(Context context) {
        mContext = context;
        mCache = new HashSet<Uri>();

        refresh();
    }

    public boolean isFavorite(Uri uri) {
        for (Iterator<Uri> it = mCache.iterator(); it.hasNext();) {
            Uri item = it.next();
            if (uri.equals(item))
                return true;
        }

        return false;
    }

    public void refresh() {
        Cursor cursor = mContext.getContentResolver().query(
                FavoritesContentProvider.FAVORITES_URI,
                new String[] {
                        FavoritesContentProvider.FAVORITES_URI_COLUMN
                },
                null,
                null,
                null);

        mCache.clear();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uri = cursor.getString(0);
            mCache.add(Uri.parse(uri));
        }
    }

}
