package com.gbozza.android.stockhawk.sync;

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

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

/**
 * Main logic class for the Synchronization of quotes
 */
public final class QuoteSyncJob {

    public static final int PERIODIC_ID = 1;
    private static final int ONE_OFF_ID = 2;
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int YEARS_OF_HISTORY = 2;

    public static final String ACTION_DATA_UPDATED = "com.gbozza.android.stockhawk.ACTION_DATA_UPDATED";

    private QuoteSyncJob() { }

    /**
     * Method to query the YahooFinance API and get the details for the quotes stored in the app
     * An Intent is invoked and broadcast if there are no errors in the procedure.
     * @param context
     */
    static void getQuotes(Context context) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();
            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);

                if (null != stock) {
                    StockQuote quote = stock.getQuote();

                    if (null != quote.getPrice()) {
                        float price = quote.getPrice().floatValue();
                        float change = quote.getChange().floatValue();
                        float percentChange = quote.getChangeInPercent().floatValue();

                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                        StringBuilder historyBuilder = new StringBuilder();

                        for (HistoricalQuote it : history) {
                            historyBuilder.append(it.getDate().getTimeInMillis());
                            historyBuilder.append(", ");
                            historyBuilder.append(it.getClose());
                            historyBuilder.append("\n");
                        }

                        ContentValues quoteCV = new ContentValues();
                        quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                        quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                        quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                        quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                        quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                        quoteCVs.add(quoteCV);
                    }
                }
            }

            if (quoteCVs.size() != 0) {
                context.getContentResolver()
                        .bulkInsert(
                                Contract.Quote.URI,
                                quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                context.sendBroadcast(dataUpdatedIntent);
            }
        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    /**
     * Schedule a Job to update the data
     *
     * @param context
     * @param id univocal integer for the job builder object
     */
    private static void schedulePeriodic(Context context, int id) {
        JobInfo.Builder builder = new JobInfo.Builder(id, new ComponentName(context, QuoteJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());
    }

    /**
     * Shorthand method to initialize a job schedule
     *
     * @param context
     * @param id univocal integer for the job builder object
     */
    public static synchronized void initialize(final Context context, int id) {
        schedulePeriodic(context, id);
        syncImmediately(context);
    }

    /**
     * Immediately start a synchronization if there's an active connection to the internet
     *
     * @param context
     */
    public static synchronized void syncImmediately(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {
            schedulePeriodic(context, ONE_OFF_ID);
        }
    }

}
