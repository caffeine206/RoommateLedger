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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class PaymentEdit extends Activity {

    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mAmountText;
    private Long mRowId;
    private Long mLedgerId;
    private PaymentsDbAdapter mDbHelper;
    private Spinner mSpinner1;
    private Spinner mSpinner2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PaymentsDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.payment_edit);
        setTitle(R.string.edit_payment);

        mTitleText = (EditText) findViewById(R.id.title);
        mDescriptionText = (EditText) findViewById(R.id.description);
        mAmountText = (EditText) findViewById(R.id.amount);
        mSpinner1 = (Spinner) findViewById(R.id.spinner1);
        mSpinner2 = (Spinner) findViewById(R.id.spinner2);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(PaymentsDbAdapter.KEY_ROWID);
        mLedgerId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(PaymentsDbAdapter.KEY_LEDGER_ID);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Long id = extras.getLong(LedgerDbAdapter.KEY_ROWID);
            if (mRowId == null && id != -1) {
                mRowId = id;
            }
            if (mLedgerId == null) {
                mLedgerId = extras.getLong(LedgerDbAdapter.KEY_LEDGER_ID);
            }
        }

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mLedgerId != null) {
            List<String> members = mDbHelper.fetchAllRoommates(mLedgerId);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, members);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // attaching data adapter to spinner
            mSpinner1.setAdapter(dataAdapter);
            mSpinner2.setAdapter(dataAdapter);
        }
        if (mRowId != null) {
            Cursor purchase = mDbHelper.fetchPayment(mRowId);
            startManagingCursor(purchase);
            if (purchase != null && purchase.moveToFirst()) {
                mTitleText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(PaymentsDbAdapter.KEY_TITLE)));
                mDescriptionText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(PaymentsDbAdapter.KEY_DESCRIPTION)));
                mAmountText.setText(purchase.getString(
                        purchase.getColumnIndexOrThrow(PaymentsDbAdapter.KEY_AMOUNT)));
                String text = (purchase.getString(
                        purchase.getColumnIndexOrThrow(PaymentsDbAdapter.KEY_FROM_MEMBER_ID)));
                int offset = mDbHelper.getCountBefore(mLedgerId);
                mSpinner1.setSelection(Integer.valueOf(text) - 1 - offset);
                text = (purchase.getString(
                        purchase.getColumnIndexOrThrow(PaymentsDbAdapter.KEY_TO_MEMBER_ID)));
                mSpinner2.setSelection(Integer.valueOf(text) - 1 - offset);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(PaymentsDbAdapter.KEY_ROWID, mRowId);
        outState.putSerializable(PaymentsDbAdapter.KEY_LEDGER_ID, mLedgerId);
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
        String from = mSpinner1.getSelectedItem().toString();
        String to = mSpinner2.getSelectedItem().toString();
        double amount = Double.parseDouble(mAmountText.getText().toString());

        if (mRowId == null) {
            long id = mDbHelper.createPayment(title, description, from, to, amount, mLedgerId);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updatePayment(title, description, from, to, amount, mRowId, mLedgerId);
        }
    }
}
