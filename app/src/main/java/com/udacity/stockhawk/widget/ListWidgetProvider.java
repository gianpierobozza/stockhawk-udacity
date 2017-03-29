package com.udacity.stockhawk.widget;

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

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

public class ListWidgetProvider extends AppWidgetProvider {

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static StockProviderObserver sDataObserver;

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
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

class StockProviderObserver extends ContentObserver {

    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;

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