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
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.android.demo.roommateledger.HomeDbAdapter;
import com.android.demo.roommateledger.LedgerDbAdapter;
import com.android.demo.roommateledger.PaymentEdit;
import com.android.demo.roommateledger.PaymentsDbAdapter;
import com.android.demo.roommateledger.R;

public class Payments extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private PaymentsDbAdapter mDbHelper;
    private LedgerDbAdapter mLedgerDbHelper;
    private Long mLedgerId;

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
        setContentView(R.layout.payment_list);
        mDbHelper = new PaymentsDbAdapter(this);
        mDbHelper.open();
        mLedgerDbHelper = new LedgerDbAdapter(this);
        mLedgerDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        Cursor paymentsCursor = mDbHelper.fetchAllPayments(mLedgerId);
        startManagingCursor(paymentsCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{PaymentsDbAdapter.KEY_TITLE, PaymentsDbAdapter.KEY_AMOUNT};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter payments =
                new SimpleCursorAdapter(this, R.layout.payment_row, paymentsCursor, from, to);
        setListAdapter(payments);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_payment_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createPayment();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete_payment);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deletePayment(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createPayment() {
        Intent i = new Intent(this, PaymentEdit.class);
        i.putExtra(PaymentsDbAdapter.KEY_LEDGER_ID, mLedgerId);
        i.putExtra(PaymentsDbAdapter.KEY_ROWID, -1L);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, PaymentEdit.class);
        i.putExtra(PaymentsDbAdapter.KEY_LEDGER_ID, mLedgerId);
        i.putExtra(PaymentsDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}