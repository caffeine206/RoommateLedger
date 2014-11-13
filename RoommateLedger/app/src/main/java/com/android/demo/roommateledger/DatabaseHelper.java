package com.android.demo.roommateledger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Derek on 11/13/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper sInstance;

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "DatabaseHelper";

    private static final String LEDGERS_TABLE_CREATE =
            "create table ledgers (_id integer primary key autoincrement, "
                    + "title text not null, description text not null);";

    private static final String MEMBERS_TABLE_CREATE =
            "create table members (_id integer primary key autoincrement, "
                    + "ledger_id integer not null, member text not null, "
                    + "FOREIGN KEY (ledger_id) REFERENCES ledgers(_id));";

    private static final String PURCHASES_TABLE_CREATE =
            "create table purchases (_id integer primary key autoincrement, ledger_id integer not null, "
                    + "title text not null, description text not null, amount decimal (19,4) not null, " +
                    "FOREIGN KEY (ledger_id) REFERENCES ledgers(_id));";

    public static DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LEDGERS_TABLE_CREATE);
        db.execSQL(MEMBERS_TABLE_CREATE);
        db.execSQL(PURCHASES_TABLE_CREATE);
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
