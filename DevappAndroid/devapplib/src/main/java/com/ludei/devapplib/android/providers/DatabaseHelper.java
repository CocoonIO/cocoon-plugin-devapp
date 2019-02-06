package com.ludei.devapplib.android.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "developer_app.db";
    protected static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	// Called when no database exists in disk and the helper class needs
	// to create a new one.
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(HistoryContentProvider.DATABASE_HISTORY_CREATE);
        db.execSQL(FavoritesContentProvider.DATABASE_FAVORITES_CREATE);
        db.execSQL(DemosContentProvider.DATABASE_DEMOS_CREATE);
	}

	// Called when there is a database version mismatch meaning that the
	// version
	// of the database on disk needs to be upgraded to the current version.
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// The simplest case is to drop the old table and create a new one.
		db.execSQL("DROP TABLE IF EXISTS " + HistoryContentProvider.HISTORY_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesContentProvider.FAVORITES_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + DemosContentProvider.DATABASE_DEMOS_CREATE + ";");

		// Create a new one.
		onCreate(db);
	}
	
}
