package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by Zac on 6/21/16.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StockWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] STOCK_COLUMNS = {
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns._ID
    };

    // these indices must match the projection
    static final int INDEX_SYMBOL = 0;
    static final int INDEX_BIDPRICE = 1;
    static final int INDEX_CHANGE = 2;
    static final int INDEX_PERCENT_CHANGE = 3;
    static final int INDEX_ID = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();


//                data = getContentResolver().query(weatherForLocationUri,
//                        FORECAST_COLUMNS,
//                        null,
//                        null,
//                        WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        STOCK_COLUMNS,
                        null,
                        null,
                        null
                        );


                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);


                String symbol = data.getString(INDEX_SYMBOL);
                String bidPrice = data.getString(INDEX_BIDPRICE);
                String change = data.getString(INDEX_CHANGE);
                String percentChange = data.getString(INDEX_PERCENT_CHANGE);
                String id = data.getString(INDEX_ID);
                //Set the a11y to pronounce every letter to be more comprehensible,
                char[] chars = new char[symbol.length()];
                symbol.getChars(0, symbol.length(), chars, 0);

                String stringChars = "";

                for (int i=0; i < symbol.length(); i++){
                    stringChars += chars[i] + " ";
                }

                String description = getString(R.string.a11y_quotes, stringChars, bidPrice, change, percentChange);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }

                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_bid_price, bidPrice);
                views.setTextViewText(R.id.widget_change, change);

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra("id", id);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            //TODO will need this if using a11y
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_symbol, description);
                views.setContentDescription(R.id.widget_bid_price, ".");
                views.setContentDescription(R.id.widget_change, ".");
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getInt(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

