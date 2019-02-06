package com.ludei.devapplib.android.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DemosContentProvider extends ContentProvider {

	public static String AUTHORITY = "com.ludei.devapplib.android.provider.demos.authority";

	public static Uri DEMO_URI = Uri.parse("content://com.ludei.devapplib.android.provider.demos.authority/demo");

	protected static final String DEMOS_TABLE = "Demos";

	public static final String DEMO_ID_COLUMN = "_id";
	public static final int DEMO_ID_COLUMN_POSITION = 1;
	public static final String DEMO_NAME_COLUMN = "name";
	public static final int DEMO_NAME_COLUMN_POSITION = 2;
	public static final String DEMO_DESCRIPTION_COLUMN = "description";
	public static final int DEMO_DESCRIPTION_COLUMN_POSITION = 3;
    public static final String DEMO_IMAGE_URL_COLUMN = "image_url";
    public static final int DEMO_IMAGE_URL_COLUMN_POSITION = 4;
    public static final String DEMO_GITHUB_URL_COLUMN = "github_url";
    public static final int DEMO_GITHUB_URL_COLUMN_POSITION = 5;
    public static final String DEMO_WEB_COLUMN = "web";
    public static final int DEMO_WEB_COLUMN_POSITION = 6;
	public static final String DEMO_ORIENTATION_COLUMN = "orientation";
	public static final int DEMO_ORIENTATION_COLUMN_POSITION = 7;
    public static final String DEMO_ENVIRONMENT_COLUMN = "environment";
    public static final int DEMO_ENVIRONMENT_COLUMN_POSITION = 8;

	private static final int ALL_DEMOS = 1;
	private static final int SINGLE_DEMO = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	}

	protected static final String DATABASE_DEMOS_CREATE = "create table "
			+ DEMOS_TABLE + " ( " 
			+ DEMO_ID_COLUMN + " integer primary key autoincrement" + ", "
			+ DEMO_NAME_COLUMN + " text" + ", "
			+ DEMO_DESCRIPTION_COLUMN + " text" + ", "
			+ DEMO_IMAGE_URL_COLUMN + " text" + ", "
			+ DEMO_GITHUB_URL_COLUMN + " text" + ", "
			+ DEMO_WEB_COLUMN + " boolean" + ", "
			+ DEMO_ORIENTATION_COLUMN + " text" + ", "
            + DEMO_ENVIRONMENT_COLUMN + " text"
			+ " );";

	private DatabaseHelper myOpenHelper;

	@Override
	public boolean onCreate() {
		try {
			String packageName = this.getContext().getPackageManager().getPackageInfo(this.getContext().getPackageName(), 0).packageName;

			AUTHORITY = packageName + ".provider.demos.authority";
			DEMO_URI = Uri.parse("content://" + AUTHORITY + "/demo");

			uriMatcher.addURI(AUTHORITY, "demo", ALL_DEMOS);
			uriMatcher.addURI(AUTHORITY, "demo/#", SINGLE_DEMO);

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		myOpenHelper = new DatabaseHelper(getContext(), DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION);
		return true;
	}

	/**
	 * Returns the right table name for the given uri
	 * 
	 * @param uri
	 * @return
	 */
	private String getTableNameFromUri(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALL_DEMOS:
		case SINGLE_DEMO:
			return DEMOS_TABLE;
		default:
			break;
		}

		return null;
	}

	/**
	 * Returns the parent uri for the given uri
	 * 
	 * @param uri
	 * @return
	 */
	private Uri getContentUriFromUri(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALL_DEMOS:
		case SINGLE_DEMO:
			return DEMO_URI;
		default:
			break;
		}

		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Open the database.
		SQLiteDatabase db;
		try {
			db = myOpenHelper.getWritableDatabase();
			
		} catch (SQLiteException ex) {
			try {
				db = myOpenHelper.getReadableDatabase();
				
			} catch (SQLiteException e) {
				return null;
			}
		}

		// Replace these with valid SQL statements if necessary.
		String groupBy = null;
		String having = null;

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case SINGLE_DEMO:
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(DEMO_ID_COLUMN + "=" + rowID);
		default:
			break;
		}

		// Specify the table on which to perform the query. This can
		// be a specific table or a join as required.
		queryBuilder.setTables(getTableNameFromUri(uri));

		// Execute the query.
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
		if (cursor != null)
			cursor.setNotificationUri(getContext().getContentResolver(), uri);

		// Return the result Cursor.
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// Return a string that identifies the MIME type
		// for a Content Provider URI
		switch (uriMatcher.match(uri)) {
		case ALL_DEMOS:
			return "vnd.android.cursor.dir/vnd.com.ludei.devapplib.android.provider.demo";
		case SINGLE_DEMO:
			return "vnd.android.cursor.dir/vnd.com.ludei.devapplib.android.provider.demo";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		switch (uriMatcher.match(uri)) {
		case SINGLE_DEMO:
			String rowID = uri.getPathSegments().get(1);
			selection = DEMO_ID_COLUMN
					+ "="
					+ rowID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : "");
		default:
			break;
		}

		if (selection == null)
			selection = "1";

		int deleteCount = db.delete(getTableNameFromUri(uri), selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return deleteCount;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		String nullColumnHack = null;
		long id = db.replace(getTableNameFromUri(uri), nullColumnHack, values);
//		long id = db.insert(getTableNameFromUri(uri), nullColumnHack, values);
		if (id > -1) {
			Uri insertedId = ContentUris.withAppendedId(getContentUriFromUri(uri), id);
			getContext().getContentResolver().notifyChange(insertedId, null);
			
			return insertedId;
			
		} else {
			return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		// If this is a row URI, limit the deletion to the specified row.
		switch (uriMatcher.match(uri)) {
		case SINGLE_DEMO:
			String rowID = uri.getPathSegments().get(1);
			selection = DEMO_ID_COLUMN
					+ "="
					+ rowID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : "");
		default:
			break;
		}

		// Perform the update.
		int updateCount = db.update(getTableNameFromUri(uri), values, selection, selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);

		return updateCount;
	}

}