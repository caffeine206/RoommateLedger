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

import java.util.List;

public class Ledger extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int PAYMENT_ID = Menu.FIRST + 1;
    private static final int BALANCE_ID = Menu.FIRST + 2;
    private static final int DELETE_ID = Menu.FIRST;

    private LedgerDbAdapter mDbHelper;
    private Long mLedgerId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLedgerId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(HomeDbAdapter.KEY_ROWID);
        if (mLedgerId == null) {
            Bundle extras = getIntent().getExtras();
            mLedgerId = extras != null ? extras.getLong(HomeDbAdapter.KEY_ROWID)
                    : null;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_list);
        mDbHelper = new LedgerDbAdapter(this);
        mDbHelper.open();
        fillData();
        updateTotal();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        Cursor purchasesCursor = mDbHelper.fetchAllPurchases(mLedgerId);
        startManagingCursor(purchasesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{LedgerDbAdapter.KEY_TITLE, LedgerDbAdapter.KEY_AMOUNT};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter purchases =
                new SimpleCursorAdapter(this, R.layout.purchase_row, purchasesCursor, from, to);
        setListAdapter(purchases);
    }

    private void updateTotal() {
        double total = mDbHelper.fetchTotalOfPurchases(mLedgerId);
        TextView t = (TextView)findViewById(R.id.textViewFooter2);
        t.setText("$" + String.valueOf(total));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, PAYMENT_ID, 0, R.string.manage_payments);
        menu.add(0, BALANCE_ID, 0, R.string.view_balances);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createPurchase();
                return true;
            case PAYMENT_ID:
                Intent i = new Intent(this, Payments.class);
                i.putExtra(HomeDbAdapter.KEY_ROWID, mLedgerId);
                startActivity(i);
            case BALANCE_ID:
                Intent j = new Intent(this, Balances.class);
                j.putExtra(HomeDbAdapter.KEY_ROWID, mLedgerId);
                startActivity(j);
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deletePurchase(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createPurchase() {
        Intent i = new Intent(this, PurchaseEdit.class);
        i.putExtra(LedgerDbAdapter.KEY_LEDGER_ID, mLedgerId);
        i.putExtra(LedgerDbAdapter.KEY_ROWID, -1L);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, PurchaseEdit.class);
        i.putExtra(LedgerDbAdapter.KEY_LEDGER_ID, mLedgerId);
        i.putExtra(LedgerDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        updateTotal();
    }
}
