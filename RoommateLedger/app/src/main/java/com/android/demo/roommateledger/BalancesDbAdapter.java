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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class BalancesDbAdapter {
    private static final String MEMBERS_DATABASE_TABLE = "members";
    private static final String PURCHASES_DATABASE_TABLE = "purchases";
    private static final String PAYMENTS_DATABASE_TABLE = "payments";
    public static final String KEY_AMOUNT = "amount";
    private static final String KEY_MEMBER_ID = "_id";
    private static final String KEY_MEMBER_NAME = "member";
    public static final String KEY_FROM_MEMBER_ID = "from_member_id";
    public static final String KEY_TO_MEMBER_ID = "to_member_id";

    private LedgerDbAdapter mLedgerDbHelper;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public BalancesDbAdapter(Context ctx) {
        this.mCtx = ctx;
        mLedgerDbHelper = new LedgerDbAdapter(ctx);
        mLedgerDbHelper.open();
    }

    /**
     * Open the purchases database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public BalancesDbAdapter open() throws SQLException {
        mDbHelper = DatabaseHelper.getInstance(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }



    /**
     * Return a Cursor over the list of roommates for the given ledger_id in the database
     *
     * @return Cursor over all roommates
     */
    public int getRoommateCount(long ledger_id) {
        return mLedgerDbHelper.fetchAllRoommates(ledger_id).size();
    }

    public Map<Integer, Roommate> getAllRoommates(long ledger_id) {
        Cursor mCursor =
                mDb.query(MEMBERS_DATABASE_TABLE, new String[]{KEY_MEMBER_ID, KEY_MEMBER_NAME},
                        "ledger_id = ?",
                        new String[]{String.valueOf(ledger_id)}, null, null, null);
        Map<Integer, Roommate> roommates = new HashMap<Integer, Roommate>();
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            Roommate roommate = new Roommate();
            int id = (mCursor.getInt((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_MEMBER_ID))));
            roommate.id = id;
            roommate.name = (mCursor.getString((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_MEMBER_NAME))));
            roommates.put(id, roommate);
        }
        return roommates;
    }

    public double getTotalOfPurchases(int member_id) {
        Cursor mCursor =
                mDb.query(PURCHASES_DATABASE_TABLE, new String[]{KEY_AMOUNT}, "member_id = ?",
                        new String[]{String.valueOf(member_id)}, null, null, null);
        double result = 0;
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            result += (mCursor.getDouble((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_AMOUNT))));
        }
        return result;
    }

    public List<Payment> getAllPayments(long ledger_id) {
        Cursor mCursor =
                mDb.query(PAYMENTS_DATABASE_TABLE, new String[]{KEY_FROM_MEMBER_ID, KEY_TO_MEMBER_ID, KEY_AMOUNT},
                        "ledger_id = ?",
                        new String[]{String.valueOf(ledger_id)}, null, null, null);
        List<Payment> payments = new ArrayList<Payment>();
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            Payment payment = new Payment();
            payment.from = (mCursor.getInt((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_FROM_MEMBER_ID))));
            payment.to = (mCursor.getInt((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_TO_MEMBER_ID))));
            payment.amount = (mCursor.getDouble((mCursor.getColumnIndexOrThrow(BalancesDbAdapter.KEY_AMOUNT))));
            payments.add(payment);
        }
        return payments;
    }
}
