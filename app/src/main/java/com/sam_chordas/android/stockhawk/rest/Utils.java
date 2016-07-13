package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.HistoricalColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");

                     batchOperations.add(buildQuoteBatchOperation(jsonObject));

                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                                batchOperations.add(buildQuoteBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        try {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        } catch (Exception e) {
        }
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {

        try {
            String weight = change.substring(0, 1);
            String ampersand = "";
            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format("%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
        } catch (Exception e) {
        }
        return change;
    }

    public static ContentProviderOperation buildQuoteBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
                String change = jsonObject.getString("Change");
                builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        jsonObject.getString("ChangeinPercent"), true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }


    public static ArrayList historicalJsonToContentVals(String JSON){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject historicalJsonObject = null;
        JSONArray historicalResultsArray = null;

        try {
            historicalJsonObject = new JSONObject(JSON);
            if (historicalJsonObject != null && historicalJsonObject.length() != 0) {
                historicalJsonObject = historicalJsonObject.getJSONObject("query");
                int count = Integer.parseInt(historicalJsonObject.getString("count"));
                if (count == 1) {
                    historicalJsonObject = historicalJsonObject.getJSONObject("results")
                            .getJSONObject("quote");

                    batchOperations.add(buildGraphBatchOperation(historicalJsonObject));

                } else {
                    historicalResultsArray = historicalJsonObject.getJSONObject("results").getJSONArray("quote");

                    if (historicalResultsArray != null && historicalResultsArray.length() != 0) {
                        for (int i = 0; i < historicalResultsArray.length(); i++) {
                            historicalJsonObject = historicalResultsArray.getJSONObject(i);
                            batchOperations.add(buildGraphBatchOperation(historicalJsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }



    public static ContentProviderOperation buildGraphBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                HistoricalProvider.HistoricalData.CONTENT_URI);
        try {
                builder.withValue(HistoricalColumns.SYMBOL, jsonObject.getString("Symbol"));
                builder.withValue(HistoricalColumns.DATE, jsonObject.getString("Date"));
                builder.withValue(HistoricalColumns.CLOSE, jsonObject.getString("Close"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    public static float getMaxRoundedAxisValue(float maxValue){
        if (maxValue > 10) {
            return (Math.round(((maxValue + 5) / 10d)) * 10);
        } else {
            return maxValue + 1;
        }
    }

    public static float getMinRoundedAxisValue(float minValue, float maxValue) {
        if (maxValue > 10) {
            return (Math.round(((minValue - 5) / 10d)) * 10);
        } else if (minValue > 1) {
            return minValue - 1;
        } else {
            return 0f;
        }
        }

    public static int getAxisInterval(int minValue, int maxValue) {
        if (maxValue > 10 && ((maxValue-minValue) != 0)) {
                return  ((maxValue - minValue) / 10);
        } else {
            return 1;
        }
    }

    public static String getYearFromString(String date) {
        StringTokenizer strToke = new StringTokenizer(date, "-");
        return strToke.nextToken();
    }

    public static String getMonthFromString(String date) {
        StringTokenizer strToke = new StringTokenizer(date, "-");
        strToke.nextToken();
        return strToke.nextToken();
    }

    public static String getDayFromString(String date) {
        StringTokenizer strToke = new StringTokenizer(date, "-");
        strToke.nextToken();
        strToke.nextToken();
        return strToke.nextToken();
    }

}
