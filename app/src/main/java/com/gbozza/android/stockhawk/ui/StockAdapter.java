package com.gbozza.android.stockhawk.ui;

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
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbozza.android.stockhawk.R;
import com.gbozza.android.stockhawk.data.Contract;
import com.gbozza.android.stockhawk.data.PrefUtils;
import com.gbozza.android.stockhawk.utilities.DecimalFormatUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The Adapter for the RecyclerView
 */
class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    /**
     * Base Constructor
     *
     * @param context
     * @param clickHandler the instance of the ClickHandler for the single item action
     */
    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    /**
     *
     * @param cursor the Cursor we need to set
     */
    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Getter method for the current cursor
     *
     * @return the current cursor
     */
    Cursor getCursor() {
        return cursor;
    }

    /**
     * Request the symbol at the specified position
     *
     * @param position the integer of the position we need
     * @return a String contained in the requested position
     */
    String getSymbolAtPosition(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.price.setText(DecimalFormatUtils.getDollarFormat(cursor.getString(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        String change = DecimalFormatUtils.getDollarFormatWithPlus(rawAbsoluteChange);
        String percentage = DecimalFormatUtils.getPercentage(percentageChange);

        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
        } else {
            holder.change.setText(percentage);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    interface StockAdapterOnClickHandler {
        void onClick(String symbol, StockAdapter.StockViewHolder vh);
    }

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item) LinearLayout mListItem;
        @BindView(R.id.symbol) TextView symbol;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.change) TextView change;

        /**
         * Constructor for the ViewHolder
         *
         * @param itemView the view we are binding
         */
        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            clickHandler.onClick(cursor.getString(
                    cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)),
                    this
            );
        }
    }

}
