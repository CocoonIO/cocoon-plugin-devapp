package com.ludei.devapplib.android.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AbsListView;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.PreferencesActivity;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.providers.DemosContentProvider;
import com.ludei.devapplib.android.sync.DemosSyncUtils;
import com.ludei.devapplib.android.utils.VolleySingleton;
import com.ludei.devapplib.android.widgets.GridFragment;
import com.segment.analytics.Analytics;

public class DemosFragment extends GridFragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener, SearchView.OnClickListener, SearchView.OnCloseListener {

	public static final String TAG = DemosFragment.class.getSimpleName();
	
	private static final int DEMOS_LOADER = 0;
	
	private SimpleCursorAdapter mAdapter;
	private Object mSyncObserverHandle;
    private SwipeRefreshLayout mSwipeLayout;
	
	private static final String[] PROJECTION = new String[] {
        	DemosContentProvider.DEMO_ID_COLUMN,
        	DemosContentProvider.DEMO_NAME_COLUMN,
        	DemosContentProvider.DEMO_DESCRIPTION_COLUMN,
        	DemosContentProvider.DEMO_IMAGE_URL_COLUMN,
            DemosContentProvider.DEMO_GITHUB_URL_COLUMN,
        	DemosContentProvider.DEMO_WEB_COLUMN,
        	DemosContentProvider.DEMO_ORIENTATION_COLUMN,
            DemosContentProvider.DEMO_ENVIRONMENT_COLUMN};
	
    private static final String[] FROM_COLUMNS = new String[]{
    		DemosContentProvider.DEMO_IMAGE_URL_COLUMN,
            DemosContentProvider.DEMO_NAME_COLUMN
    };
    
    private static final int DEMO_ID_COLUMN_POS = 0;
    private static final int DEMO_NAME_COLUMN_POS = 1;
    private static final int DEMO_DESCRIPTION_COLUMN_POS = 2;
    private static final int DEMO_IMAGE_URL_COLUMN_POS = 3;
    private static final int DEMO_GITHUB_URL_COLUMN_POS = 4;
    private static final int DEMO_WEB_COLUMN_POS = 5;
    private static final int DEMO_ORIENTATION_COLUMN_POS = 6;
    private static final int DEMO_ENGINE_COLUMN_POS = 7;

    private static final int[] TO_FIELDS = new int[]{
    		R.id.demo_thumbnail,
            R.id.demo_name};

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create account, if needed
        DemosSyncUtils.CreateSyncAccount(activity);
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (container == null) {
            return null;
        }
		
		return inflater.inflate(R.layout.fragment_demos, null);
	}

	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        if (mSwipeLayout != null) {
            mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    DemosSyncUtils.TriggerRefresh();
                }
            });
            mSwipeLayout.setColorSchemeResources(
                    R.color.blue_transparent,
                    R.color.blue,
                    R.color.highlighted_blue,
                    android.R.color.white);

            getGridView().setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) { /* Nothing to do here */ }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (DemosFragment.this.isVisible()) {
                        int topRowVerticalPosition = (getGridView() == null || getGridView().getChildCount() == 0) ? 0 : getGridView().getChildAt(0).getTop();
                        mSwipeLayout.setEnabled(topRowVerticalPosition >= 0);
                    }
                }
            });
        }

        View actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar().getCustomView();
        TextView title = (TextView)actionBar.findViewById(R.id.title);
        title.setText(getString(R.string.title_demos).toUpperCase());
        ImageView icon = (ImageView)actionBar.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_demos);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),       		// Current context
                R.layout.list_item_demo,  	// Layout for individual rows
                null,                		// Cursor
                FROM_COLUMNS,        		// Cursor columns to use
                TO_FIELDS,           		// Layout fields to use
                0                    		// No flags
        );

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getActivity().getContentResolver().query(
                        DemosContentProvider.DEMO_URI, 			                            // URI
                        PROJECTION,                							                // Projection
                        DemosContentProvider.DEMO_NAME_COLUMN + " LIKE ?",            // Selection
                        new String[]{
                                "%" + constraint.toString() + "%"
                        },                                                                  // Selection args
                        DemosContentProvider.DEMO_ID_COLUMN + " asc"                      // Order By
                );
            }
        });

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == DEMO_NAME_COLUMN_POS) {
                	String name = cursor.getString(i);
                    TextView nameView = (TextView)view.findViewById(R.id.demo_name);
                    nameView.setText(name.toUpperCase());

                    Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
                    nameView.startAnimation(bottomUp);
                    nameView.setVisibility(View.VISIBLE);

                    return true;

                } else if (i == DEMO_IMAGE_URL_COLUMN_POS) {
                	String url = cursor.getString(i);
                    ImageView imageView = (ImageView) view.findViewById(R.id.demo_thumbnail);
                    VolleySingleton.getInstance(getActivity()).getImageLoader().get(url, ImageLoader.getImageListener(imageView, R.drawable.detail_default, R.drawable.detail_default));

                    return true;

                }

                return false;
            }
        });
        setGridAdapter(mAdapter);
        getGridView().setLayoutAnimation(new GridLayoutAnimationController(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in)));
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getGridView().setNumColumns(3);
        else
            getGridView().setNumColumns(2);

        LoaderManager lm = getLoaderManager();
        if (lm.getLoader(DEMOS_LOADER) == null) {
            lm.initLoader(DEMOS_LOADER, null, this);

        } else {
            lm.restartLoader(DEMOS_LOADER, null, this);
        }

        Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS_VIEW);
        Analytics.with(getActivity()).screen(CommonConsts.SEG_DEMOS_VIEW, CommonConsts.SEG_DEMOS_VIEW);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            getGridView().setNumColumns(3);
        else
            getGridView().setNumColumns(2);

        getGridView().invalidate();
        mAdapter.notifyDataSetChanged();
    }
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle extras) {
		if (loaderId != DEMOS_LOADER)
			return null;
		
		return new CursorLoader(getActivity(),                                          // Context
                DemosContentProvider.DEMO_URI, 						                    // URI
                PROJECTION,                							                    // Projection
                null,                                                                   // Selection
                null,                                                                   // Selection args
                DemosContentProvider.DEMO_ID_COLUMN + " asc"); 	                    // Sort
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mSwipeLayout != null)
            mSwipeLayout.setRefreshing(false);

        if (loader.getId() != DEMOS_LOADER)
			return;
		
		if(mAdapter != null && cursor != null){
			mAdapter.changeCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() != DEMOS_LOADER)
			return;
		
		if(mAdapter != null)
			mAdapter.changeCursor(null);
	}

	@Override
    public void onGridItemClick(GridView listView, View view, int position, long id) {
        super.onGridItemClick(listView, view, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);
        String name = c.getString(DEMO_NAME_COLUMN_POS);
        String description = c.getString(DEMO_DESCRIPTION_COLUMN_POS);
        String imageUrl = c.getString(DEMO_IMAGE_URL_COLUMN_POS);
        String githubUrl = c.getString(DEMO_GITHUB_URL_COLUMN_POS);
        boolean isWebOnly = c.getInt(DEMO_WEB_COLUMN_POS)>0;
        String orientation = c.getString(DEMO_ORIENTATION_COLUMN_POS);
        String engine = c.getString(DEMO_ENGINE_COLUMN_POS);

        Bundle extras = new Bundle();
        extras.putString(DemosContentProvider.DEMO_NAME_COLUMN, name);
        extras.putString(DemosContentProvider.DEMO_DESCRIPTION_COLUMN, description);
        extras.putString(DemosContentProvider.DEMO_IMAGE_URL_COLUMN, imageUrl);
        extras.putString(DemosContentProvider.DEMO_GITHUB_URL_COLUMN, githubUrl);
        extras.putString(DemosContentProvider.DEMO_ORIENTATION_COLUMN, orientation);
        extras.putBoolean(DemosContentProvider.DEMO_WEB_COLUMN, isWebOnly);
        extras.putString(DemosContentProvider.DEMO_ENVIRONMENT_COLUMN, engine);
        
        Fragment fragment = new DemoDetailsFragment();
        fragment.setArguments(extras);
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
		transaction.replace(R.id.content_frame, fragment, DemoDetailsFragment.TAG);
		transaction.addToBackStack(DemoDetailsFragment.TAG);
		transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.demos, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setOnSearchClickListener(this);
            searchView.setOnCloseListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_settings) {
            getActivity().startActivity(new Intent(getActivity(), PreferencesActivity.class));
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS_SEARCH_ENTER);

        mAdapter.getFilter().filter(s);
        mAdapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mAdapter.getFilter().filter(s);
        mAdapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void onClick(View view) {
        Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS_SEARCH);
    }

    @Override
    public boolean onClose() {
        Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS_SEARCH_CANCEL);
        return false;
    }
}
