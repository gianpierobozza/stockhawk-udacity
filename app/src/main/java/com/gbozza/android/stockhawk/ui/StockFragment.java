package com.gbozza.android.stockhawk.ui;

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
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gbozza.android.stockhawk.R;
import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.data.PrefUtils;
import com.gbozza.android.stockhawk.data.StockParcelable;
import com.gbozza.android.stockhawk.sync.QuoteSyncJob;
import com.gbozza.android.stockhawk.widget.ListWidgetService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * The Fragment containing the main list of quotes, it implements a callback Loader and a
 * Swipe Refresh Listener
 */
public class StockFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view) RecyclerView mStockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error) TextView mError;
    public StockAdapter mAdapter;
    private Context mContext;
    private boolean mTwoPane;

    private static final String BUNDLE_STOCK_KEY = "stockList";

    private static final int STOCK_LOADER = 0;

    /**
     * Public interface to be implemented, used to manage the click on the single items
     */
    public interface Callback {
        void onItemSelected(String symbol, StockAdapter.StockViewHolder vh);
    }

    /**
     * Empty Base Constructor, for reflection
     */
    public StockFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stocks, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();

        mAdapter = new StockAdapter(mContext, new StockAdapter.StockAdapterOnClickHandler() {
            @Override
            public void onClick(String symbol, StockAdapter.StockViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(symbol, vh);
            }
        });
        mStockRecyclerView.setAdapter(mAdapter);
        mStockRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        if (null != savedInstanceState) {
            manageError();
            if (savedInstanceState.containsKey(BUNDLE_STOCK_KEY)) {
                ArrayList<StockParcelable> stockList = savedInstanceState.getParcelableArrayList(BUNDLE_STOCK_KEY);
                MatrixCursor matrixCursor = new MatrixCursor(DetailFragment.DETAIL_COLUMNS);
                getActivity().startManagingCursor(matrixCursor);
                for (StockParcelable stock: stockList) {
                    matrixCursor.addRow(new Object[] {
                            stock.getId(),
                            stock.getSymbol(),
                            stock.getPrice(),
                            stock.getAbsolute_change(),
                            stock.getPercentage_change(),
                            stock.getHistory()
                    });
                }
                mAdapter.setCursor(matrixCursor);
            }
        } else {
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mSwipeRefreshLayout.setRefreshing(true);
            Intent inboundIntent = getActivity().getIntent();
            if (null != inboundIntent &&
                    !inboundIntent.hasExtra(ListWidgetService.EXTRA_LIST_WIDGET_SYMBOL)) {
                QuoteSyncJob.initialize(mContext, QuoteSyncJob.PERIODIC_ID);
                onRefresh();
            }
            getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
        }

        FloatingActionButton addStockFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        addStockFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AddStockDialog().show(getActivity().getSupportFragmentManager(), "StockDialogFragment");
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = mAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(mContext, symbol);
                mContext.getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(mStockRecyclerView);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Cursor stockCursor = mAdapter.getCursor();

        if (null != stockCursor && stockCursor.getCount() != 0) {
            stockCursor.moveToFirst();
            List<StockParcelable> stockList = new ArrayList<>();
            try {
                do {
                    stockList.add(createStockFromCursor(stockCursor));
                } while (stockCursor.moveToNext());
            } catch (Exception e) {
                Timber.e("Cursor Exception");
            }
            outState.putParcelableArrayList(BUNDLE_STOCK_KEY, new ArrayList<>(stockList));
        }
    }

    /**
     * Utility method to check if the internet connection is available
     *
     * @return true or false
     */
    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            mError.setVisibility(View.GONE);
        }
        mAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.setCursor(null);
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(mContext);
        manageError();
    }

    /**
     * Method to show / hide the error message in the main list
     */
    private void manageError() {
        if (!networkUp() && mAdapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mError.setText(getString(R.string.error_no_network));
            mError.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(mContext).size() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mError.setText(getString(R.string.error_no_stocks));
            mError.setVisibility(View.VISIBLE);
        } else {
            mError.setVisibility(View.GONE);
        }
    }

    /**
     * Method used by the onSaveInstanceState mechanism
     *
     * @param cursor the Cursor object containing our data
     * @return a Parcelable instance of the Stock
     */
    private StockParcelable createStockFromCursor(Cursor cursor) {
        return new StockParcelable(
                cursor.getInt(DetailFragment.POSITION_ID),
                cursor.getString(DetailFragment.POSITION_SYMBOL),
                cursor.getString(DetailFragment.POSITION_PRICE),
                cursor.getString(DetailFragment.POSITION_ABSOLUTE_CHANGE),
                cursor.getString(DetailFragment.POSITION_PERCENTAGE_CHANGE),
                cursor.getString(DetailFragment.POSITION_HISTORY)
        );
    }

    /**
     * Asynchronously add a new element in the main list if it exists
     *
     * @param symbol
     */
    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                new StockFragment.SymbolAddTask().execute(symbol);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                PrefUtils.addStock(mContext, symbol);
                QuoteSyncJob.syncImmediately(mContext);
            }
        }
    }

    /**
     * Set the flag for the Master/Detail flow or single Activity
     *
     * @param twoPane the boolean flag
     */
    public void setTwoPane(boolean twoPane) {
        mTwoPane = twoPane;
    }

    /**
     * Background worker that queries the YahooFinance API service to add a new symbol.
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    private class SymbolAddTask extends AsyncTask<String, Void, Stock> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Stock doInBackground(String... params) {
            try {
                return YahooFinance.get(params[0]);
            }  catch (IOException exception) {
                Timber.e(exception, "Error fetching stock quotes");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Stock stock) {
            super.onPostExecute(stock);

            if (null != stock.getName()) {
                PrefUtils.addStock(mContext, stock.getSymbol());
                QuoteSyncJob.syncImmediately(mContext);
            } else {
                Toast.makeText(mContext, "Symbol not found", Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

}