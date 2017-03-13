package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "stockSymbol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
