package com.gbozza.android.stockhawk.data;

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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Parcelable implementation of the Stock model, for serialization purposes
 */
public class StockParcelable implements Parcelable {

    private int id;
    private String symbol;
    private String price;
    private String absolute_change;
    private String percentage_change;
    private String history;

    /**
     * Base Constructor for the Class
     *
     * @param id the integer id of a stock
     * @param symbol the symbol associated with the stock
     * @param price the current price
     * @param absolute_change the absolute change
     * @param percentage_change the percentage change
     * @param history the csv string containing the history of the quote
     */
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

    /**
     * Constructor used by the save instance mechanism that handles a Parcel to achieve it
     *
     * @param parcel the object containing the stock data of the object we need to create
     */
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

    /*
     * Following getter and setter methods for the class properties
     */

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
