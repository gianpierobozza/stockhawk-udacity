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

        String symbol = "GOOG";
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
            RemoteViews views = new RemoteViews(
                    getPackageName(),
                    R.layout.widget_quote
            );

            views.setTextViewText(R.id.widget_symbol, symbol);
            views.setTextViewText(R.id.widget_price, price);

            views.setInt(R.id.widget_abs_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            if (absChange > 0) {
                views.setInt(R.id.widget_abs_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            }
            views.setTextViewText(R.id.widget_abs_change, DecimalFormatUtils.getDollarFormatWithPlus(absChange));

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
