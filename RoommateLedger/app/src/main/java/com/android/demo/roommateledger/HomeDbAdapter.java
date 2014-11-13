/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.demo.roommateledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Simple ledgers database access helper class. Defines the basic CRUD operations
 * for the ledger example, and gives the ability to list all ledgers as well as
 * retrieve or modify a specific ledger.
 * <p/>
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class HomeDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_MEMBER = "member";
    public static final String KEY_LEDGER_ID = "ledger_id";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ROWID_MEMBER = "_id";

    private static final String TAG = "HomeDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table ledgers (_id integer primary key autoincrement, "
                    + "title text not null, description text not null);";

    private static final String MEMBER_DATABASE_CREATE =
            "create table members (_id integer primary key autoincrement, "
                    + "ledger_id integer not null, member text not null, "
                    + "FOREIGN KEY (ledger_id) REFERENCES ledgers(_id));";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "ledgers";
    private static final String MEMBERS_DATABASE_TABLE = "members";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(MEMBER_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS ledgers");
            db.execSQL("DROP TABLE IF EXISTS members");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public HomeDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the ledgers database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public HomeDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Create a new ledger using the title, description, and amount provided. If the ledger is
     * successfully created return the new rowId for that ledger, otherwise return
     * a -1 to indicate failure.
     *
     * @param title       the title of the ledger
     * @param description the description of the ledger
     * @return rowId or -1 if failed
     */
    public long createLedger(String title, String description, List<String> members) {
        System.out.println("create ledger called");
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DESCRIPTION, description);
        long ledger_id = mDb.insert(DATABASE_TABLE, null, initialValues);
        if (ledger_id == -1)
            return -1;
        updateMembers(ledger_id, members);
        return ledger_id;
    }

    /**
     * Delete the ledger with the given rowId
     *
     * @param rowId id of ledger to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteLedger(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Delete the member with the given rowId
     *
     * @param rowId id of ledger to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteMember(long rowId) {
        return mDb.delete(MEMBERS_DATABASE_TABLE, KEY_ROWID_MEMBER + "=" + rowId, null) > 0;
    }

    public boolean deleteMembers(long ledger_id) {
        return mDb.delete(MEMBERS_DATABASE_TABLE, KEY_LEDGER_ID + "=" + ledger_id, null) > 0;
    }

    /**
     * Return a Cursor over the list of all ledgers in the database
     *
     * @return Cursor over all ledgers
     */
    public Cursor fetchAllLedgers() {
        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
                KEY_DESCRIPTION}, null, null, null, null, null);
    }

    /**
     * Return a Cursor over the list of all ledgers in the database
     *
     * @return Cursor over all ledgers
     */
    public Cursor fetchAllMembers(long ledger_id) {
//        return mDb.query(MEMBERS_DATABASE_TABLE, new String[]{KEY_ROWID_MEMBER, KEY_LEDGER_ID,
//                KEY_MEMBER}, null, null, null, null, null);
        return mDb.rawQuery("SELECT member FROM " + MEMBERS_DATABASE_TABLE + " WHERE ledger_id = ?",
                new String[] {String.valueOf(ledger_id)});
    }

    /**
     * Return a Cursor positioned at the ledger that matches the given rowId
     *
     * @param rowId id of ledger to retrieve
     * @return Cursor positioned to matching ledger, if found
     * @throws android.database.SQLException if ledger could not be found/retrieved
     */
    public Cursor fetchLedger(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,
                                KEY_TITLE, KEY_DESCRIPTION}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Return a Cursor positioned at the member that matches the given rowId
     *
     * @param rowId id of member to retrieve
     * @return Cursor positioned to matching ledger, if found
     * @throws android.database.SQLException if ledger could not be found/retrieved
     */
    public Cursor fetchMember(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, MEMBERS_DATABASE_TABLE, new String[]{KEY_ROWID_MEMBER,
                                KEY_LEDGER_ID, KEY_MEMBER}, KEY_ROWID_MEMBER + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the ledger using the details provided. The ledger to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of ledger to update
     * @param title value to set ledger title to
     * @param description  value to set ledger body to
     * @return true if the ledger was successfully updated, false otherwise
     */
    public boolean updateLedger(long rowId, String title, String description,
                                List<String> members) {
        System.out.println("update ledger called");
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_DESCRIPTION, description);

        boolean updated = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        if (!updated)
            return false;

        updateMembers(rowId, members);

        return updated;
    }

    public boolean updateMembers(long ledger_id, List<String> members) {
        System.out.println("update members called");
        ContentValues initialValues;
        try {
            mDb.beginTransaction();
            deleteMembers(ledger_id);
            for (String member : members) {
                initialValues = new ContentValues();
                initialValues.put(KEY_MEMBER, member);
                initialValues.put(KEY_LEDGER_ID, ledger_id);

                mDb.insert(MEMBERS_DATABASE_TABLE, null, initialValues);
            }
            mDb.setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            mDb.endTransaction();
        }
        return true;
    }
}
