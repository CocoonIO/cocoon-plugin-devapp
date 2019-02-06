package com.ludei.devapplib.android.widgets.prefs;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.utils.FontUtils;

/**
 * Created by imanolmartin on 05/09/14.
 */
public class CustomPreferenceCategory extends PreferenceCategory {

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(Color.GRAY);
        titleView.setTypeface(FontUtils.getFont(getContext(), getContext().getString(R.string.app_font_bold)));
    }
}
