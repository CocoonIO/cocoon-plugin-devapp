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

public class FavoritesContentProvider extends ContentProvider {
	
	public static String AUTHORITY = "com.ludei.devapplib.android.provider.favorites.authority";

	public static Uri FAVORITES_URI = Uri.parse("content://com.ludei.devapplib.android.provider.favorites.authority/favorites");

	protected static final String FAVORITES_TABLE = "Favorites";

	public static final String FAVORITES_ID_COLUMN = "_id";
	public static final String FAVORITES_URI_COLUMN = "url";
    public static final String FAVORITES_NAME_COLUMN = "name";
    public static final String FAVORITES_DATE_COLUMN = "date";
	
	private static final int ALL_FAVORITES = 1;
	private static final int SINGLE_FAVORITES = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	}

	protected static final String DATABASE_FAVORITES_CREATE = "create table "
			+ FAVORITES_TABLE + " ( " 
			+ FAVORITES_ID_COLUMN + " integer primary key autoincrement" + ", " 
			+ FAVORITES_URI_COLUMN + " text, "
            + FAVORITES_NAME_COLUMN + " text, "
            + FAVORITES_DATE_COLUMN + " integer"
			+ " );";

	private DatabaseHelper myOpenHelper;

	@Override
	public boolean onCreate() {
		try {
			String packageName = this.getContext().getPackageManager().getPackageInfo(this.getContext().getPackageName(), 0).packageName;

			AUTHORITY = packageName + ".provider.favorites.authority";
			FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/favorites");

			uriMatcher.addURI(AUTHORITY, "favorites", ALL_FAVORITES);
			uriMatcher.addURI(AUTHORITY, "favorites/#", SINGLE_FAVORITES);

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
		case ALL_FAVORITES:
		case SINGLE_FAVORITES:
			return FAVORITES_TABLE;
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
		case ALL_FAVORITES:
		case SINGLE_FAVORITES:
			return FAVORITES_URI;
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
			db = myOpenHelper.getReadableDatabase();
		}

		// Replace these with valid SQL statements if necessary.
		String groupBy = null;
		String having = null;

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case SINGLE_FAVORITES:
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(FAVORITES_ID_COLUMN + "=" + rowID);
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
		case ALL_FAVORITES:
			return "vnd.android.cursor.dir/vnd.com.ludei.devapp.android.provider.favorites";
		case SINGLE_FAVORITES:
			return "vnd.android.cursor.dir/vnd.com.ludei.devapp.android.provider.favorites";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		switch (uriMatcher.match(uri)) {
		case SINGLE_FAVORITES:
			String rowID = uri.getPathSegments().get(1);
			selection = FAVORITES_ID_COLUMN
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
		case SINGLE_FAVORITES:
			String rowID = uri.getPathSegments().get(1);
			selection = FAVORITES_ID_COLUMN
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