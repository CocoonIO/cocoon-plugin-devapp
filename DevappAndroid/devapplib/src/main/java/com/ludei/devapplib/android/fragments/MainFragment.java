package com.ludei.devapplib.android.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.Consts;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.auth.AccountUtils;
import com.ludei.devapplib.android.auth.cocoon.AddAccountResponse;
import com.ludei.devapplib.android.utils.SystemUtils;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;


public class MainFragment extends BaseFragment implements AccountUtils.AddCocoonAccountCallback {

    public static final String TAG = MainFragment.class.getSimpleName();

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

        return inflater.inflate(R.layout.fragment_main, null);
    }
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar().getCustomView();
        TextView title = (TextView)actionBar.findViewById(R.id.title);
        title.setText(getString(R.string.title_main).toUpperCase());
        ImageView icon = (ImageView)actionBar.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_main);

        TextView versionText = (TextView)getView().findViewById(R.id.version_text);
        versionText.setText(SystemUtils.getApplicationVersion(getActivity()));

        Button demos_button = (Button)getView().findViewById(R.id.button_demos);
        demos_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_DEMOS);

                Fragment fragment = new DemosFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.content_frame, fragment, DemosFragment.TAG);
                transaction.addToBackStack(DemosFragment.TAG);
                transaction.commitAllowingStateLoss();
            }
        });

        Button yourAppButton = (Button)getView().findViewById(R.id.button_your_app);
        yourAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_YOUR_APP);

                Fragment fragment = new YourAppFragment();
                Bundle args = new Bundle();
                fragment.setArguments(args);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
                transaction.replace(R.id.content_frame, fragment, YourAppFragment.TAG);
                transaction.addToBackStack(YourAppFragment.TAG);
                transaction.commitAllowingStateLoss();
            }
        });

        Button loginButton = (Button)getView().findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_LOGIN);
                AccountUtils.addAccount(getActivity(), MainFragment.this);
            }
        });

        Button signupButton = (Button)getView().findViewById(R.id.button_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SIGNUP);
                AccountUtils.addAccount(getActivity(), MainFragment.this);
            }
        });

        if (!AccountUtils.checkUserHasAccount(getActivity())) {
            View loginLayout = getView().findViewById(R.id.login_layout);
            loginLayout.setVisibility(View.VISIBLE);
            yourAppButton.setVisibility(View.GONE);

        } else {
            View loginLayout = getView().findViewById(R.id.login_layout);
            loginLayout.setVisibility(View.GONE);
            yourAppButton.setVisibility(View.VISIBLE);
        }

        Analytics.with(getActivity()).track(CommonConsts.SEG_HOME_VIEW);
        Analytics.with(getActivity()).screen(CommonConsts.SEG_HOME_VIEW, CommonConsts.SEG_HOME_VIEW);
	}

    @Override
    public void onCocoonAccountAdded() {
        Toast.makeText(getActivity(), getString(R.string.account_created_msg), Toast.LENGTH_LONG).show();

        Fragment fragment = new YourAppFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        transaction.replace(R.id.content_frame, fragment, YourAppFragment.TAG);
        transaction.addToBackStack(YourAppFragment.TAG);
        transaction.commitAllowingStateLoss();

        Account account = AccountUtils.getAccount(getActivity());
        if (account != null) {
            AccountManager manager = AccountManager.get(getActivity());
            Analytics.with(getActivity()).identify(manager.getUserData(account, AddAccountResponse.ID), new Traits()
                    .putValue(AddAccountResponse.EMAIL, manager.getUserData(account, AddAccountResponse.EMAIL))
                    .putValue(AddAccountResponse.LASTNAME, manager.getUserData(account, AddAccountResponse.LASTNAME))
                    .putValue(AddAccountResponse.NAME, manager.getUserData(account, AddAccountResponse.NAME))
                    .putValue(AddAccountResponse.USERNAME, manager.getUserData(account, AddAccountResponse.USERNAME)), null);
        }
    }

    @Override
    public void onCocoonAccountAddError() {
        Toast.makeText(getActivity(), getString(R.string.no_account_msg), Toast.LENGTH_LONG).show();
    }

}
