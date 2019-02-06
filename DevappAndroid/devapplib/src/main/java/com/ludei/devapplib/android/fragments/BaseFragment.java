package com.ludei.devapplib.android.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;

import com.ludei.devapplib.android.CommonConsts;
import com.segment.analytics.Analytics;

import java.util.List;

/**
 * Created by imanolmartin on 21/08/14.
 */
public abstract class BaseFragment extends Fragment {

    private final SparseIntArray mRequestCodes = new SparseIntArray();

    public boolean onBackPressed() {
        try {
            if (getClass() == Class.forName("DemosFragment")) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS_BACK);

            } else if (getClass() == UrlFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_URL_BACK_TO_MENU);

            } else if (getClass() == DocumentsFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_BACK_TO_MENU);

            } else if (getClass() == FavoritesFragment.class) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_BACK_TO_MENU);
            }

        } catch (ClassNotFoundException e) {}
        return false;
    }

    /**
     * Registers request code (used in
     * {@link #startActivityForResult(android.content.Intent, int)}).
     *
     * @param requestCode
     *            the request code.
     * @param id
     *            the fragment ID (can be {@link android.support.v4.app.Fragment#getId()} of
     *            {@link android.support.v4.app.Fragment#hashCode()}).
     */
    public void registerRequestCode(int requestCode, int id) {
        mRequestCodes.put(requestCode, id);
    }// registerRequestCode()

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() instanceof BaseFragment) {
            ((BaseFragment) getParentFragment()).registerRequestCode(
                    requestCode, hashCode());
            getParentFragment().startActivityForResult(intent, requestCode);
        } else
            super.startActivityForResult(intent, requestCode);
    }// startActivityForResult()

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!checkNestedFragmentsForResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }// onActivityResult()

    /**
     * Checks to see whether there is any children fragments which has been
     * registered with {@code requestCode} before. If so, let it handle the
     * {@code requestCode}.
     *
     * @param requestCode
     *            the code from {@link #onActivityResult(int, int, android.content.Intent)}.
     * @param resultCode
     *            the code from {@link #onActivityResult(int, int, android.content.Intent)}.
     * @param data
     *            the data from {@link #onActivityResult(int, int, android.content.Intent)}.
     * @return {@code true} if the results have been handed over to some child
     *         fragment. {@code false} otherwise.
     */
    protected boolean checkNestedFragmentsForResult(int requestCode,
                                                    int resultCode, Intent data) {
        final int id = mRequestCodes.get(requestCode);
        if (id == 0)
            return false;

        mRequestCodes.delete(requestCode);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments == null)
            return false;

        for (Fragment fragment : fragments) {
            if (fragment.hashCode() == id) {
                fragment.onActivityResult(requestCode, resultCode, data);
                return true;
            }
        }

        return false;
    }// checkNestedFragmentsForResult()


}