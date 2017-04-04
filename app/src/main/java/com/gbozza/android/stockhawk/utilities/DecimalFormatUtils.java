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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility to format prices and percentage numbers
 */
public final class DecimalFormatUtils {

    private static DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    /**
     * Empty Base constructor, for reflection
     */
    private DecimalFormatUtils() { }

    /**
     * Get the dollar format
     *
     * @param value the string representing the value
     * @return the float value formatted for dollar currency
     */
    public static String getDollarFormat(String value) {
        return dollarFormat.format(Float.parseFloat(value));
    }

    /**
     * Positive formatter for price value
     *
     * @param value the string representing the value
     * @return the formatted String
     */
    public static String getDollarFormatWithPlus(float value) {
        dollarFormatWithPlus.setPositivePrefix("+$");
        return dollarFormatWithPlus.format(value);
    }

    /**
     * Positive formatter for percentage value
     *
     * @param value the float representing the value
     * @return the formatted String
     */
    public static String getPercentage(float value) {
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        return percentageFormat.format(value / 100);
    }

}
