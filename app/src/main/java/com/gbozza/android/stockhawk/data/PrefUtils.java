package com.gbozza.android.stockhawk.data;

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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gbozza.android.stockhawk.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for the Shared Preferences management
 */
public final class PrefUtils {

    private PrefUtils() {
    }

    /**
     * Method to get all the stocks saved in the shared prefs
     *
     * @param context
     * @return a HashSet of Strings with the symbols of stocks
     */
    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());
    }

    /**
     * Method to get a stock from a specific position
     *
     * @param context
     * @param pos the position we want to get from the prefs
     * @return a String with the symbol
     */
    public static String getSymbolAtPos(Context context, int pos) {
        Set<String> stocks = PrefUtils.getStocks(context);
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : stocks)
            arrayList.add(str);
        Collections.sort(arrayList);
        if (arrayList.size() > pos) {
            return arrayList.get(pos);
        }
        return "";
    }

    /**
     * Method for adding or removing a stock symbol from the prefs
     *
     * @param context
     * @param symbol the String we want to add/remove
     * @param add the Boolean flag controlling the add/remove operation
     */
    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    /**
     * Shorthand method to add a symbol
     *
     * @param context
     * @param symbol the String we want to add to the prefs
     */
    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    /**
     * Shorthand method to remove a symbol
     *
     * @param context
     * @param symbol the String we want to remove from the prefs
     */
    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    /**
     * Get the selected method of display for the change of the stocks in the main list
     *
     * @param context
     * @return a String containing the absolute or percentage change
     */
    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    /**
     * Swap between the two methods of display for the change
     *
     * @param context
     */
    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayMode = getDisplayMode(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

}
