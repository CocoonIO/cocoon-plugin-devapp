package com.ludei.devapplib.android.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class FontUtils {
	
	public static final String TAG = FontUtils.class.getSimpleName();

	private static final HashMap<String, WeakReference<Typeface>> fontsCache = new HashMap<String, WeakReference<Typeface>>();
	
	public static Typeface getFont(Context context, String fontname) {
		Typeface font = null;
        try {
        	if (fontsCache.containsKey(fontname) && fontsCache.get(fontname).get() != null) {
        		return fontsCache.get(fontname).get();
        		
        	} else {
	        	font = Typeface.createFromAsset(context.getAssets(), fontname); 
	        	fontsCache.put(fontname, new WeakReference<Typeface>(font));
	        	return font;
        	}
        
        } catch (Exception e) {
            Log.e(TAG, "Could not get typeface: " + e.getMessage());
        }
        
        return font;
	}
	
}
