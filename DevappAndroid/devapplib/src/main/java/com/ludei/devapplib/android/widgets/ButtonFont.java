package com.ludei.devapplib.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.utils.FontUtils;

public class ButtonFont extends Button {

	public ButtonFont(Context context) {
        super(context);
    }

    public ButtonFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public ButtonFont(Context context, AttributeSet attrs, int defStyle) {
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
