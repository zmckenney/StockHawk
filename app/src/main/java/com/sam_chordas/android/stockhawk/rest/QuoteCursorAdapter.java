package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static final String LOG_TAG = QuoteCursorAdapter.class.getSimpleName();
  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){

    //I didnt like that it blinked the null values but I still wanted to display an easy Toast on the MyStocksActivity so I added
    //the null data but set it to invisible so it doesnt ever show.  Then in the onLoadFinished of MyStocksActivity we will
    // find the null values, delete the data, and show a toast message

    String cursorSymbol = cursor.getString(cursor.getColumnIndex("symbol"));
    String cursorBidPrice = cursor.getString(cursor.getColumnIndex("bid_price"));
    String cursorPercentChange = cursor.getString(cursor.getColumnIndex("percent_change"));
    String cursorChange = cursor.getString(cursor.getColumnIndex("change"));


    if (!cursorBidPrice.equals("null")) {
      viewHolder.change.setVisibility(View.VISIBLE);
      viewHolder.bidPrice.setVisibility(View.VISIBLE);
      viewHolder.symbol.setVisibility(View.VISIBLE);
      viewHolder.symbol.setText(cursorSymbol);
      viewHolder.bidPrice.setText(cursorBidPrice);
      int sdk = Build.VERSION.SDK_INT;
      if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
          viewHolder.change.setBackgroundDrawable(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
        } else {
          viewHolder.change.setBackground(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
        }
      } else {
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
          viewHolder.change.setBackgroundDrawable(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
        } else {
          viewHolder.change.setBackground(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
        }
      }
      if (Utils.showPercent) {
        viewHolder.change.setText(cursorPercentChange);
      } else {
        viewHolder.change.setText(cursorChange);
      }


      //Set the a11y to pronounce every letter to be more comprehensible,
      char[] chars = new char[cursorSymbol.length()];
      cursorSymbol.getChars(0, cursorSymbol.length(), chars, 0);

      String stringChars = "";

      for (int i=0; i < cursorSymbol.length(); i++){
        stringChars += chars[i] + " ";
      }

      Log.v(LOG_TAG, "String chars = " + stringChars);

      viewHolder.itemView.setContentDescription(mContext.getString(R.string.a11y_quotes, stringChars, cursorBidPrice, cursorChange, cursorPercentChange ));
    }

    //If the symbol was not found, add the null data but keep it invisible.  It will be deleted and toast shown in MyStocksActivity
    else if (cursorBidPrice.equals("null")) {
      viewHolder.symbol.setText(cursorSymbol);
      viewHolder.bidPrice.setText(cursorBidPrice);
      int sdk = Build.VERSION.SDK_INT;
      if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
          viewHolder.change.setBackgroundDrawable(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
        } else {
          viewHolder.change.setBackground(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
        }
      } else {
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
          viewHolder.change.setBackgroundDrawable(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
        } else {
          viewHolder.change.setBackground(
                  mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
        }
      }
        viewHolder.change.setText(cursorChange);

      viewHolder.change.setVisibility(View.INVISIBLE);
      viewHolder.bidPrice.setVisibility(View.INVISIBLE);
      viewHolder.symbol.setVisibility(View.INVISIBLE);
      }

    }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    mContext.getContentResolver().delete(HistoricalProvider.HistoricalData.withSymbol(symbol), null, null);
    notifyItemRemoved(position);

    Intent dataUpdatedIntent = new Intent(StockTaskService.ACTION_DATA_UPDATED)
            .setPackage(mContext.getPackageName());
    mContext.sendBroadcast(dataUpdatedIntent);
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {}
  }
}
