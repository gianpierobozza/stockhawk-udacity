package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "stockSymbol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /*if (null != savedInstanceState) {
            Timber.d("not null");
            ArrayList<StockParcelable> stockList = savedInstanceState.getParcelableArrayList(BUNDLE_STOCK_KEY);
            String[] columns = new String[] { "_id", "symbol", "price", "absolute_change", "percentage_change", "history" };
            MatrixCursor matrixCursor = new MatrixCursor(columns);
            startManagingCursor(matrixCursor);
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
            adapter.setCursor(matrixCursor);
        } else {
            Timber.d("null");
            QuoteSyncJob.initialize(this);
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
            getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
        }*/




    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Cursor stockCursor = adapter.getCursor();

        if (null != stockCursor) {
            stockCursor.moveToFirst();
            List<StockParcelable> stockList = new ArrayList<>();
            try {
                do {
                    stockList.add(createStockFromCursor(stockCursor));
                } while (stockCursor.moveToNext());
            } finally {
                stockCursor.close();
            }
            outState.putParcelableArrayList(BUNDLE_STOCK_KEY, new ArrayList<>(stockList));
        }
    }*/

}
