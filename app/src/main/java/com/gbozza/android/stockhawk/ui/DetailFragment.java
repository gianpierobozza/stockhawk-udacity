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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbozza.android.stockhawk.R;
import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.utilities.DecimalFormatUtils;
import com.gbozza.android.stockhawk.utilities.XAxisDateValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * The Detail screen Fragment, used by the Detail Activity
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.stock_chart) LineChart mLineChart;
    @BindView(R.id.detail_symbol) TextView mDetailSymbol;
    @BindView(R.id.detail_price)TextView mDetailPrice;
    @BindView(R.id.detail_abs_change) TextView mDetailAbsChange;
    @BindView(R.id.detail_perc_change) TextView mDetailPercChange;
    @BindView(R.id.detail_data) LinearLayout mDetailData;
    @BindView(R.id.detail_error) TextView mDetailError;
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
            mDetailSymbol.setText(mSymbol);
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

                mDetailError.setVisibility(View.GONE);
                mDetailSymbol.setVisibility(View.VISIBLE);
                mDetailData.setVisibility(View.VISIBLE);
            } catch (IOException exception) {
                Timber.e(exception, "Error parsing stock history");
            }
        } else {
            mDetailError.setText(getString(R.string.error_no_detail));
            mDetailError.setVisibility(View.VISIBLE);
            mDetailSymbol.setVisibility(View.INVISIBLE);
            mDetailData.setVisibility(View.INVISIBLE);
            mLineChart.clear();
            mLineChart.setBackgroundColor(Color.DKGRAY);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
