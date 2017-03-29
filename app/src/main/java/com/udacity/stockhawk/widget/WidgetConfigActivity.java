package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetConfigActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.quote_widget_recycler_view) RecyclerView mQuoteWidgetRecyclerView;
    private int mWidgetId;
    private WidgetStockAdapter mAdapter;

    private static final int QUOTE_WIDGET_STOCK_LOADER = 2;
    public static String EXTRA_QUOTE_WIDGET_SYMBOL = "QUOTE_WIDGET_SYMBOL";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWidgetId = INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
        }

        setContentView(R.layout.quote_widget_config);
        setTitle(R.string.quote_widget_config_activity_title);
        ButterKnife.bind(this);

        mAdapter = new WidgetStockAdapter(this, new WidgetStockAdapter.WidgetStockAdapterOnClickHandler() {
            @Override
            public void onClick(String symbol, WidgetStockAdapter.WidgetStockViewHolder vh) {
                if (mWidgetId != INVALID_APPWIDGET_ID) {
                    Intent intent = new Intent(WidgetConfigActivity.this,
                            QuoteWidgetIntentService.class);
                    intent.putExtra(EXTRA_APPWIDGET_ID, mWidgetId);
                    intent.putExtra(EXTRA_QUOTE_WIDGET_SYMBOL, symbol);
                    setResult(RESULT_OK);
                    startService(intent);

                    finish();
                } else {
                    Timber.e("Invalid AppWidgetID");
                    finish();
                }
            }
        });
        mQuoteWidgetRecyclerView.setAdapter(mAdapter);
        mQuoteWidgetRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getSupportLoaderManager().initLoader(QUOTE_WIDGET_STOCK_LOADER, null, this);

        setResult(RESULT_CANCELED);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

}
