package com.ludei.devapplib.android.widgets.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.utils.FontUtils;

/**
 * Created by imanolmartin on 05/09/14.
 */
@TargetApi(14)
public class CustomSwitchPreference extends SwitchPreference {

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(Color.GRAY);
        titleView.setTypeface(FontUtils.getFont(getContext(), getContext().getString(R.string.app_font_bold)));

        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setTextColor(Color.GRAY);
        summaryView.setTypeface(FontUtils.getFont(getContext(), getContext().getString(R.string.app_font)));
    }
}
