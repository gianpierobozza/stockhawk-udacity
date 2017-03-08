package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra(MainActivity.EXTRA_SYMBOL);
        setTitle(symbol);

        String[] selectionArgs = {symbol};
        Uri uri = Contract.Quote.makeUriForStock(symbol);
        Cursor cursor = getContentResolver().query(uri,
                null,
                Contract.Quote.COLUMN_SYMBOL + "=?",
                selectionArgs,
                null);

        while (cursor.moveToNext()) {
            Timber.i("History: %s", cursor.getString(5));
        }
    }
}
