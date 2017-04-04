package com.gbozza.android.stockhawk.widget;

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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.RemoteViews;

import com.gbozza.android.stockhawk.R;
import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.sync.QuoteSyncJob;
import com.gbozza.android.stockhawk.ui.MainActivity;

/**
 * Based on the official Google documentation for ListView Widgets.
 * This is the provider for our list.
 */
public class ListWidgetProvider extends AppWidgetProvider {

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static StockProviderObserver sDataObserver;

    /**
     * Base Constructor
     */
    public ListWidgetProvider() {
        sWorkerThread = new HandlerThread("ListWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews layout = buildLayout(context, appWidgetIds[i]);

            Intent startActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, 0);
            layout.setPendingIntentTemplate(R.id.list_widget_list_view, startActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, ListWidgetProvider.class);
            sDataObserver = new StockProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(Contract.Quote.URI, true, sDataObserver);
        }
    }

    /**
     * Method that sets the single item view as remote adapter
     *
     * @param context
     * @param appWidgetId the integer of the current widget, there can be multiple ones
     * @return the RemoteView object
     */
    private RemoteViews buildLayout(Context context, int appWidgetId) {
        RemoteViews view;
        final Intent intent = new Intent(context, ListWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        view = new RemoteViews(context.getPackageName(), R.layout.widget_list);
        view.setRemoteAdapter(appWidgetId, R.id.list_widget_list_view, intent);

        view.setEmptyView(R.id.list_widget_list_view, R.id.list_widget_empty_view);

        return view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction()) && null != sDataObserver) {
            final Context localContext = context;
            sWorkerQueue.removeMessages(0);
            sWorkerQueue.post(new Runnable() {
                @Override
                public void run() {
                    final AppWidgetManager mgr = AppWidgetManager.getInstance(localContext);
                    final ComponentName cn = new ComponentName(localContext, ListWidgetProvider.class);
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.list_widget_list_view);
                }
            });
        }
        super.onReceive(context, intent);
    }

}

/**
 * Content Observer class extension, notifies widgets for changes in the data
 */
class StockProviderObserver extends ContentObserver {

    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;

    /**
     * Base Constructor
     *
     * @param mgr the widget manager instance
     * @param cn the component name
     * @param h the handler instance
     */
    StockProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }

    @Override
    public void onChange(boolean selfChange) {
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.list_widget_list_view);
    }

}