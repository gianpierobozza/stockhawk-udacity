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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.gbozza.android.stockhawk.R;
import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.utilities.DecimalFormatUtils;

/**
 * Based on the official Google documentation for ListView Widgets.
 * This is the remote view service for our list.
 */
public class ListWidgetService extends RemoteViewsService {

    public static String EXTRA_LIST_WIDGET_SYMBOL = "LIST_WIDGET_SYMBOL";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

/**
 * Factory Class extension for the Remote View
 */
class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;

    ListRemoteViewsFactory(Context context) {
        mContext = context;
    }

    /**
     * onCreate will not be used
     */
    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), nothing to do here.
    }

    /**
     * Destroy and close the cursor
     */
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    /**
     * Getter method for the number of items in the cursor
     * @return the size of the cursor
     */
    public int getCount() {
        return mCursor.getCount();
    }

    /**
     * This Method binds and attaches all the data of our single item element
     * @param position the integer position
     * @return a RemoveView to add to the main list of the widget
     */
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

    /**
     * New query to the Content Provider when the data changes
     */
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

    /*
     * Following getter and setter methods
     */

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

}