package com.ludei.devapplib.android.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.PreferencesActivity;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.providers.DemosContentProvider;
import com.ludei.devapplib.android.tasks.FileAsyncTask;
import com.ludei.devapplib.android.utils.LauncherUtils;
import com.ludei.devapplib.android.utils.VolleySingleton;
import com.segment.analytics.Analytics;

public class DemoDetailsFragment extends LauncherFragment {
	
	public static final String TAG = DemoDetailsFragment.class.getSimpleName();
	
	private String mName, mDescription, mImageUrl, mGithubUrl;
    private String mEngines;
    private boolean mWebOnly = false;
	private ProgressDialog mProgressDialog;
    private FileAsyncTask unzipAsyncTask = null;
    private FileAsyncTask downloadAsyncTask = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (container == null) {
            return null;
        }
		
		return inflater.inflate(R.layout.fragment_demo_details, null);
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        View actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar().getCustomView();
        TextView title = (TextView)actionBar.findViewById(R.id.title);
        title.setText(getString(R.string.title_details).toUpperCase());
        ImageView icon = (ImageView)actionBar.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_demos);

        Bundle args = getArguments();
        mName = (String) args.get(DemosContentProvider.DEMO_NAME_COLUMN);
        mDescription = (String) args.get(DemosContentProvider.DEMO_DESCRIPTION_COLUMN);
        mImageUrl = (String) args.get(DemosContentProvider.DEMO_IMAGE_URL_COLUMN);
        mGithubUrl = (String) args.get(DemosContentProvider.DEMO_GITHUB_URL_COLUMN);
        mOrientation = (String) args.get(DemosContentProvider.DEMO_ORIENTATION_COLUMN);
        mWebOnly = (Boolean) args.get(DemosContentProvider.DEMO_WEB_COLUMN);
        mEngines = (String) args.get(DemosContentProvider.DEMO_ENVIRONMENT_COLUMN);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        ImageView imageView = (ImageView) view.findViewById(R.id.demo_thumbnail);
        VolleySingleton.getInstance(getActivity()).getImageLoader().get(mImageUrl, ImageLoader.getImageListener(imageView, R.drawable.detail_default, R.drawable.detail_default));
        Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        imageView.startAnimation(animation);
        imageView.setVisibility(View.VISIBLE);
		TextView nameView = (TextView)getView().findViewById(R.id.demo_name_text);
		nameView.setText(mName);
		TextView longDescriptionView = (TextView)getView().findViewById(R.id.demo_long_description_text);
		longDescriptionView.setText(mDescription);

        Button mWebviewButton = (Button) getView().findViewById(R.id.webview_button);
        mWebviewButton.setOnClickListener(webviewClickListener);
        Button mWebviewPlusButton = (Button) getView().findViewById(R.id.webviewplus_button);
        mWebviewPlusButton.setOnClickListener(webviewPlusClickListener);
        Button mCanvasPlusButton = (Button) getView().findViewById(R.id.canvasplus_button);
        mCanvasPlusButton.setOnClickListener(canvasplusClickListener);

        if (mEngines.equalsIgnoreCase(LauncherUtils.CANVAS_PLUS)) {
            mWebviewButton.setEnabled(false);
            mWebviewPlusButton.setEnabled(false);
        }

        if (mEngines.equalsIgnoreCase(LauncherUtils.WEBVIEW)) {
            mCanvasPlusButton.setEnabled(false);
        }

        View launchButtons = getView().findViewById(R.id.launch_buttons_layout);
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
        launchButtons.startAnimation(bottomUp);
        launchButtons.setVisibility(View.VISIBLE);

        mLaunchUri = Uri.parse(mGithubUrl);

        Analytics.with(getActivity()).track(String.format("%s%s", CommonConsts.SEG_DEMOS_ENTER, mName.replaceAll("\\s", "")));
        Analytics.with(getActivity()).screen(CommonConsts.SEG_DEMOS_DETAIL_VIEW, CommonConsts.SEG_DEMOS_DETAIL_VIEW);
	}

    @Override
    public void onResume(){
        super.onResume();

        if (downloadAsyncTask != null && !downloadAsyncTask.isFinished()) {
            mProgressDialog.setTitle("Downloading " + mName);
            mProgressDialog.show();
        }

        if (unzipAsyncTask != null && !unzipAsyncTask.isFinished()) {
            mProgressDialog.setTitle("Unzipping " + mName);
            mProgressDialog.show();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ViewGroup viewGroup = (ViewGroup) getView();
        viewGroup.removeAllViewsInLayout();
        View view = onCreateView(getActivity().getLayoutInflater(), viewGroup, null);
        viewGroup.addView(view);
        onViewCreated(view, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.demos_details, menu);
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

}
