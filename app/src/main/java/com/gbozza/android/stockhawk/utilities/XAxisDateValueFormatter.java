package com.gbozza.android.stockhawk.utilities;

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
import android.os.Build;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility Class used by the Graph library to format the Y Axis values, month/year
 */
public class XAxisDateValueFormatter implements IAxisValueFormatter {

    private Context mContext;
    private String[] mValues;

    /**
     * Base Constructor
     *
     * @param values list of values to format
     * @param context
     */
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