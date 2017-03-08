package com.udacity.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

public class StockParcelable implements Parcelable {

    private int id;
    private String symbol;
    private String price;
    private String absolute_change;
    private String percentage_change;
    private String history;

    public StockParcelable(int id,
                           String symbol,
                           String price,
                           String absolute_change,
                           String percentage_change,
                           String history) {
        this.id = id;
        this.symbol = symbol;
        this.price = price;
        this.absolute_change = absolute_change;
        this.percentage_change = percentage_change;
        this.history = history;
    }

    private StockParcelable (Parcel parcel) {
        id = parcel.readInt();
        symbol = parcel.readString();
        price = parcel.readString();
        absolute_change = parcel.readString();
        percentage_change = parcel.readString();
        history = parcel.readString();
    }

    public static final Parcelable.Creator<StockParcelable> CREATOR = new Parcelable.Creator<StockParcelable>() {
        @Override
        public StockParcelable createFromParcel(Parcel parcel) {
            return new StockParcelable(parcel);
        }

        @Override
        public StockParcelable[] newArray(int i) {
            return new StockParcelable[i];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(symbol);
        parcel.writeString(price);
        parcel.writeString(absolute_change);
        parcel.writeString(percentage_change);
        parcel.writeString(history);
    }

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getAbsolute_change() {
        return absolute_change;
    }

    public String getPercentage_change() {
        return percentage_change;
    }

    public String getHistory() {
        return history;
    }
}
