package com.gbozza.android.stockhawk.utilities;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class DecimalFormatUtils {

    private static DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    private DecimalFormatUtils() { }

    public static String getDollarFormat(String value) {
        return dollarFormat.format(Float.parseFloat(value));
    }

    public static String getDollarFormatWithPlus(float value) {
        dollarFormatWithPlus.setPositivePrefix("+$");
        return dollarFormatWithPlus.format(value);
    }

    public static String getPercentage(float value) {
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        return percentageFormat.format(value / 100);
    }

}
