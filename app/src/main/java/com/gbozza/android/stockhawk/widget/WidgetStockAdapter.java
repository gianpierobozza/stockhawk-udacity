package com.gbozza.stockhawk.widget;

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

class WidgetStockAdapter extends RecyclerView.Adapter<WidgetStockAdapter.WidgetStockViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final WidgetStockAdapterOnClickHandler clickHandler;

    WidgetStockAdapter(Context context, WidgetStockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public WidgetStockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        return new WidgetStockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(WidgetStockViewHolder holder, int position) {
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

    interface WidgetStockAdapterOnClickHandler {
        void onClick(String symbol, WidgetStockAdapter.WidgetStockViewHolder vh);
    }

    class WidgetStockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.list_item) LinearLayout mListItem;
        @BindView(R.id.symbol) TextView symbol;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.change) TextView change;

        WidgetStockViewHolder(View itemView) {
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
