package com.ludei.devapplib.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.fragments.DocumentsFragment;
import com.ludei.devapplib.android.fragments.ResourcesFragment;
import com.ludei.devapplib.android.utils.FavoritesCache;
import com.ludei.devapplib.android.utils.UriHelper;

/**
 * Created by imanolmartin on 11/09/14.
 */
public abstract class ResourcesCursorAdapter extends CursorAdapter {

    public static final int RESOURCE_URI_COLUMN_POS = 1;

    private Context context;
    private int mSelectedIndex;
    private View mSelectedView;
    private int mPreviousSelectedIndex;
    private View mPreviousView;

    public ResourcesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        this.context = context;

        mSelectedIndex = -1;
        mPreviousSelectedIndex = mSelectedIndex;
        mSelectedView = null;
        mPreviousView = null;
    }

    public ResourcesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        this.context = context;

        mSelectedIndex = -1;
        mPreviousSelectedIndex = mSelectedIndex;
        mSelectedView = null;
        mPreviousView = null;
    }

    public void setSelectedIndex(int ind, View view) {
        mPreviousSelectedIndex = mSelectedIndex;
        mPreviousView = mSelectedView;
        mSelectedIndex = ind;
        mSelectedView = view;

        if (mSelectedIndex != -1 && mSelectedIndex != mPreviousSelectedIndex) {
            closeItem(mPreviousView);

        } else {
            Uri uri = (Uri) mSelectedView.getTag();
            if (uri != null && UriHelper.isLaunchable(uri))
                switchItem(mSelectedView);
        }

//        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);

        for (int i=0; i<((ViewGroup)v).getChildCount(); i++) {
            View child = ((ViewGroup)v).getChildAt(i);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                if (mSelectedIndex != -1 && position == mSelectedIndex) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.list_item_selected_color));
                    child.setSelected(true);

                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.background));
                    child.setSelected(false);
                }
            }
            child.clearAnimation();
        }

        View frontView = v.findViewById(R.id.front);
        frontView.setTag(Boolean.valueOf(false));

        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(RESOURCE_URI_COLUMN_POS))));

        TextView nameView = (TextView) view.findViewById(R.id.item_file_name);
        nameView.setText(uri.toString());

        ImageView icon = (ImageView) view.findViewById(R.id.item_file_icon);
        if (UriHelper.isDirectory(uri)) {
            if (UriHelper.isLaunchable(uri))
                icon.setImageResource(R.drawable.ic_exec_folder);
            else
                icon.setImageResource(R.drawable.ic_folder);

        } else if (UriHelper.isUrl(uri)) {
            icon.setImageResource(R.drawable.ic_url);

        } else {
            if (UriHelper.getExtension(uri).equalsIgnoreCase(UriHelper.LAUNCHABLE_EXTENSIONS[0])) {
                icon.setImageResource(R.drawable.ic_zip);

            } else if (UriHelper.getExtension(uri).equalsIgnoreCase(UriHelper.HTML_EXT) ||
                    UriHelper.getExtension(uri).equalsIgnoreCase(UriHelper.HTM_EXT) ||
                    UriHelper.getExtension(uri).equalsIgnoreCase(UriHelper.JS_EXT)) {
                icon.setImageResource(R.drawable.ic_html);
            }
        }

        final ImageView addFav = (ImageView) view.findViewById(R.id.add_fav);
        addFav.setTag(uri);
        addFav.setEnabled(false);
        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri item = (Uri) v.getTag();

                Intent intent = new Intent(DocumentsFragment.TAG);
                intent.setAction(ResourcesFragment.ACTION_ADD_FAV);
                intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, item.getPath());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        final ImageView removeFav = (ImageView) view.findViewById(R.id.remove_fav);
        removeFav.setTag(uri);
        removeFav.setEnabled(false);
        removeFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri item = (Uri) v.getTag();

                Intent intent = new Intent(DocumentsFragment.TAG);
                intent.setAction(ResourcesFragment.ACTION_REMOVE_FAV);
                intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, item.getPath());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        final ImageView trash = (ImageView) view.findViewById(R.id.thrash);
        trash.setTag(uri);
        trash.setEnabled(false);
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri item = (Uri) v.getTag();

                Intent intent = new Intent(DocumentsFragment.TAG);
                intent.setAction(ResourcesFragment.ACTION_TRASH);
                intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, item.getPath());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        ImageView fav = (ImageView) view.findViewById(R.id.item_fav_button);
        if (FavoritesCache.getInstance(context).isFavorite(uri)) {
            fav.setVisibility(View.VISIBLE);
            addFav.setVisibility(View.GONE);
            removeFav.setVisibility(View.VISIBLE);

        } else {
            fav.setVisibility(View.GONE);
            addFav.setVisibility(View.VISIBLE);
            removeFav.setVisibility(View.GONE);
        }

        view.setTag(uri);
    }

    private void openItem(View view) {
        if (view != null) {
            View frontView = view.findViewById(R.id.front);
            View disclosure = frontView.findViewById(R.id.item_disclosure_button);
            View add_fav = view.findViewById(R.id.add_fav);
            View remove_fav = view.findViewById(R.id.remove_fav);
            View thrash = view.findViewById(R.id.thrash);
            View rename = view.findViewById(R.id.rename);

            if (frontView.getTag() == null || !(Boolean)frontView.getTag()) {
                Animation slideOutLeft = AnimationUtils.loadAnimation(context, R.anim.list_item_slide_out_left);
                if (disclosure != null) disclosure.setClickable(false);
                if (add_fav != null) add_fav.setEnabled(true);
                if (remove_fav != null) remove_fav.setEnabled(true);
                if (thrash != null) thrash.setEnabled(true);
                if (rename != null) rename.setEnabled(true);

                frontView.startAnimation(slideOutLeft);
                frontView.setTag(Boolean.valueOf(true));

                Uri item = (Uri) view.getTag();

                Intent intent = new Intent(ResourcesFragment.TAG);
                intent.setAction(ResourcesFragment.ACTION_SWIPE);
                intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, item.getPath());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }

    private void closeItem(View view) {
        if (view != null) {
            View frontView = view.findViewById(R.id.front);
            View disclosure = frontView.findViewById(R.id.item_disclosure_button);
            View add_fav = view.findViewById(R.id.add_fav);
            View remove_fav = view.findViewById(R.id.remove_fav);
            View thrash = view.findViewById(R.id.thrash);
            View rename = view.findViewById(R.id.rename);

            if (frontView.getTag() == null || (Boolean)frontView.getTag()) {
                Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.list_item_slide_in_right);
                if (disclosure != null) disclosure.setClickable(true);
                if (add_fav != null) add_fav.setEnabled(false);
                if (remove_fav != null) remove_fav.setEnabled(false);
                if (thrash != null) thrash.setEnabled(false);
                if (rename != null) rename.setEnabled(false);

                frontView.startAnimation(slideInRight);
                frontView.setTag(Boolean.valueOf(false));
            }
        }
    }

    private void switchItem(View view) {
        if (view != null) {
            final View frontView = view.findViewById(R.id.front);
            if (frontView.getTag() == null || (Boolean) frontView.getTag())
                closeItem(view);
            else
                openItem(view);
        }
    }

}
