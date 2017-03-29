package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.udacity.stockhawk.sync.QuoteSyncJob;

public class QuoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, QuoteWidgetIntentService.class));
        }
    }

}
