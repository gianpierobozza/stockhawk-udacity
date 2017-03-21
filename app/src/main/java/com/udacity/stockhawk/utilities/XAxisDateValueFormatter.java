package com.udacity.stockhawk.utilities;

import android.content.Context;
import android.os.Build;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisDateValueFormatter implements IAxisValueFormatter {

    private Context mContext;
    private String[] mValues;

    public XAxisDateValueFormatter(String[] values, Context context) {
        this.mContext = context;
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = mContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = mContext.getResources().getConfiguration().locale;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy", locale);
        return formatter.format(new Date(Long.parseLong(mValues[(int) value])));
    }
}