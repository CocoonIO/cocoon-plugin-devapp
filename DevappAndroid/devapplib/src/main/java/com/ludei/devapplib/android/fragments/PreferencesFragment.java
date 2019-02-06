package com.ludei.devapplib.android.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.auth.AccountUtils;
import com.ludei.devapplib.android.auth.cocoon.AddAccountResponse;
import com.segment.analytics.Analytics;

/**
 * Created by imanolmartin on 05/09/14.
 */
public class PreferencesFragment  extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            updatePreference(getPreferenceScreen().getPreference(i));
        }

        findPreference(getString(R.string.pref_logout_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_debug_enable_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_remote_debug_enable_key)).setOnPreferenceClickListener(this);
        ListPreference pref = (ListPreference)findPreference(getString(R.string.pref_debug_position_key));
        pref.setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_orientation_mode_key));
        pref.setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_fps_type_key));
        pref.setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_webgl_enabled_key)).setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_webgl_sreencanvas_key));
        pref.setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_texture_reducer_key));
        pref.setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_supersampling_level_key));
        pref.setOnPreferenceClickListener(this);
        pref = (ListPreference)findPreference(getString(R.string.pref_path_render_quality_key));
        pref.setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_npot_allowed_key)).setOnPreferenceClickListener(this);

        Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_VIEW);
        Analytics.with(getActivity()).screen(CommonConsts.SEG_SETTINGS_VIEW, CommonConsts.SEG_SETTINGS_VIEW);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle("Reset settings");
            adb.setMessage("Are you sure you want to revert all the settings to their default values?");
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ACTIONS_RESET);
                    resetToDefaults();
                }
            });
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adb.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        preferenceChanged(findPreference(key));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void resetToDefaults() {
        PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit()
                .clear()
                .commit();

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
        startActivity(getActivity().getIntent());
        getActivity().overridePendingTransition(0, 0);
    }

    private void updatePreference(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                updatePreference(pCat.getPreference(i));
            }

        } else if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());

        } else if (p instanceof Preference) {
            if (p.getKey().equalsIgnoreCase(getString(R.string.pref_logout_key))) {
                Account account = AccountUtils.getAccount(getActivity());
                if (account != null) {
                    AccountManager manager = AccountManager.get(getActivity());
                    String email = manager.getUserData(account, AddAccountResponse.EMAIL);
                    p.setSummary(email);

                } else {
                    p.setEnabled(false);
                }
            }
        }
    }

    private void preferenceChanged(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_orientation_mode_key))) {
                if (listPref.getValue().equalsIgnoreCase("Landscape")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ORIENTATION_LANDSCAPE);

                } else if (listPref.getValue().equalsIgnoreCase("Portrait")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ORIENTATION_PORTRAIT);

                } else if (listPref.getValue().equalsIgnoreCase("Both")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ORIENTATION_BOTH);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_debug_position_key))) {
                if (listPref.getValue().equalsIgnoreCase("Bottom left")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_POSITION_BOTTOM_LEFT);

                } else if (listPref.getValue().equalsIgnoreCase("Bottom right")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_POSITION_BOTTOM_RIGHT);

                } else if (listPref.getValue().equalsIgnoreCase("Top left")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_POSITION_TOP_LEFT);

                } else if (listPref.getValue().equalsIgnoreCase("Top right")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_POSITION_TOP_RIGHT);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_fps_type_key))) {
                if (listPref.getValue().equalsIgnoreCase("Frame time")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_FPSTYPE_TIME);

                } else if (listPref.getValue().equalsIgnoreCase("Frames per second")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_FPSTYPE_FPS);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_webgl_sreencanvas_key))) {
                if (listPref.getValue().equalsIgnoreCase("Enabled by default")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_SCREENCANVAS_ENABLED);

                } else if (listPref.getValue().equalsIgnoreCase("Disabled by default")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_SCREENCANVAS_DISABLED);

                } else if (listPref.getValue().equalsIgnoreCase("Force enabled")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_SCREENCANVAS_FORCE_ENABLED);

                } else if (listPref.getValue().equalsIgnoreCase("Force disabled")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_SCREENCANVAS_FORCE_DISABLED);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_texture_reducer_key))) {
                if (listPref.getValue().equalsIgnoreCase("Disabled")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_DISABLED);

                } else if (listPref.getValue().equalsIgnoreCase("Above 64")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_64);

                } else if (listPref.getValue().equalsIgnoreCase("Above 128")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_128);

                } else if (listPref.getValue().equalsIgnoreCase("Above 256")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_256);

                } else if (listPref.getValue().equalsIgnoreCase("Above 512")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_512);

                } else if (listPref.getValue().equalsIgnoreCase("Above 1024")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_1024);

                } else if (listPref.getValue().equalsIgnoreCase("Above 2048")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER_2048);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_supersampling_level_key))) {
                if (listPref.getValue().equalsIgnoreCase("Disabled")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_DISABLED);

                } else if (listPref.getValue().equalsIgnoreCase("0.5x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_015);

                } else if (listPref.getValue().equalsIgnoreCase("1.5x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_150);

                } else if (listPref.getValue().equalsIgnoreCase("2.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_200);

                } else if (listPref.getValue().equalsIgnoreCase("2.5x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_250);

                } else if (listPref.getValue().equalsIgnoreCase("3.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_300);

                } else if (listPref.getValue().equalsIgnoreCase("4.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_400);

                } else if (listPref.getValue().equalsIgnoreCase("5.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_500);

                } else if (listPref.getValue().equalsIgnoreCase("6.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_600);

                } else if (listPref.getValue().equalsIgnoreCase("7.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_700);

                } else if (listPref.getValue().equalsIgnoreCase("8.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_800);

                } else if (listPref.getValue().equalsIgnoreCase("9.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_900);

                } else if (listPref.getValue().equalsIgnoreCase("10.0x")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_SUPERSAMPLING_LEVEL_100);
                }

            } else if (listPref.getKey().equalsIgnoreCase(getString(R.string.pref_path_render_quality_key))) {
                if (listPref.getValue().equalsIgnoreCase("Fastest")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY_FASTEST);

                } else if (listPref.getValue().equalsIgnoreCase("Fast")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY_FAST);

                } else if (listPref.getValue().equalsIgnoreCase("Default")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY_DEFAULT);

                } else if (listPref.getValue().equalsIgnoreCase("Prefer quality")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY_PREFER);

                } else if (listPref.getValue().equalsIgnoreCase("Best quality")) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY_BEST);
                }

            }

        } else if (p instanceof SwitchPreference) {
            SwitchPreference pref = (SwitchPreference) p;
            if (pref.getKey().equalsIgnoreCase(getString(R.string.pref_debug_enable_key))) {
                if (pref.isChecked())
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_ENABLED);
                else
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_DISABLED);

            } else if (pref.getKey().equalsIgnoreCase(getString(R.string.pref_webgl_enabled_key))) {
                if (pref.isChecked())
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_ENABLED);
                else
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_DISABLED);

            } else if (pref.getKey().equalsIgnoreCase(getString(R.string.pref_npot_allowed_key))) {
                if (pref.isChecked())
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_CANVAS_ENABLED);
                else
                    Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_CANVAS_DISABLED);
            }
        }

        updatePreference(p);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof Preference) {
            if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_orientation_mode_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ORIENTATION);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_debug_position_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_DEBUG_POSITION);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_fps_type_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_FPSTYPE);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_webgl_sreencanvas_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_WEBGL_SCREENCANVAS);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_texture_reducer_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_TEXTUREREDUCER);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_path_render_quality_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_PATHRENDER_QUALITY);

            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_logout_key))) {
                Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ACCOUNT);

                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Account Logout");
                adb.setMessage("Are you sure you want to logout from account " + preference.getSummary() + "?");
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Analytics.with(getActivity()).track(CommonConsts.SEG_SETTINGS_ACCOUNT_LOGOUT);

                        Account account = AccountUtils.getAccount(getActivity());
                        AccountManager manager = AccountManager.get(getActivity());
                        manager.removeAccount(account, new AccountManagerCallback<Boolean>() {

                            @Override
                            public void run(AccountManagerFuture<Boolean> accountManagerFuture) {}

                        }, null);

                        getActivity().finish();
                    }
                });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.show();

            }
        }

        return true;
    }
}
