package com.ludei.devapplib.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.utils.FontUtils;

public class TextViewFont extends TextView {
	
    public TextViewFont(Context context) {
        super(context);
    }

    public TextViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public TextViewFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomWidget);
        String customFont = a.getString(R.styleable.CustomWidget_customFont);
        Typeface font = FontUtils.getFont(getContext(), customFont);
        if (font != null)
        	setTypeface(font);
        a.recycle();
    }

}