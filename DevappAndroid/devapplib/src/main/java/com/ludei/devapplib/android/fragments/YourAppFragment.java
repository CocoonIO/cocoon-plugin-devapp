package com.ludei.devapplib.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.utils.FavoritesCache;
import com.segment.analytics.Analytics;

/**
 * Created by imanolmartin on 03/09/14.
 */
public class YourAppFragment extends BaseFragment {

    public static final String TAG = YourAppFragment.class.getSimpleName();

    private static final int NUM_ITEMS = 3;

    private static final int URL = 0;
    private static final int DOCUMENTS = 1;
    private static final int FAVORITES = 2;

    private class Tab {
        ImageView mImage;
        TextView mText;

        public Tab(ImageView image, TextView text) {
            mImage = image;
            mText = text;
        }

        public void setSelected(boolean selected) {
            if (mImage != null) mImage.setSelected(selected);
            if (mText != null) mText.setSelected(selected);
        }

    }

    private ViewPager mViewPager;
    private ResourcesSectionPagerAdapter mResourcesSectionsPagerAdapter;
    private Tab[] tabs = new Tab[NUM_ITEMS];
    private ActionBar mActionBar;
    private int mCurrentTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (container == null) {
            return null;
        }

        return inflater.inflate(R.layout.fragment_yourapp, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        mResourcesSectionsPagerAdapter = new ResourcesSectionPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) getView().findViewById(R.id.pager);
        mViewPager.setAdapter(mResourcesSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUM_ITEMS);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position != mCurrentTab) {
                    switch (mCurrentTab) {
                        case URL:
                            Analytics.with(getActivity()).track(CommonConsts.SEG_URL_VIEW);
                            Analytics.with(getActivity()).screen(CommonConsts.SEG_URL_VIEW, CommonConsts.SEG_URL_VIEW);
                            break;

                        case DOCUMENTS:
                            Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_VIEW);
                            Analytics.with(getActivity()).screen(CommonConsts.SEG_DOC_VIEW, CommonConsts.SEG_DOC_VIEW);
                            break;

                        case FAVORITES:
                            Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_VIEW);
                            Analytics.with(getActivity()).screen(CommonConsts.SEG_FAV_VIEW, CommonConsts.SEG_FAV_VIEW);
                            break;
                    }

                    mCurrentTab = position;
                }

                FavoritesCache.getInstance(getActivity()).refresh();

                for (int i=0; i<tabs.length; i++) {
                    tabs[i].setSelected(false);
                }
                tabs[position].setSelected(true);

                TextView title = (TextView)mActionBar.getCustomView().findViewById(R.id.title);
                ImageView icon = (ImageView)mActionBar.getCustomView().findViewById(R.id.icon);
                switch (position) {
                    case URL:
                        title.setText(getString(R.string.title_url).toUpperCase());
                        icon.setImageResource(R.mipmap.actionbar_url);
                        break;

                    case DOCUMENTS:
                        title.setText(getString(R.string.title_documents).toUpperCase());
                        icon.setImageResource(R.mipmap.actionbar_docs);
                        break;

                    case FAVORITES:
                        title.setText(getString(R.string.title_favorites).toUpperCase());
                        icon.setImageResource(R.mipmap.actionbar_favs);

                        Intent intent = new Intent();
                        intent.setAction(ResourcesFragment.ACTION_REFRESH);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        break;
                }

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
        });

        ImageView image_url = (ImageView)getView().findViewById(R.id.tab_url);
        TextView text_url = (TextView)getView().findViewById(R.id.text_url);
        image_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(URL);
            }
        });
        tabs[URL] = new Tab(image_url, text_url);
        tabs[URL].setSelected(true);

        ImageView image_docs = (ImageView)getView().findViewById(R.id.tab_docs);
        TextView text_docs = (TextView)getView().findViewById(R.id.text_docs);
        image_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(DOCUMENTS);
            }
        });
        tabs[DOCUMENTS] = new Tab(image_docs, text_docs);

        ImageView image_favs = (ImageView)getView().findViewById(R.id.tab_favs);
        TextView text_favs = (TextView)getView().findViewById(R.id.text_favs);
        image_favs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(FAVORITES);
            }
        });
        tabs[FAVORITES] = new Tab(image_favs, text_favs);

        TextView title = (TextView)mActionBar.getCustomView().findViewById(R.id.title);
        title.setText(getString(R.string.title_url).toUpperCase());
        ImageView icon = (ImageView)mActionBar.getCustomView().findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.actionbar_url);

        mCurrentTab = URL;
    }

    @Override
    public void onPause() {
        super.onPause();

        saveLastTab();
    }

    @Override
    public void onResume() {
        super.onResume();

        restoreLastTab();
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class ResourcesSectionPagerAdapter extends FragmentPagerAdapter {

        public ResourcesSectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case URL:
                    return new UrlFragment();

                case DOCUMENTS:
                    return new DocumentsFragment();

                case FAVORITES:
                    return new FavoritesFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

    private void saveLastTab() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.pref_current_tab), mViewPager.getCurrentItem());
        editor.commit();
    }

    private void restoreLastTab() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int tab = prefs.getInt(getString(R.string.pref_current_tab), URL);
        mViewPager.setCurrentItem(tab);
    }
}
