package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.formatter.XAxisDateValueFormatter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.stock_chart) LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

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
            try {
                CSVReader reader = new CSVReader(new StringReader(cursor.getString(5)));
                List<String[]> history = reader.readAll();
                Collections.reverse(history);

                String[] xValues = new String[history.size()];

                List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < history.size(); i++) {
                    Timber.d("First value: %s", history.get(i)[0]);
                    Timber.d("Second value: %s", history.get(i)[1]);
                    entries.add(new Entry(i, Float.parseFloat(history.get(i)[1])));

                    xValues[i] = history.get(i)[0];
                }

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new XAxisDateValueFormatter(xValues, getApplicationContext()));

                LineDataSet dataSet = new LineDataSet(entries, "Label");
                dataSet.setColor(Color.RED);
                dataSet.setCircleColor(Color.BLACK);
                dataSet.setValueTextColor(Color.BLACK);

                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.setBackgroundColor(Color.WHITE);
                lineChart.invalidate();

            } catch (IOException exception) {
                Timber.e(exception, "Error parsing stock history");
            }
        }


    }
}
