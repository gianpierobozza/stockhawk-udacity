package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utilities.DecimalFormatUtils;

public class ListWidgetService extends RemoteViewsService {

    public static String EXTRA_LIST_WIDGET_SYMBOL = "LIST_WIDGET_SYMBOL";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;

    ListRemoteViewsFactory(Context context) {
        mContext = context;
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), nothing to do here.
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
        String symbol = "";
        String price = "";
        float absChange = 0;
        if (mCursor.moveToPosition(position)) {
            symbol = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            price = DecimalFormatUtils.getDollarFormat(
                    mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE)));
            absChange = Float.parseFloat(mCursor.getString(
                    mCursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)));
        }

        final int itemId = R.layout.widget_list_item;
        RemoteViews view = new RemoteViews(mContext.getPackageName(), itemId);
        view.setTextViewText(R.id.widget_list_symbol, symbol);
        view.setTextViewText(R.id.widget_list_price, price);
        view.setInt(
                R.id.widget_list_abs_change,
                "setBackgroundResource",
                R.drawable.percent_change_pill_red
        );
        if (absChange > 0) {
            view.setInt(
                    R.id.widget_list_abs_change,
                    "setBackgroundResource",
                    R.drawable.percent_change_pill_green
            );
        }
        view.setTextViewText(
                R.id.widget_list_abs_change,
                DecimalFormatUtils.getDollarFormatWithPlus(absChange)
        );

        Intent fillInIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putString(ListWidgetService.EXTRA_LIST_WIDGET_SYMBOL, symbol);
        fillInIntent.putExtras(extras);
        view.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return view;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(
                Contract.Quote.URI,
                null,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL
        );
    }

}