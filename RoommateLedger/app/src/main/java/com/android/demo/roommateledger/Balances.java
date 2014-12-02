/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.roommateledger;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Balances extends ListActivity {
    private BalancesDbAdapter mDbHelper;
    private LedgerDbAdapter mLedgerDbHelper;
    private Long mLedgerId;
    private Map<Integer, Roommate> mRoommates;
    private List<Payment> mPayments;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLedgerId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(LedgerDbAdapter.KEY_ROWID);
        if (mLedgerId == null) {
            Bundle extras = getIntent().getExtras();
            mLedgerId = extras != null ? extras.getLong(LedgerDbAdapter.KEY_ROWID)
                    : null;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_list);
        mDbHelper = new BalancesDbAdapter(this);
        mDbHelper.open();
        mLedgerDbHelper = new LedgerDbAdapter(this);
        mLedgerDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        int numRoommates = mDbHelper.getRoommateCount(mLedgerId);
        double purchaseTotal = mLedgerDbHelper.fetchTotalOfPurchases(mLedgerId);
        double expectedContribution = purchaseTotal / numRoommates;
        mRoommates = mDbHelper.getAllRoommates(mLedgerId);
        for (int id : mRoommates.keySet()) {
            mRoommates.get(id).balance = expectedContribution - mDbHelper.getTotalOfPurchases(id);
        }
        mPayments = mDbHelper.getAllPayments(mLedgerId);
        for (Payment payment : mPayments) {
            mRoommates.get(payment.from).balance = mRoommates.get(payment.from).balance - payment.amount;
            mRoommates.get(payment.to).balance = mRoommates.get(payment.to).balance + payment.amount;
        }
        ArrayList<Roommate> roommates = new ArrayList<Roommate>();
        roommates.addAll(mRoommates.values());
        BalanceAdapter adapter = new BalanceAdapter(this, R.layout.balance_row, roommates);
        setListAdapter(adapter);
    }
}