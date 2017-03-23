package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utilities.DecimalFormatUtils;
import com.udacity.stockhawk.utilities.XAxisDateValueFormatter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.stock_chart) LineChart mLineChart;
    @BindView(R.id.detail_symbol) TextView mDetailSymbol;
    @BindView(R.id.detail_price)TextView mDetailPrice;
    @BindView(R.id.detail_abs_change) TextView mDetailAbsChange;
    @BindView(R.id.detail_perc_change) TextView mDetailPercChange;
    private Uri mUri;
    private String mSymbol;

    public static final String[] DETAIL_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    public static final int POSITION_ID = 0;
    public static final int POSITION_SYMBOL = 1;
    public static final int POSITION_PRICE = 2;
    public static final int POSITION_ABSOLUTE_CHANGE = 3;
    public static final int POSITION_PERCENTAGE_CHANGE = 4;
    public static final int POSITION_HISTORY = 5;

    private static final int DETAIL_LOADER = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        if (null != arguments) {
            mSymbol = arguments.getString(MainActivity.EXTRA_SYMBOL);
            mUri = Contract.Quote.makeUriForStock(mSymbol);
            mDetailSymbol.setText(mSymbol);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            String[] selectionArgs = {mSymbol};
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    null,
                    DETAIL_COLUMNS[POSITION_SYMBOL] + "=?",
                    selectionArgs,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mDetailPrice.setText(DecimalFormatUtils.getDollarFormat(data.getString(POSITION_PRICE)));

            float rawAbsoluteChange = Float.parseFloat(data.getString(POSITION_ABSOLUTE_CHANGE));
            float percentageChange = Float.parseFloat(data.getString(POSITION_PERCENTAGE_CHANGE));

            mDetailAbsChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            if (rawAbsoluteChange > 0) {
                mDetailAbsChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            }

            mDetailPercChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            if (percentageChange > 0) {
                mDetailPercChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            }

            mDetailAbsChange.setText(DecimalFormatUtils.getDollarFormatWithPlus(rawAbsoluteChange));
            mDetailPercChange.setText(DecimalFormatUtils.getPercentage(percentageChange));

            try {
                CSVReader reader = new CSVReader(new StringReader(data.getString(POSITION_HISTORY)));
                List<String[]> history = reader.readAll();
                Collections.reverse(history);

                String[] xValues = new String[history.size()];

                List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < history.size(); i++) {
                    entries.add(new Entry(i, Float.parseFloat(history.get(i)[1])));
                    xValues[i] = history.get(i)[0];
                }

                XAxis xAxis = mLineChart.getXAxis();
                xAxis.setValueFormatter(new XAxisDateValueFormatter(xValues, getActivity().getApplicationContext()));

                LineDataSet dataSet = new LineDataSet(entries, mSymbol);
                dataSet.setColor(Color.RED);
                dataSet.setCircleColor(Color.BLACK);
                dataSet.setValueTextColor(Color.BLACK);

                LineData lineData = new LineData(dataSet);
                Description chartDesc = new Description();
                chartDesc.setText(getString(R.string.chart_description));
                mLineChart.setDescription(chartDesc);
                mLineChart.setData(lineData);
                mLineChart.setBackgroundColor(Color.WHITE);
                mLineChart.invalidate();
            } catch (IOException exception) {
                Timber.e(exception, "Error parsing stock history");
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
