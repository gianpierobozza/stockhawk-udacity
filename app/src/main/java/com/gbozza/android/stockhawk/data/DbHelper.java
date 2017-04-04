package com.gbozza.android.stockhawk.data;

/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gbozza.android.stockhawk.data.Contract.Quote;

/**
 * Helper class for the SQLite extension
 */
class DbHelper extends SQLiteOpenHelper {

    private static final String NAME = "StockHawk.db";
    private static final int VERSION = 1;

    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " ("
                + Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Quote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Quote.COLUMN_PRICE + " REAL NOT NULL, "
                + Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_HISTORY + " TEXT NOT NULL, "
                + "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";
        db.execSQL(builder);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);
        onCreate(db);
    }

}
