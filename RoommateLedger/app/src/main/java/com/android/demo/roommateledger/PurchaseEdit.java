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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PurchaseEdit extends Activity {

    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mAmountText;
    private Long mRowId;
    private LedgerDbAdapter mDbHelper;

    private Long ledger_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new LedgerDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.purchase_edit);
        setTitle(R.string.edit_purchase);

        mTitleText = (EditText) findViewById(R.id.title);
        mDescriptionText = (EditText) findViewById(R.id.description);
        mAmountText = (EditText) findViewById(R.id.amount);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(LedgerDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(LedgerDbAdapter.KEY_ROWID)
									: null;

		}
        ledger_id = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(HomeDbAdapter.KEY_ROWID);
        if (ledger_id == null) {
            Bundle extras = getIntent().getExtras();
            ledger_id = extras != null ? extras.getLong(HomeDbAdapter.KEY_ROWID)
                    : null;
        }
        System.out.println("mRowId = " + mRowId);
        System.out.println("ledger_id = " + ledger_id);

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null && ledger_id != null) {
            Cursor purchase = mDbHelper.fetchPurchase(mRowId);
            startManagingCursor(purchase);
            if (purchase != null && purchase.moveToFirst()) {
                mTitleText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(LedgerDbAdapter.KEY_TITLE)));
                mDescriptionText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(LedgerDbAdapter.KEY_DESCRIPTION)));
                mAmountText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(LedgerDbAdapter.KEY_AMOUNT)));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(LedgerDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String description = mDescriptionText.getText().toString();
        double amount = Double.parseDouble(mAmountText.getText().toString());

        if (mRowId == null) {
            long id = mDbHelper.createPurchase(title, description, amount, ledger_id);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updatePurchase(mRowId, title, description, amount, ledger_id);
        }
    }

}
