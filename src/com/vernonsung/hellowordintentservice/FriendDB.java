package com.vernonsung.hellowordintentservice;

import java.util.Calendar;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class FriendDB {
	// Constant
	private static final String LOG_TAG = "Debug";
	private final FriendListOpenHelper mFriendListOpenHelper;

	/* Inner class that defines the table contents
	 * By implementing the BaseColumns interface, 
	 * your inner class can inherit a primary key field called _ID 
	 * that some Android classes such as cursor adaptors will expect it to have. 
	 * It's not required, 
	 * but this can help your database work harmoniously with the Android framework.
	 */
	public static abstract class FriendPeople implements BaseColumns {
		public static final String TABLE_NAME = "people";
		public static final String COLUMN_NAME_UUID = "uuid";
		public static final String COLUMN_NAME_TIME = "time";
	}
	
    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public FriendDB(Context context) {
		Log.d(LOG_TAG, "Enter FriendDB constructor");
		// Create database
    	mFriendListOpenHelper = new FriendListOpenHelper(context);
    }

    // Debug
    public long insertRandom() {
    	// Gets the data repository in write mode
    	SQLiteDatabase db = mFriendListOpenHelper.getWritableDatabase();

    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(FriendPeople.COLUMN_NAME_UUID, new Random().nextInt(100));
    	values.put(FriendPeople.COLUMN_NAME_TIME, Calendar.getInstance().getTimeInMillis());

    	// Insert the new row, returning the primary key value of the new row
    	long newRowId = db.insert(FriendPeople.TABLE_NAME, null, values);
    	
    	return newRowId;
    }

    /**
     * Update the together time of a friend
     * @param uuid
     * The UUID of the friend
     * @param time
     * The time (minutes) we get together more than before
     * @return
     * If update DB failed, return false so that caller won't reset the timer. 
     * Otherwise, return true.
     */
    public boolean updateFriend(long uuid, long time) {
    	SQLiteDatabase db = mFriendListOpenHelper.getWritableDatabase();
    	long sequentialId = 0;
    	long originalTime = 0;
    	
    	// Query existing record
		String[] projection = { 
				FriendPeople._ID, 
				FriendPeople.COLUMN_NAME_TIME };
		String selection = FriendPeople.COLUMN_NAME_UUID + " = ?";
		String[] selectionArgs = { String.valueOf(uuid) };

		Cursor c = db.query(FriendPeople.TABLE_NAME, // The table to query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null); // don't sort order
		if (c.moveToFirst() == true) {
			try {
				originalTime = c.getLong(c.getColumnIndexOrThrow(FriendPeople.COLUMN_NAME_TIME));
				sequentialId = c.getLong(c.getColumnIndexOrThrow(FriendPeople._ID));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		if (sequentialId != 0) {
	    	// Update existing record
			long newTime = originalTime + time;
	    	ContentValues updateValues = new ContentValues();
	    	updateValues.put(FriendPeople.COLUMN_NAME_TIME, newTime);
	    	String updateClause = FriendPeople._ID + " = ?";
	    	String [] updateArgs = { String.valueOf(sequentialId) };
	    	int updateRowNum = db.update(FriendPeople.TABLE_NAME, updateValues, updateClause, updateArgs);
	    	if (updateRowNum != 1) {
	    		Log.w(LOG_TAG, "Update " + String.valueOf(updateRowNum) + " rows in DB at one time.");
	    		return false;
	    	}
		} else {
	    	// Insert a new record
	    	ContentValues insertValues = new ContentValues();
	    	insertValues.put(FriendPeople.COLUMN_NAME_UUID, uuid);
	    	insertValues.put(FriendPeople.COLUMN_NAME_TIME, time);
	    	long newRowId = db.insert(FriendPeople.TABLE_NAME, null, insertValues);
	    	if (newRowId == -1) {
	    		Log.w(LOG_TAG, "Insert new record into DB failed.");
	    		return false;
	    	}
		}

		return true;
    }
    
    /**
     * Get all friends information
     * @return
     * The cursor points to rows of friends information
     */
    public Cursor getFriends() {
    	// Query existing record
    	SQLiteDatabase db = mFriendListOpenHelper.getReadableDatabase();
    	String[] projection = null;
    	String selection = null;
    	String[] selectionArgs = null;
    	String sortOrder = FriendPeople.COLUMN_NAME_TIME + " DESC";

    	Cursor c = db.query(
    	    FriendPeople.TABLE_NAME,  // The table to query
    	    projection,               // The columns to return
    	    selection,                // The columns for the WHERE clause
    	    selectionArgs,            // The values for the WHERE clause
    	    null,                     // don't group the rows
    	    null,                     // don't filter by row groups
    	    sortOrder                 // The sort order
    	);
    	if (c.moveToFirst() == false) {
    		return null;
    	} else {
    		return c;
    	}
    }
    
    // Debug
    public int executeSample() {
    	int i = 0;
    	
    	// Gets the data repository in write mode
    	SQLiteDatabase db = mFriendListOpenHelper.getWritableDatabase();
    	
    	/*
    	 * Delete
    	 */
    	int deleteRowNum = db.delete(FriendPeople.TABLE_NAME, FriendPeople._ID + "> ?", new String []{"0"});
    	
    	/*
    	 * Write
    	 */
    	// Create a new map of values, where column names are the keys
    	for (i = 0; i < 10; i++) {
	    	ContentValues values = new ContentValues();
	    	values.put(FriendPeople.COLUMN_NAME_UUID, new Random().nextInt(20));
	    	values.put(FriendPeople.COLUMN_NAME_TIME, Calendar.getInstance().getTimeInMillis());
    	
	    	// Insert the new row, returning the primary key value of the new row
	    	long newRowId = db.insert(FriendPeople.TABLE_NAME, null, values);
    	}
    	
    	/*
    	 * Read
    	 */
    	
    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    FriendPeople._ID,
    	    FriendPeople.COLUMN_NAME_UUID,
    	    FriendPeople.COLUMN_NAME_TIME
    	};
    	String selection = FriendPeople.COLUMN_NAME_UUID + " > ?";
    	String[] selectionArgs = {"0"};
    	
    	// How you want the results sorted in the resulting Cursor
    	String sortOrder = FriendPeople.COLUMN_NAME_UUID + " ASC";

    	Cursor c = db.query(
    	    FriendPeople.TABLE_NAME,  // The table to query
    	    projection,                               // The columns to return
    	    selection,                                // The columns for the WHERE clause
    	    selectionArgs,                            // The values for the WHERE clause
    	    null,                                     // don't group the rows
    	    null,                                     // don't filter by row groups
    	    sortOrder                                 // The sort order
    	);
    	
    	int uuid = 0;
    	
    	if (c.moveToFirst() == false) {
    		uuid = -1;
    	} else {
	    	try {
				uuid = c.getInt(c.getColumnIndexOrThrow(FriendPeople.COLUMN_NAME_UUID));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	/*
    	 * Update
    	 */
    	ContentValues updateValues = new ContentValues();
    	updateValues.put(FriendPeople.COLUMN_NAME_TIME, 12345);
    	String updateClause = FriendPeople.COLUMN_NAME_UUID + " > ?";
    	String [] updateArgs = {"10"};
    	int updateRowNum = db.update(FriendPeople.TABLE_NAME, updateValues, updateClause, updateArgs);
    	
    	return c.getCount();
    }
    
    public class FriendListOpenHelper extends SQLiteOpenHelper {
    	public static final int DATABASE_VERSION = 2;
    	public static final String DATABASE_NAME = "friend.db";
        private static final String SQL_CREATE_PEOPLE =
                "CREATE TABLE " + FriendPeople.TABLE_NAME + " (" +
                FriendPeople._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FriendPeople.COLUMN_NAME_UUID + " INTEGER NOT NULL, " +
                FriendPeople.COLUMN_NAME_TIME + " INTEGER NOT NULL);";
        private static final String SQL_DELETE_PEOPLE =
        		"DROP TABLE IF EXISTS " + FriendPeople.TABLE_NAME;
        
    	public FriendListOpenHelper(Context context) {
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    		Log.d(LOG_TAG, "Enter FriendListOpenHelper constructor");
    	}

    	@Override
    	public void onCreate(SQLiteDatabase db) {
    		db.execSQL(SQL_CREATE_PEOPLE);
    		Log.d(LOG_TAG, "Table friend created");
    	}

    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		db.execSQL(SQL_DELETE_PEOPLE);
            onCreate(db);
    	}

    }
}
