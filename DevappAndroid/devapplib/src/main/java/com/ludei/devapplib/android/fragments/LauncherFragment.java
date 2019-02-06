package com.ludei.devapplib.android.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.tasks.FileAsyncTask;
import com.ludei.devapplib.android.tasks.FileDownloadAsyncTask;
import com.ludei.devapplib.android.tasks.FileUnzipAsyncTask;
import com.ludei.devapplib.android.utils.FileUtils;
import com.ludei.devapplib.android.utils.LauncherUtils;
import com.ludei.devapplib.android.utils.SystemUtils;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by imanolmartin on 28/10/15.
 */
public abstract class LauncherFragment extends BaseFragment {

    protected ProgressDialog mProgressDialog;
    protected FileAsyncTask unzipAsyncTask = null;
    protected FileAsyncTask downloadAsyncTask = null;
    protected Uri mLaunchUri;
    protected String mOrientation;
    private LauncherUtils.Engines mEnvironment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mLaunchUri = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (downloadAsyncTask != null && !downloadAsyncTask.isFinished()) {
            mProgressDialog.setTitle("Downloading...");
            mProgressDialog.show();
        }

        if (unzipAsyncTask != null && !unzipAsyncTask.isFinished()) {
            mProgressDialog.setTitle("Unzipping...");
            mProgressDialog.show();
        }
    }

    protected View.OnClickListener webviewPlusClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEnvironment = LauncherUtils.Engines.WEBVIEW_PLUS;
            launch(mLaunchUri.toString());
        }
    };

    protected View.OnClickListener webviewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEnvironment = LauncherUtils.Engines.SYSTEM_WEBVIEW;
            launch(mLaunchUri.toString());
        }
    };

    protected View.OnClickListener canvasplusClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEnvironment = LauncherUtils.Engines.CANVAS_PLUS;
            launch(mLaunchUri.toString());
        }
    };

    protected void launch(String urlStr) {
        boolean isInvalidUrl = false;
        String errorMsg = "Invalid URL";

        if (urlStr == null) {
            isInvalidUrl = true;

        } else {
            try {
                if (!URLUtil.isHttpUrl(urlStr) && !URLUtil.isHttpsUrl(urlStr) && !URLUtil.isFileUrl(urlStr) && !urlStr.startsWith("file:///android_asset"))
                    urlStr = "http://" + urlStr;

                URL url = new URL(URLDecoder.decode(urlStr, "UTF-8"));
                if (URLUtil.isFileUrl(url.toString())) {
                    launchFile(url.toString());

                } else {
                    String launchUrl = url.toString();
                    if (launchUrl.endsWith(".git")) {
                        int index = launchUrl.lastIndexOf(".git");
                        launchUrl = launchUrl.substring(0, index) + "/archive/master.zip";
                    }
                    launchUrl(launchUrl);
                }

            } catch (UnsupportedEncodingException e) {
                isInvalidUrl = true;
                errorMsg = e.getMessage();

            } catch (MalformedURLException e) {
                isInvalidUrl = true;
                errorMsg = e.getMessage();
            }
        }

        if (isInvalidUrl) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Invalid URL")
                    .setMessage(errorMsg)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    protected void launchFile(String urlStr) {
        if (urlStr.endsWith(".zip")) {
            urlStr = urlStr.substring("file://".length());
            String filename = "www";
            String outputPath = FileUtils.getOutputPath(getActivity()) + File.separator + filename;

            FileUtils.deleteFile(outputPath);

            unzipAsyncTask = new FileUnzipAsyncTask(getActivity(), outputPath, unzipListener);
            unzipAsyncTask.execute(urlStr);

        } else {
            launchEnvironment(urlStr);
        }
    }

    protected void launchUrl(String urlStr) {
        if (urlStr.endsWith(".zip")) {
            try {
                String outputPath = FileUtils.getOutputPath(getActivity());

                URL url = new URL(URLDecoder.decode(urlStr, "UTF-8"));
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

                downloadAsyncTask = new FileDownloadAsyncTask(getActivity(), outputPath, downloadListener);
                SystemUtils.executeAsyncTask(downloadAsyncTask, uri.toURL().toString());

            } catch (URISyntaxException e) {
                e.printStackTrace();

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            launchEnvironment(urlStr);
        }
    }

    protected FileAsyncTask.FileAsyncTaskListener downloadListener = new FileAsyncTask.FileAsyncTaskListener() {

        @Override
        public void onFileTaskStarted(final FileAsyncTask task) {
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    task.cancel(true);
                }
            });
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMax(0);
            mProgressDialog.setProgress(0);
            mProgressDialog.setTitle("Downloading...");
            mProgressDialog.show();
        }

        @Override
        public void onFileTaskProgress(FileAsyncTask task, long size, long progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax((int) size);
            mProgressDialog.setProgress((int) progress);
        }

        @Override
        public void onFileTaskFinished(FileAsyncTask task, String downloadedFilePath) {
            String filename = "www";
            String outputPath = FileUtils.getOutputPath(getActivity()) + File.separator + filename;

            FileUtils.deleteFile(outputPath);
            unzipAsyncTask = new FileUnzipAsyncTask(getActivity(), outputPath, unzipListener);
            SystemUtils.executeAsyncTask(unzipAsyncTask, downloadedFilePath);
        }

        @Override
        public void onFileTaskError(FileAsyncTask task, String errorMsg) {
            mProgressDialog.dismiss();
            SystemUtils.showAlert(getActivity(), "Download error", "Error downloading file: " + mLaunchUri.toString(), false);
        }

        @Override
        public void onFileTaskCancelled(FileAsyncTask task) {
            mProgressDialog.cancel();
        }

    };

    protected FileAsyncTask.FileAsyncTaskListener unzipListener = new FileAsyncTask.FileAsyncTaskListener() {

        @Override
        public void onFileTaskStarted(final FileAsyncTask task) {
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    task.cancel(true);
                }
            });
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMax(0);
            mProgressDialog.setProgress(0);
            mProgressDialog.setTitle("Unzipping...");
            mProgressDialog.show();
        }

        @Override
        public void onFileTaskProgress(FileAsyncTask task, long size, long progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax((int) size);
            mProgressDialog.setProgress((int) progress);
        }

        @Override
        public void onFileTaskFinished(FileAsyncTask task, String outputPath) {
            // Copy the cordova files in case the user hasn't uploaded them
            try {
                String dstPath = new File(FileUtils.getFirstIndex(outputPath)).getParent();
                FileUtils.copyFromAssetsFilesDir(getActivity(), "www" + File.separator + "cordova.js", dstPath);
                FileUtils.copyFromAssetsFilesDir(getActivity(), "www" + File.separator + "cordova_plugins.js", dstPath);
                FileUtils.copyFromAssetsFilesDir(getActivity(), "www" + File.separator + "plugins", dstPath);
                FileUtils.copyFromAssetsFilesDir(getActivity(), "www" + File.separator + "cordova-js-src", dstPath);

            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
            launchEnvironment("file://" + FileUtils.getFirstIndex(outputPath));
        }

        @Override
        public void onFileTaskError(FileAsyncTask task, String errorMsg) {
            mProgressDialog.dismiss();
            SystemUtils.showAlert(getActivity(), "Uncompressing error", "Error uncompressing file: " + mLaunchUri.toString(), false);
        }

        @Override
        public void onFileTaskCancelled(FileAsyncTask task) {
            mProgressDialog.cancel();
        }

    };

    protected void launchEnvironment(String url) {
        switch (mEnvironment) {
            case SYSTEM_WEBVIEW:
                if (getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_WEBVIEW, new Properties().putValue("url", url));

                } else if (getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_WEBVIEW, new Properties().putValue("url", url));

                } else if (getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_WEBVIEW, new Properties().putValue("url", url));
                }

                LauncherUtils.launchWebView(getActivity(), url, mOrientation);
                addHistoryItem(mLaunchUri, System.currentTimeMillis());
                break;

            case WEBVIEW_PLUS:
                if (getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_WEBVIEWPLUS, new Properties().putValue("url", url));

                } else if (getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_WEBVIEWPLUS, new Properties().putValue("url", url));

                } else if (getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_WEBVIEWPLUS, new Properties().putValue("url", url));
                }

                LauncherUtils.launchWebViewPlus(getActivity(), url, mOrientation);
                addHistoryItem(mLaunchUri, System.currentTimeMillis());
                break;

            case CANVAS_PLUS:
                if (getClass() == UrlFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_URL_CANVASPLUS, new Properties().putValue("url", url));

                } else if (getClass() == DocumentsFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_DOC_CANVASPLUS, new Properties().putValue("url", url));

                } else if (getClass() == FavoritesFragment.class) {
                    Analytics.with(getActivity()).track(CommonConsts.SEG_FAV_CANVASPLUS, new Properties().putValue("url", url));
                }

                LauncherUtils.launchCanvasPlus(getActivity(), url, mOrientation);
                addHistoryItem(mLaunchUri, System.currentTimeMillis());
                break;
        }
    }

    protected void addHistoryItem(Uri uri, long timestamp) {
        // Nothing to do by default
    }


}
