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

/**
 * Simple purchases database access helper class. Defines the basic CRUD operations
 * for the purchase example, and gives the ability to list all purchases as well as
 * retrieve or modify a specific purchase.
 * <p/>
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class LedgerDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_LEDGER_ID = "ledger_id";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "LedgerDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_TABLE = "purchases";

    private final Context mCtx;
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public LedgerDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the purchases database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public LedgerDbAdapter open() throws SQLException {
        mDbHelper = DatabaseHelper.getInstance(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new purchase using the title, description, and amount provided. If the purchase is
     * successfully created return the new rowId for that purchase, otherwise return
     * a -1 to indicate failure.
     *
     * @param title       the title of the purchase
     * @param description the description of the purchase
     * @param amount      the amount of the purchase
     * @return rowId or -1 if failed
     */
    public long createPurchase(String title, String description, double amount, long ledger_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DESCRIPTION, description);
        initialValues.put(KEY_AMOUNT, amount);
        initialValues.put(KEY_LEDGER_ID, ledger_id);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the purchase with the given rowId
     *
     * @param rowId id of purchase to delete
     * @return true if deleted, false otherwise
     */
    public boolean deletePurchase(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all purchases in the database
     *
     * @return Cursor over all purchases
     */
    public Cursor fetchAllPurchases(long ledger_id) {
        Cursor mCursor = mDb.rawQuery("SELECT _id, title, amount FROM " + DATABASE_TABLE + " WHERE ledger_id = ?",
                new String[] {String.valueOf(ledger_id)});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Return a Cursor positioned at the purchase that matches the given rowId
     *
     * @param rowId id of purchase to retrieve
     * @return Cursor positioned to matching purchase, if found
     * @throws android.database.SQLException if purchase could not be found/retrieved
     */
    public Cursor fetchPurchase(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,
                                KEY_TITLE, KEY_DESCRIPTION, KEY_AMOUNT}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the purchase using the details provided. The purchase to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of purchase to update
     * @param title value to set purchase title to
     * @param body  value to set purchase body to
     * @return true if the purchase was successfully updated, false otherwise
     */
    public boolean updatePurchase(long rowId, String title, String body, double amount, long ledger_id) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_DESCRIPTION, body);
        args.put(KEY_AMOUNT, amount);
        args.put(KEY_LEDGER_ID, ledger_id);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public double fetchTotalOfPurchases(long ledger_id) {
        String sql = "SELECT SUM(" + KEY_AMOUNT + ") FROM " + DATABASE_TABLE +
                " WHERE ledger_id=" + ledger_id;
        Cursor cursor = mDb.rawQuery(sql, null);
        if(cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0;
    }

}
