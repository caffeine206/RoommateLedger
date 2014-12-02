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

import java.util.ArrayList;
import java.util.List;

public class LedgerEdit extends Activity {

    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mRoommateText1;
    private EditText mRoommateText2;
    private EditText mRoommateText3;
    private EditText mRoommateText4;
    private EditText mRoommateText5;
    private EditText mRoommateText6;
    private List<EditText> roommates;
    private Long mRowId;
    private HomeDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new HomeDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.ledger_edit);
        setTitle(R.string.edit_ledger);

        mTitleText = (EditText) findViewById(R.id.title);
        mDescriptionText = (EditText) findViewById(R.id.description);
        roommates = new ArrayList<EditText>();
        mRoommateText1 = (EditText) findViewById(R.id.roommate1);
        mRoommateText2 = (EditText) findViewById(R.id.roommate2);
        mRoommateText3 = (EditText) findViewById(R.id.roommate3);
        mRoommateText4 = (EditText) findViewById(R.id.roommate4);
        mRoommateText5 = (EditText) findViewById(R.id.roommate5);
        mRoommateText6 = (EditText) findViewById(R.id.roommate6);
        roommates.add(mRoommateText1);
        roommates.add(mRoommateText2);
        roommates.add(mRoommateText3);
        roommates.add(mRoommateText4);
        roommates.add(mRoommateText5);
        roommates.add(mRoommateText6);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(HomeDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(HomeDbAdapter.KEY_ROWID)
									: null;
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
        if (mRowId != null) {
            Cursor ledger = mDbHelper.fetchLedger(mRowId);
            startManagingCursor(ledger);
            Cursor members = mDbHelper.fetchAllMembers(mRowId);
            startManagingCursor(members);
            mTitleText.setText(ledger.getString(
                    ledger.getColumnIndexOrThrow(HomeDbAdapter.KEY_TITLE)));
            mDescriptionText.setText(ledger.getString(
                    ledger.getColumnIndexOrThrow(HomeDbAdapter.KEY_DESCRIPTION)));
            int index = 0;
            members.moveToPosition(-1);
            while (members.moveToNext()) {
                roommates.get(index).setText(members.getString(members.getColumnIndexOrThrow(HomeDbAdapter.KEY_MEMBER)));
                index++;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(HomeDbAdapter.KEY_ROWID, mRowId);
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
        List<String> members = new ArrayList<String>();
        for (EditText roommate : roommates) {
            String member = roommate.getText().toString();
            if (!member.equals("")) {
                members.add(roommate.getText().toString());
            }
        }

        if (mRowId == null) {
            long id = mDbHelper.createLedger(title, description, members);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateLedger(mRowId, title, description, members);
        }
    }

}
