package com.ludei.devapplib.android.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.ludei.devapplib.android.utils.FileUtils;
import com.ludei.devapplib.android.utils.UriHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesContentProvider extends ContentProvider {

	public static String AUTHORITY = "com.ludei.devapplib.android.provider.files.authority";

	public static Uri FILE_URI = Uri.parse("content://" + AUTHORITY + "/file");

    public static final String FILE_ID_COLUMN = "_id";
    public static final String FILE_PATH_COLUMN = "path";
    public static final String FILE_URI_COLUMN = "uri";
    public static final String FILE_NAME_COLUMN = "name";
    public static final String FILE_SIZE_COLUMN = "size";
    public static final String FILE_LAST_MODIFICATION_DATE_COLUMN = "last_modification_date";

	private static final int ALL_FILES = 1;
	private static final int SINGLE_FILE = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	}

	@Override
	public boolean onCreate() {

        try {
            String packageName = this.getContext().getPackageManager().getPackageInfo(this.getContext().getPackageName(), 0).packageName;

            AUTHORITY = packageName + ".provider.files.authority";
            FILE_URI = Uri.parse("content://" + AUTHORITY + "/demo");

            uriMatcher.addURI(AUTHORITY, "file", ALL_FILES);
            uriMatcher.addURI(AUTHORITY, "files/#", SINGLE_FILE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return true;
	}

    @Override
    public String getType(Uri uri) {
        // Return a string that identifies the MIME type
        // for a Content Provider URI
        switch (uriMatcher.match(uri)) {
            case ALL_FILES:
                return "vnd.android.cursor.dir/vnd.com.ludei.devapplib.android.provider.file";
            case SINGLE_FILE:
                return "vnd.android.cursor.dir/vnd.com.ludei.devapplib.android.provider.file";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                FILE_ID_COLUMN,
                FILE_URI_COLUMN,
        });

        if (selectionArgs == null)
            return cursor;

        String sortColumn = FILE_NAME_COLUMN;
        String sortOperator = "asc";
        if (sortOrder != null) {
            Pattern p = Pattern.compile("^(.*?)\\s*?(asc|desc).*?\\b");
            Matcher m = p.matcher(sortOrder);
            if(m.find()){
                MatchResult mr=m.toMatchResult();
                sortColumn = mr.group(1);
                sortOperator = mr.group(2);
            }
        }
        Comparator comparator = new FileNameComparatorAsc();
        if (sortColumn.equalsIgnoreCase(FILE_NAME_COLUMN)) {
            if (sortOperator.equalsIgnoreCase("asc"))
                comparator = new FileNameComparatorAsc();
            else
                comparator = new FileNameComparatorDsc();

        } else if (sortColumn.equalsIgnoreCase(FILE_SIZE_COLUMN)) {
            if (sortOperator.equalsIgnoreCase("asc"))
                comparator = new FileSizeComparatorAsc();
            else
                comparator = new FileSizeComparatorDsc();

        } else if (sortColumn.equalsIgnoreCase(FILE_LAST_MODIFICATION_DATE_COLUMN)) {
            if (sortOperator.equalsIgnoreCase("asc"))
                comparator = new FileDateComparatorAsc();
            else
                comparator = new FileDateComparatorDsc();
        }

        if (selectionArgs.length == 1) {
            int i = 0;
            Uri pathUri = Uri.parse(selectionArgs[0]);
            ArrayList<File> files = FileUtils.listFiles(pathUri.getPath(), UriHelper.LAUNCHABLE_EXTENSIONS, true);
            Collections.sort(files, comparator);
            for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
                Uri fileUri = Uri.fromFile(it.next());
                cursor.addRow(new Object[]{
                        i,
                        fileUri
                });
                i++;
            }

        } else if (selectionArgs.length == 2) {
            int i = 0;
            Uri pathUri = Uri.parse(selectionArgs[0]);
            ArrayList<File> files = FileUtils.listFiles(pathUri.getPath(), UriHelper.LAUNCHABLE_EXTENSIONS, true);
            Collections.sort(files, comparator);
            for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
                File file = it.next();
                Uri fileUri = Uri.fromFile(file);
                if (fileUri.getLastPathSegment() != null &&
                        fileUri.getLastPathSegment().toString().contains(selectionArgs[1])) {
                    cursor.addRow(new Object[]{
                            i,
                            fileUri
                    });
                    i++;
                }
            }
        }

		// Return the result Cursor.
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (selectionArgs == null)
            return 0;

        Uri fileUri = Uri.parse(selectionArgs[0]);
        if (FileUtils.deleteFile(fileUri.getPath()))
            return 1;

        return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        throw new IllegalArgumentException("Unsupported operation");
    }

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new IllegalArgumentException("Unsupported operation");
	}

    private class FileNameComparatorAsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    private class FileNameComparatorDsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return rhs.getName().compareTo(lhs.getName());
        }
    }

    private class FileDateComparatorAsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            Long lhsL = new Long(lhs.lastModified());
            Long rhsL = new Long(rhs.lastModified());

            return lhsL.compareTo(rhsL);
        }
    }

    private class FileDateComparatorDsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            Long lhsL = new Long(lhs.lastModified());
            Long rhsL = new Long(rhs.lastModified());

            return rhsL.compareTo(lhsL);
        }
    }

    private class FileSizeComparatorAsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            Long lhsL = new Long(lhs.length());
            Long rhsL = new Long(rhs.length());

            return lhsL.compareTo(rhsL);
        }
    }

    private class FileSizeComparatorDsc implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            Long lhsL = new Long(lhs.length());
            Long rhsL = new Long(rhs.length());

            return rhsL.compareTo(lhsL);
        }
    }

}