package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.utilities.DecimalFormatUtils;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class QuoteWidgetIntentService extends IntentService {

    private static final String[] STOCK_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
    };

    private static final int POSITION_SYMBOL = 1;
    private static final int POSITION_PRICE = 2;
    private static final int POSITION_ABSOLUTE_CHANGE = 3;

    public QuoteWidgetIntentService() {
        super("QuoteWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                QuoteWidgetProvider.class));

        int incomingWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                INVALID_APPWIDGET_ID);

        if (incomingWidgetId != INVALID_APPWIDGET_ID) {
            if (intent.hasExtra(WidgetConfigActivity.EXTRA_QUOTE_WIDGET_SYMBOL)) {
                String symbol = intent.getStringExtra(
                        WidgetConfigActivity.EXTRA_QUOTE_WIDGET_SYMBOL
                );
                String[] selectionArgs = {String.valueOf(symbol)};
                Uri getQuoteUri = Contract.Quote.makeUriForStock(symbol);

                Cursor data = getContentResolver().query(
                        getQuoteUri,
                        null,
                        STOCK_COLUMNS[POSITION_SYMBOL] + "=?",
                        selectionArgs,
                        null);
                if (data == null) {
                    return;
                }
                if (!data.moveToFirst()) {
                    data.close();
                    return;
                }

                String price = DecimalFormatUtils.getDollarFormat(data.getString(POSITION_PRICE));
                float absChange = Float.parseFloat(data.getString(POSITION_ABSOLUTE_CHANGE));
                data.close();

                for (int appWidgetId : appWidgetIds) {
                    if (appWidgetId == incomingWidgetId) {
                        RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget_quote);

                        view.setTextViewText(R.id.widget_symbol, symbol);
                        view.setTextViewText(R.id.widget_price, price);

                        view.setInt(
                                R.id.widget_abs_change,
                                "setBackgroundResource",
                                R.drawable.percent_change_pill_red
                        );
                        if (absChange > 0) {
                            view.setInt(
                                    R.id.widget_abs_change,
                                    "setBackgroundResource",
                                    R.drawable.percent_change_pill_green
                            );
                        }
                        view.setTextViewText(
                                R.id.widget_abs_change,
                                DecimalFormatUtils.getDollarFormatWithPlus(absChange)
                        );

                        Intent launchIntent = new Intent(this, MainActivity.class);
                        launchIntent.putExtra(WidgetConfigActivity.EXTRA_QUOTE_WIDGET_SYMBOL, symbol);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);
                        view.setOnClickPendingIntent(R.id.widget, pendingIntent);

                        appWidgetManager.updateAppWidget(appWidgetId, view);
                    }
                }
            }
        }
    }
}
