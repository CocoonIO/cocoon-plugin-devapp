package com.ludei.devapplib.android.utils;

import android.net.Uri;
import android.webkit.URLUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by imanolmartin on 27/08/14.
 */
public class UriHelper {

    public final static String ZIP_EXT = "zip";
    public final static String HTM_EXT = "htm";
    public final static String HTML_EXT = "html";
    public final static String JS_EXT = "js";

    public static final String[] LAUNCHABLE_EXTENSIONS = new String[]{ZIP_EXT, HTM_EXT, HTML_EXT, JS_EXT};

    public static String getName(Uri uri) {
        if (isUrl(uri))
            return uri.toString();

        return uri.getLastPathSegment();
    }

    public static String getSize(Uri uri) {
        if (URLUtil.isFileUrl(uri.toString())) {
            File file = new File(uri.getPath());
            long size = file.length();
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));

            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        return "";
    }

    public static String getExtension(Uri uri) {
        String extension = "";
        if (uri.getLastPathSegment() != null) {
            int index = uri.getLastPathSegment().lastIndexOf(".");
            if (index != -1)
                extension = uri.getLastPathSegment().substring(index + 1);
        }

        return extension;
    }

    public static String getLastModificationDate(Uri uri) {
        if (URLUtil.isFileUrl(uri.toString())) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String lastModificationDate = format.format(new File(uri.getPath()).lastModified());

            return lastModificationDate;
        }

        return "";
    }

    public static boolean isLaunchable(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return false;

        if (URLUtil.isFileUrl(uri.toString())) {
            File file = new File(uri.getPath());
            if (file.isFile() && (
                    getExtension(uri).equalsIgnoreCase(HTML_EXT) ||
                    getExtension(uri).equalsIgnoreCase(HTM_EXT) ||
                    getExtension(uri).equalsIgnoreCase(JS_EXT) ||
                    getExtension(uri).equalsIgnoreCase(ZIP_EXT)))
                return true;

            if (file.isDirectory()) {
                List<File> files = FileUtils.listFiles(file.getAbsolutePath(), new String[]{HTML_EXT, HTM_EXT, JS_EXT}, false);
                for (Iterator<File> it = files.iterator(); it.hasNext();) {
                    File item = it.next();
                    if (isLaunchable(Uri.fromFile(item)))
                        return true;
                }
            }

            return false;

        } else {
            return true;
        }
    }

    public static boolean isDirectory(Uri uri) {
        if (URLUtil.isFileUrl(uri.toString())) {
            File file = new File(uri.getPath());

            return file.isDirectory();
        }

        return false;
    }

    public static boolean isUrl(Uri uri) {
        if (uri.getScheme() == null ||
                uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https") ||
                uri.getScheme().isEmpty()) {
            return true;
        }

        return false;
    }

}

