package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteIntentService;
import com.udacity.stockhawk.widget.WidgetConfigActivity;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements StockFragment.Callback {

    private boolean mTwoPane;
    private String mSymbol;
    private StockFragment mStockFragment;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    public static final String EXTRA_SYMBOL = "stockSymbol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent inboundIntent = getIntent();
        mSymbol = null;
        if (null != inboundIntent && inboundIntent.hasExtra(EXTRA_SYMBOL)) {
            mSymbol = inboundIntent.getStringExtra(EXTRA_SYMBOL);
        }
        if (null != inboundIntent && inboundIntent.hasExtra(WidgetConfigActivity.EXTRA_QUOTE_WIDGET_SYMBOL)) {
            mSymbol = inboundIntent.getStringExtra(WidgetConfigActivity.EXTRA_QUOTE_WIDGET_SYMBOL);
        }
        Timber.d("symbol is %s", mSymbol);
        if (null != findViewById(R.id.stock_detail_container)) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                if (null == mSymbol) {
                    mSymbol = PrefUtils.getSymbolAtPos(this, 0);
                }
                DetailFragment fragment = new DetailFragment();
                Bundle args = new Bundle();
                args.putString(EXTRA_SYMBOL, mSymbol);
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            if (null != mSymbol) {
                Intent intent = new Intent(this, DetailActivity.class)
                        .putExtra(EXTRA_SYMBOL, mSymbol);

                ActivityCompat.startActivity(this, intent, null);
            }
            mTwoPane = false;
        }

        mStockFragment = ((StockFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_stocks));
        mStockFragment.setTwoPane(mTwoPane);
    }

    @Override
    public void onItemSelected(String symbol, StockAdapter.StockViewHolder vh) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(EXTRA_SYMBOL, symbol);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(EXTRA_SYMBOL, symbol);

            ActivityCompat.startActivity(this, intent, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            mStockFragment.mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

}
