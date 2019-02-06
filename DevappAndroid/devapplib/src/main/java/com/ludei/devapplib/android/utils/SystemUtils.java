package com.ludei.devapplib.android.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;

public class SystemUtils {

	public static void showAlert(final Activity activity, String title, String msg, final boolean exit) {
        if (activity == null)
            return;

		AlertDialog dialog = new AlertDialog.Builder(activity)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if (exit)
                    activity.finish();
            }
        }).create();

		if (title != null) 
			dialog.setTitle(title);
		
		if (msg != null)
			dialog.setMessage(msg);
		
		dialog.show();
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

    public static String getApplicationVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "0";
    }

    public static String getStringByKey(Context app, String key) {
        int resourceId = app.getResources().getIdentifier(key, "string", app.getPackageName());
        return app.getString(resourceId);
    }

    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("SystemUtils", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("SystemUtils", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("SystemUtils", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("SystemUtils", "Was not able to restart application");
        }
    }

}
