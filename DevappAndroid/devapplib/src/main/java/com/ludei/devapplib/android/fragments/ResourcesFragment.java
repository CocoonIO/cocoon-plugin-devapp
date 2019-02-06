package com.ludei.devapplib.android.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.PreferencesActivity;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.adapters.ResourcesCursorAdapter;
import com.ludei.devapplib.android.auth.AccountUtils;
import com.ludei.devapplib.android.providers.FavoritesContentProvider;
import com.ludei.devapplib.android.providers.HistoryContentProvider;
import com.ludei.devapplib.android.utils.FavoritesCache;
import com.ludei.devapplib.android.utils.LauncherUtils;
import com.ludei.devapplib.android.utils.SystemUtils;
import com.ludei.devapplib.android.utils.UriHelper;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ResourcesFragment extends LauncherFragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = ResourcesFragment.class.getSimpleName();

    public static final String BUNDLE_ORDER_KEY = "order";

    public static final String ACTION_ADD_FAV = ResourcesFragment.class.getSimpleName() + ".ACTION_ADD_FAV";
    public static final String ACTION_REMOVE_FAV = ResourcesFragment.class.getSimpleName() + ".ACTION_REMOVE_FAV";
    public static final String ACTION_TRASH = ResourcesFragment.class.getSimpleName() + ".ACTION_TRASH";
    public static final String ACTION_RENAME = ResourcesFragment.class.getSimpleName() + ".ACTION_RENAME";
    public static final String ACTION_REFRESH = ResourcesFragment.class.getSimpleName() + ".ACTION_REFRESH";
    public static final String ACTION_SWIPE = ResourcesFragment.class.getSimpleName() + ".ACTION_SWIPE";

    protected static final int LOADER = 0;

    private View mLaunchButtons;
    private View mUrlButtons;
    private View mMultiButtons;
    private View mListEmptyView;
    private View mListProgressView;
    private Button cancelButton;
    private ListView mItemsList;
    protected EditText mUrlEdit;
    private int mSelectedItemPosition = -1;
    private ResourcesCursorAdapter mAdapter = null;
    private SwipeRefreshLayout mSwipeLayout;
    private SortOrder mCurrentOrder = SortOrder.NAME_ASC;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();

            mSwipeLayout.setRefreshing(false);
        }
    };
    private LocalBroadcastManager localBroadcastManager = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!getUserVisibleHint())
                return;

            String action = intent.getAction();
            if (action.equalsIgnoreCase(ACTION_ADD_FAV)) {
                hideLaunchButtons();
                addSelectedFavorites();

                if (ResourcesFragment.this.getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SWIPE_FAV);

                } else if (ResourcesFragment.this.getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_SWIPE_FAV);

                } else if (ResourcesFragment.this.getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_SWIPE_FAV);
                }

            } else if (action.equalsIgnoreCase(ACTION_REMOVE_FAV)) {
                hideLaunchButtons();
                removeSelectedFavorites();

            } else if (action.equalsIgnoreCase(ACTION_TRASH)) {
                hideLaunchButtons();
                deleteSelectedItems();

                if (ResourcesFragment.this.getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SWIPE_DEL);

                } else if (ResourcesFragment.this.getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_SWIPE_DEL);

                } else if (ResourcesFragment.this.getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_SWIPE_DEL);
                }

            } else if (action.equalsIgnoreCase(ACTION_RENAME)) {
                hideLaunchButtons();
                renameSelectedItems();

            } else if (action.equalsIgnoreCase(ACTION_REFRESH)) {
                reloadList();

            } else if (action.equalsIgnoreCase(ACTION_SWIPE)) {
                if (ResourcesFragment.this.getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SWIPE);

                } else if (ResourcesFragment.this.getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_SWIPE);

                } else if (ResourcesFragment.this.getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_SWIPE);
                }
            }
        }
    };

    protected enum SortOrder {
        NAME_ASC,
        NAME_DSC,
        SIZE_ASC,
        SIZE_DSC,
        DATE_ASC,
        DATE_DSC
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        IntentFilter filter = new IntentFilter(TAG);
        filter.addAction(ACTION_ADD_FAV);
        filter.addAction(ACTION_REMOVE_FAV);
        filter.addAction(ACTION_TRASH);
        filter.addAction(ACTION_RENAME);
        filter.addAction(ACTION_REFRESH);
        filter.addAction(ACTION_SWIPE);
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(mReceiver, filter);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (container == null) {
            return null;
        }

        return inflater.inflate(R.layout.fragment_resources, null);
	}

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        ViewGroup urlLayout = (ViewGroup) getView().findViewById(R.id.url_edit_layout);
        urlLayout.setVisibility(View.GONE);

        mUrlEdit = (EditText) getView().findViewById(R.id.url_edit);
        mUrlEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (UriHelper.isLaunchable(Uri.parse(mUrlEdit.getText().toString())))
                        showLaunchButtons();
                    else
                        hideLaunchButtons();
                    showUrlButtons();

                } else {
                    hideLaunchButtons();
                    hideUrlButtons();
                }
            }
        });
        mUrlEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (UriHelper.isLaunchable(Uri.parse(mUrlEdit.getText().toString())))
                    showLaunchButtons();
                else
                    hideLaunchButtons();
                showUrlButtons();

                return false;
            }
        });
        mUrlEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);

                    return true;
                }

                return false;
            }
        });
        mUrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (UriHelper.isLaunchable(Uri.parse(mUrlEdit.getText().toString())))
                    showLaunchButtons();
                else
                    hideLaunchButtons();

                mLaunchUri = Uri.parse(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mUrlButtons = getView().findViewById(R.id.url_buttons_layout);
        Button qrCodeButton = (Button) getView().findViewById(R.id.qrcode_button);
        qrCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SCAN_QR);

                IntentIntegratorSupportV4 intentIntegrator = new IntentIntegratorSupportV4(ResourcesFragment.this);
                intentIntegrator.initiateScan();
            }
        });
        Button cancelCodeButton = (Button) getView().findViewById(R.id.cancel_button);
        cancelCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SEARCH_CANCEL);

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);

                ViewGroup urlButtons = (ViewGroup) getView().findViewById(R.id.url_buttons_layout);
                if (urlButtons.getVisibility() != View.GONE) {
                    Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_up);
                    urlButtons.startAnimation(bottomUp);
                    urlButtons.setVisibility(View.GONE);
                    mItemsList.requestFocus();
                }
            }
        });

        mLaunchButtons = getView().findViewById(R.id.launch_buttons_layout);
        mLaunchButtons.setId(generateViewId());
        Button mWebviewButton = (Button) getView().findViewById(R.id.webview_button);
		mWebviewButton.setOnClickListener(webviewClickListener);
        Button mAcceleratedWebviewButton = (Button) getView().findViewById(R.id.webviewplus_button);
        mAcceleratedWebviewButton.setOnClickListener(webviewPlusClickListener);
        mAcceleratedWebviewButton.setVisibility(LauncherUtils.isEngineAvailable(LauncherUtils.Engines.WEBVIEW_PLUS) ? View.VISIBLE : View.GONE);
        Button mCocoonjsButton = (Button) getView().findViewById(R.id.canvasplus_button);
		mCocoonjsButton.setOnClickListener(canvasplusClickListener);
        mCocoonjsButton.setVisibility(LauncherUtils.isEngineAvailable(LauncherUtils.Engines.CANVAS_PLUS) ? View.VISIBLE : View.GONE);

        mMultiButtons = getView().findViewById(R.id.multi_buttons_layout);
        mMultiButtons.setId(generateViewId());
        cancelButton = (Button) getView().findViewById(R.id.multi_cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                mItemsList.setItemChecked(mSelectedItemPosition, true);
                hideMultiButtons();
            }
        });
        ImageButton addFavoriteButton = (ImageButton) getView().findViewById(R.id.add_fav_button);
        addFavoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLaunchButtons();
                addSelectedFavorites();
            }
        });
        ImageButton deleteFavoriteButton = (ImageButton) getView().findViewById(R.id.delete_fav_button);
        deleteFavoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLaunchButtons();
                removeSelectedFavorites();
            }
        });
        ImageButton deleteButton = (ImageButton) getView().findViewById(R.id.delete_item_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLaunchButtons();
                deleteSelectedItems();
            }
        });

        mAdapter = createAdapter();

		mItemsList = (ListView)getView().findViewById(R.id.items_list);
        mItemsList.requestFocus();
		mItemsList.setAdapter(mAdapter);
        mItemsList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideLaunchButtons();
                    hideUrlButtons();
                }
            }
        });
        mItemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedItemPosition = position;
                mAdapter.setSelectedIndex(mSelectedItemPosition, view);

                hideUrlButtons();

                // Check if the search view is opened
                SearchView searchView = (SearchView)getActivity().findViewById(R.id.search);
                if (!searchView.isIconified()) {
                    if (ResourcesFragment.this.getClass() == UrlFragment.class) {
                        Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SEARCH_HISTORY);

                    } else if (ResourcesFragment.this.getClass() == DocumentsFragment.class) {
                        Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SEARCH_DOC);

                    } else if (ResourcesFragment.this.getClass() == FavoritesFragment.class) {
                        Analytics.with(getActivity()).track(CommonConsts.SEG_URL_SEARCH_FAV);

                    }
                }

                mLaunchUri = getItemAtPosition(mSelectedItemPosition);
                if (UriHelper.isLaunchable(mLaunchUri)) {
                    mUrlEdit.setText(mLaunchUri.toString());
                    showLaunchButtons();

                } else {
                    hideLaunchButtons();
                }

                if (ResourcesFragment.this.getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_SELECT, new Properties().putValue("name", mLaunchUri.getLastPathSegment()));

                } else if (ResourcesFragment.this.getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_SELECT, new Properties().putValue("name", mLaunchUri.getLastPathSegment()));

                }
            }
        });
        mItemsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mSwipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        if (mSwipeLayout != null) {
            mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    reloadList();
                }
            });
            mSwipeLayout.setColorSchemeResources(
                    R.color.blue_transparent,
                    R.color.blue,
                    R.color.highlighted_blue,
                    R.color.blue_transparent);

            mItemsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) { /* Nothing to do here */ }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (ResourcesFragment.this.isVisible()) {
                        boolean enable = false;
                        if(view != null && view.getChildCount() > 0){
                            boolean firstItemVisible = view.getFirstVisiblePosition() == 0; // check if the first item of the list is visible
                            boolean topOfFirstItemVisible = view.getChildAt(0).getTop() == 0; // check if the top of the first item is visible
                            enable = firstItemVisible && topOfFirstItemVisible; // enabling or disabling the refresh layout
                        }
                        mSwipeLayout.setEnabled(enable);
                    }
                }
            });
        }
        mSwipeLayout.setEnabled(true);

        mListEmptyView = getLayoutInflater(savedInstanceState).inflate(getEmptyResource(), null);
        mListEmptyView.setVisibility(View.GONE);
        ViewGroup listLayout = (ViewGroup) getView().findViewById(R.id.list_layout);
        listLayout.addView(mListEmptyView);

        mListProgressView = getView().findViewById(R.id.list_progress);
        mListProgressView.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mAcceleratedWebviewButton.setVisibility(View.GONE);
        }
	}

    private View getViewByPosition(int position) {
        int firstPosition = mItemsList.getFirstVisiblePosition() - mItemsList.getHeaderViewsCount();
        int wantedChild = position - firstPosition;
        if (wantedChild < 0 || wantedChild >= mItemsList.getChildCount()) {
            return null;
        }

        return mItemsList.getChildAt(wantedChild);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegratorSupportV4.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            try {
                new URL(scanResult.getContents());
                String url = scanResult.getContents();

                mLaunchUri = Uri.parse(url);
                if (UriHelper.isLaunchable(mLaunchUri)) {
                    mUrlEdit.setText(mLaunchUri.toString());
                    showLaunchButtons();

                } else {
                    hideLaunchButtons();
                }

                saveLastUrl();

            } catch (MalformedURLException e) {
                SystemUtils.showAlert(getActivity(), getString(R.string.error_msg), getString(R.string.invalid_qrcode_msg), false);
            }
        }

        reloadList();
    }

    public void onResume() {
        super.onResume();

        mAdapter.registerDataSetObserver(mDataSetObserver);

        if (!AccountUtils.checkUserHasAccount(getActivity())) {
            getActivity().getSupportFragmentManager().popBackStackImmediate();

        } else {
            restoreListStatus();
            reloadList();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        saveListStatus();

        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        mAdapter.unregisterDataSetObserver(mDataSetObserver);

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mReceiver != null)
            localBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResource(), menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                ResourcesCursorAdapter adapter = getAdapter();
                if (adapter != null)
                    adapter.getFilter().filter("");

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        if (searchItem != null && MenuItemCompat.isActionViewExpanded(searchItem))
            MenuItemCompat.collapseActionView(searchItem);

        menu.setGroupEnabled(R.id.menu_list_actions, mAdapter == null || !mAdapter.isEmpty());

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_settings) {
            getActivity().startActivity(new Intent(getActivity(), PreferencesActivity.class));

            return true;

        } else if (itemId == R.id.menu_multi) {
            if (getClass() == DocumentsFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_ACTION_MULTI);

            } else if (getClass() == FavoritesFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_ACTION_MULTI);
            }

            showMultiButtons();
            mItemsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            return true;

        } else if (itemId == R.id.menu_sort_name) {
            if (getClass() == DocumentsFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_ACTION_SORT_NAME);

            } else if (getClass() == FavoritesFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_ACTION_SORT_NAME);
            }

            if (getCurrentOrder() == SortOrder.NAME_ASC)
                setCurrentOrder(SortOrder.NAME_DSC);
            else
                setCurrentOrder(SortOrder.NAME_ASC);

            reloadList();

            return true;

        } else if (itemId == R.id.menu_sort_size) {
            if (getCurrentOrder() == SortOrder.SIZE_ASC)
                setCurrentOrder(SortOrder.SIZE_DSC);
            else
                setCurrentOrder(SortOrder.SIZE_ASC);

            reloadList();

            return true;

        } else if (itemId == R.id.menu_sort_date) {
            if (getClass() == DocumentsFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_ACTION_SORT_DATE);

            } else if (getClass() == FavoritesFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_ACTION_SORT_DATE);
            }

            if (getCurrentOrder() == SortOrder.DATE_ASC)
                setCurrentOrder(SortOrder.DATE_DSC);
            else
                setCurrentOrder(SortOrder.DATE_ASC);

            reloadList();

            return true;

        } else if (itemId == R.id.menu_clear) {
            if (getClass() == UrlFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_URL_ACTION_CLEAR);
            }

            deleteAllItems();
            reloadList();
            hideLaunchButtons();

            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId != LOADER)
            return null;

        mListProgressView.setVisibility(View.VISIBLE);
        mListEmptyView.setVisibility(View.GONE);
        getList().setVisibility(View.GONE);

        return getLoaderCursor(bundle);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SwipeRefreshLayout swipeLayout = getSwipeLayout();
        if (swipeLayout != null)
            swipeLayout.setRefreshing(false);

        if (loader.getId() != LOADER)
            return;

        mListProgressView.setVisibility(View.GONE);

        if(mAdapter != null && cursor != null){
            if (cursor.getCount() == 0) {
                mListEmptyView.setVisibility(View.VISIBLE);
                mItemsList.setVisibility(View.GONE);

            } else {
                mListEmptyView.setVisibility(View.GONE);
                mItemsList.setVisibility(View.VISIBLE);
            }

            mAdapter.changeCursor(cursor);
            mAdapter.notifyDataSetChanged();
        }

        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() != LOADER)
            return;

        ResourcesCursorAdapter adapter = getAdapter();
        if(adapter != null) {
            adapter.changeCursor(null);
            adapter.notifyDataSetChanged();
        }
    }

    protected void showLaunchButtons() {
        if (mLaunchButtons.isShown())
            return;

        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
        mLaunchButtons.startAnimation(bottomUp);
        mLaunchButtons.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, mLaunchButtons.getId());
    }

    protected void hideLaunchButtons() {
        if (!mLaunchButtons.isShown())
            return;

        Animation upBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
        mLaunchButtons.startAnimation(upBottom);
        mLaunchButtons.setVisibility(View.GONE);

        if (!mMultiButtons.isShown()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, mMultiButtons.getId());
        }
    }

    protected void showMultiButtons() {
        if (mMultiButtons.isShown())
            return;

        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
        mMultiButtons.startAnimation(bottomUp);
        mMultiButtons.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, mMultiButtons.getId());
    }

    protected void hideMultiButtons() {
        if (!mMultiButtons.isShown())
            return;

        Animation upBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
        mMultiButtons.startAnimation(upBottom);
        mMultiButtons.setVisibility(View.GONE);

        if (!mMultiButtons.isShown()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, mLaunchButtons.getId());
        }
    }

    protected void showUrlButtons() {
        if (mUrlButtons.isShown())
            return;

        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
        mUrlButtons.startAnimation(bottomUp);
        mUrlButtons.setVisibility(View.VISIBLE);
    }

    protected void hideUrlButtons() {
        if (!mUrlButtons.isShown())
            return;

        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_up);
        mUrlButtons.startAnimation(bottomUp);
        mUrlButtons.setVisibility(View.GONE);
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        ResourcesCursorAdapter adapter = getAdapter();
        if(adapter != null) {
            adapter.getFilter().filter(s);
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        ResourcesCursorAdapter adapter = getAdapter();
        if(adapter != null) {
            adapter.getFilter().filter(s);
        }

        return true;
    }

    public void addSelectedFavorites() {
        int len = mItemsList.getCount();
        HashMap<Integer, Boolean> selection = new HashMap<Integer, Boolean>();
        SparseBooleanArray checked = mItemsList.getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                selection.put(i, checked.get(i));
            }
        }

        ArrayList<Uri> selectedItems = getSelectedItems(selection);
        addFavoriteItems(selectedItems);

        FavoritesCache.getInstance(getActivity()).refresh();

        cancelButton.performClick();
        reloadList();
    }

    public void addFavoriteItems(ArrayList<Uri> selectedItems) {
        try {
            final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            int noOfModels = selectedItems.size();
            for (int i = 0; i < noOfModels; i++) {
                String select = "(" + FavoritesContentProvider.FAVORITES_URI_COLUMN + " = ? )";
                String[] selectArgs = { selectedItems.get(i).toString() };
                Cursor cursor = getActivity().getContentResolver().query(
                        FavoritesContentProvider.FAVORITES_URI,
                        new String[]{ FavoritesContentProvider.FAVORITES_URI_COLUMN },
                        select,
                        selectArgs,
                        null);

                if (cursor.getCount() == 0) {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(FavoritesContentProvider.FAVORITES_URI);
                    ContentValues cv = new ContentValues();
                    cv.put(FavoritesContentProvider.FAVORITES_URI_COLUMN, selectedItems.get(i).toString());
                    cv.put(FavoritesContentProvider.FAVORITES_NAME_COLUMN, selectedItems.get(i).toString());
                    cv.put(FavoritesContentProvider.FAVORITES_DATE_COLUMN, System.currentTimeMillis());
                    builder.withValues(cv);
                    batch.add(builder.build());

                } else {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(FavoritesContentProvider.FAVORITES_URI);
                    ContentValues cv = new ContentValues();
                    cv.put(FavoritesContentProvider.FAVORITES_URI_COLUMN, selectedItems.get(i).toString());
                    cv.put(FavoritesContentProvider.FAVORITES_NAME_COLUMN, selectedItems.get(i).toString());
                    cv.put(FavoritesContentProvider.FAVORITES_DATE_COLUMN, System.currentTimeMillis());
                    builder.withValues(cv);
                    batch.add(builder.build());
                }

                cursor.close();
            }
            getActivity().getContentResolver().applyBatch(FavoritesContentProvider.AUTHORITY, batch);

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void removeSelectedFavorites() {
        int len = mItemsList.getCount();
        HashMap<Integer, Boolean> selection = new HashMap<Integer, Boolean>();
        SparseBooleanArray checked = mItemsList.getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                selection.put(i, checked.get(i));
            }
        }

        ArrayList<Uri> selectedItems = getSelectedItems(selection);
        removeFavoriteItems(selectedItems);

        FavoritesCache.getInstance(getActivity()).refresh();

        cancelButton.performClick();
        reloadList();
    }

    public void removeFavoriteItems(ArrayList<Uri> selectedItems) {
        try {
            final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            int noOfModels = selectedItems.size();
            for (int i = 0; i < noOfModels; i++) {
                String select = "(" + FavoritesContentProvider.FAVORITES_URI_COLUMN + " = ? )";
                String[] selectArgs = { selectedItems.get(i).toString() };
                Cursor cursor = getActivity().getContentResolver().query(
                        FavoritesContentProvider.FAVORITES_URI,
                        new String[]{ FavoritesContentProvider.FAVORITES_URI_COLUMN },
                        select,
                        selectArgs,
                        null);

                if (cursor.getCount() > 0) {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(FavoritesContentProvider.FAVORITES_URI);
                    builder.withSelection(
                            select,
                            selectArgs);
                    batch.add(builder.build());
                }

                cursor.close();
            }
            getActivity().getContentResolver().applyBatch(FavoritesContentProvider.AUTHORITY, batch);

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addHistoryItem(Uri uri, long timestamp) {
        String select = "(" + HistoryContentProvider.HISTORY_URI_COLUMN + " = ? )";
        String[] selectArgs = { uri.toString() };

        Cursor entries = getActivity().getContentResolver().query(
                HistoryContentProvider.HISTORY_URI,
                new String[] {
                        HistoryContentProvider.HISTORY_ID_COLUMN,
                        HistoryContentProvider.HISTORY_URI_COLUMN,
                        HistoryContentProvider.HISTORY_DATE_COLUMN
                },
                select,
                selectArgs,
                null);

        if (entries != null && entries.getCount()== 0) {
            ContentValues cv = new ContentValues();
            cv.put(HistoryContentProvider.HISTORY_URI_COLUMN, uri.toString());
            cv.put(HistoryContentProvider.HISTORY_DATE_COLUMN, timestamp);
            getActivity().getContentResolver().insert(HistoryContentProvider.HISTORY_URI, cv);

        } else {
            ContentValues cv = new ContentValues();
            cv.put(HistoryContentProvider.HISTORY_URI_COLUMN, uri.toString());
            cv.put(HistoryContentProvider.HISTORY_DATE_COLUMN, timestamp);
            getActivity().getContentResolver().update(HistoryContentProvider.HISTORY_URI, cv, select, selectArgs);
        }
    }

    public SortOrder getCurrentOrder() {
        return mCurrentOrder;
    }

    public void setCurrentOrder(SortOrder order) {
        mCurrentOrder = order;
    }

    public ResourcesCursorAdapter getAdapter() {
        return mAdapter;
    }

    public ListView getList() {
        return mItemsList;
    }

    public SwipeRefreshLayout getSwipeLayout() {
        return mSwipeLayout;
    }

    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    public void setSelectedItemPosition(int position) {
        mSelectedItemPosition = position;
    }

    public abstract Uri getItemAtPosition(int index);

    public abstract ArrayList<Uri> getSelectedItems(HashMap<Integer, Boolean> selection);

    public abstract void deleteSelectedItems();

    public abstract void deleteAllItems();

    public abstract void renameSelectedItems();

    public abstract ResourcesCursorAdapter createAdapter();

    public abstract void saveListStatus();

    public abstract void restoreListStatus();

    public abstract void reloadList();

    protected abstract void saveLastUrl();

    public abstract int getEmptyResource();

    public abstract int getMenuResource();

    public abstract CursorLoader getLoaderCursor(Bundle bundle);

}