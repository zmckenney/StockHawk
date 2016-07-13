package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Zac on 6/2/16.
 */
public class GraphActivity extends AppCompatActivity {

    private static final String LOG_TAG = GraphActivity.class.getSimpleName();

    private final int DATA_PULL_SUCCESS = 0;
    private int dataPull = 1;

    private LineChartView chartTwelveMonths;
    private LineChartView chartSixMonths;
    private LineChartView chartOneMonth;

    private TextView graphDate;

    private String mTitle;
    public static Context mContext;

    private String id;

    //Initialize our min and max values to values that wont be seen, we can then easily find our values by
    //checking if they are below (min) or above (max) our initialized values.  The first check should always be below or above respectively.
    float minYearValue = 100000;
    float maxYearValue = -2;

    float minSixMonthValue = 100000;
    float maxSixMonthValue = -2;

    float minOneMonthValue = 100000;
    float maxOneMonthValue = -2;


    ProgressDialog dialog;
    String symbol = null;

    String strPastYearFull;
    String strPastSixMonthsFull;
    String strPastOneMonthFull;
    String strCurrentFull;
    String strYesterdayFull;

    String strCurrentYear;
    String strCurrentMonth;
    String strCurrentDay;

    int yesterdayInt;

    float lineThickness = 8f;

    LineSet dataSetTwelveMonths = new LineSet();
    LineSet dataSetOneMonth = new LineSet();
    LineSet dataSetSixMonths = new LineSet();

    private OkHttpClient client = new OkHttpClient();


    public void restoreActionBar(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, callback);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeActionContentDescription("Go back to stock list");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_menu, menu);

        // Adapter
        SpinnerAdapter adapter =
                ArrayAdapter.createFromResource(this, R.array.graph_spinner_options,
                        android.R.layout.simple_spinner_dropdown_item);


// Callback
        ActionBar.OnNavigationListener callback = new ActionBar.OnNavigationListener() {

            String[] items = getResources().getStringArray(R.array.graph_spinner_options); // List items from res

            @Override
            public boolean onNavigationItemSelected(int position, long id) {

                // Do stuff when navigation item is selected
                Log.v("NavigationItemSelected", items[position] + "<= Items position but this is the position" + position); // Debug

                switch (items[position]) {
                    case "1 Month":
                        chartTwelveMonths.setVisibility(View.INVISIBLE);
                        chartSixMonths.setVisibility(View.INVISIBLE);
                        chartOneMonth.setVisibility(View.VISIBLE);

                        if (dataPull == DATA_PULL_SUCCESS) {
                            if (getResources().getBoolean(R.bool.is_right_to_left)) {
                                graphDate.setText( getString(R.string.graph_date_range_rtl, Utils.getMonthFromString(strPastOneMonthFull), Utils.getDayFromString(strPastOneMonthFull), Utils.getYearFromString(strPastOneMonthFull), strCurrentMonth, yesterdayInt, strCurrentYear) + getString(R.string.graph_range_text));
                            } else {
                                graphDate.setText(getString(R.string.graph_range_text) + getString(R.string.graph_date_range, Utils.getMonthFromString(strPastOneMonthFull), Utils.getDayFromString(strPastOneMonthFull), Utils.getYearFromString(strPastOneMonthFull), strCurrentMonth, yesterdayInt, strCurrentYear));
                            }
                            }
                        break;

                    case "6 Months":
                        chartTwelveMonths.setVisibility(View.INVISIBLE);
                        chartOneMonth.setVisibility(View.INVISIBLE);
                        chartSixMonths.setVisibility(View.VISIBLE);

                        if (dataPull == DATA_PULL_SUCCESS) {
                            if (getResources().getBoolean(R.bool.is_right_to_left)) {
                                graphDate.setText(getString(R.string.graph_date_range_rtl, Utils.getMonthFromString(strPastSixMonthsFull), Utils.getDayFromString(strPastSixMonthsFull), Utils.getYearFromString(strPastSixMonthsFull), strCurrentMonth, yesterdayInt, strCurrentYear) + getString(R.string.graph_range_text));
                            } else {
                                graphDate.setText(getString(R.string.graph_range_text) + getString(R.string.graph_date_range, Utils.getMonthFromString(strPastSixMonthsFull), Utils.getDayFromString(strPastSixMonthsFull), Utils.getYearFromString(strPastSixMonthsFull), strCurrentMonth, yesterdayInt, strCurrentYear));
                            }
                            }
                        break;

                    case "1 Year":
                        chartOneMonth.setVisibility(View.INVISIBLE);
                        chartSixMonths.setVisibility(View.INVISIBLE);
                        chartTwelveMonths.setVisibility(View.VISIBLE);

                        if (dataPull == DATA_PULL_SUCCESS) {
                            if (getResources().getBoolean(R.bool.is_right_to_left)) {
                                graphDate.setText(getString(R.string.graph_date_range_rtl, Utils.getMonthFromString(strPastYearFull), Utils.getDayFromString(strPastYearFull), Utils.getYearFromString(strPastYearFull), strCurrentMonth, yesterdayInt, strCurrentYear) + getString(R.string.graph_range_text));
                            } else {
                                graphDate.setText(getString(R.string.graph_range_text) + getString(R.string.graph_date_range, Utils.getMonthFromString(strPastYearFull), Utils.getDayFromString(strPastYearFull), Utils.getYearFromString(strPastYearFull), strCurrentMonth, yesterdayInt, strCurrentYear));
                            }
                            }
                        break;
                }
                return true;
            }
        };
        restoreActionBar(adapter, callback);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mContext = this;

        dialog = new ProgressDialog(this);

        chartTwelveMonths = (LineChartView) findViewById(R.id.linechart_one_year);
        chartSixMonths = (LineChartView) findViewById(R.id.linechart_six_months);
        chartOneMonth = (LineChartView) findViewById(R.id.linechart_one_month);

        graphDate = (TextView) findViewById(R.id.graph_date);

        id = getIntent().getStringExtra("id");

        GraphDataPull gD = new GraphDataPull();
        gD.execute();
    }

    //If nothing can be displayed, show this message
    protected void noGraphToDisplay(){
        graphDate.setTextColor(getResources().getColor(R.color.white));
        graphDate.setText(getString(R.string.couldnt_get_graph));
    }

    protected void createGraphs(LineSet dataSet) {

        //get our axis values (this graph library requires the min and max values to be divisible so I went to the closest 10 to keep it clean)
        //If the values are small I wanted it to zoom properly so it also takes into account values that are less than 10 and therefore
        //shouldnt be rounded the same
        int minYearRoundedAxisInt = (int) Utils.getMinRoundedAxisValue(minYearValue, maxYearValue);
        int maxYearRoundedAxisInt = (int) Utils.getMaxRoundedAxisValue(maxYearValue);

        int minSixMonthRoundedAxisInt = (int) Utils.getMinRoundedAxisValue(minSixMonthValue, maxSixMonthValue);
        int maxSixMonthRoundedAxisInt = (int) Utils.getMaxRoundedAxisValue(maxSixMonthValue);

        int minOneMonthRoundedAxisInt = (int) Utils.getMinRoundedAxisValue(minOneMonthValue, maxOneMonthValue);
        int maxOneMonthRoundedAxisInt = (int) Utils.getMaxRoundedAxisValue(maxOneMonthValue);


        //Set our initial graphDate
        if (getResources().getBoolean(R.bool.is_right_to_left)) {
            graphDate.setText(getString(R.string.graph_date_range_rtl, Utils.getMonthFromString(strPastYearFull), Utils.getDayFromString(strPastYearFull), Utils.getYearFromString(strPastYearFull), strCurrentMonth, yesterdayInt, strCurrentYear) + getString(R.string.graph_range_text));
        } else {
            graphDate.setText(getString(R.string.graph_range_text) + getString(R.string.graph_date_range, Utils.getMonthFromString(strPastYearFull), Utils.getDayFromString(strPastYearFull), Utils.getYearFromString(strPastYearFull), strCurrentMonth, yesterdayInt, strCurrentYear));
        }


        //Twelve Month Chart
        dataSet.setColor(getResources().getColor(R.color.material_blue_500));
        dataSet.setThickness(lineThickness);

        chartTwelveMonths.addData(dataSet);

        chartTwelveMonths.setAxisBorderValues(minYearRoundedAxisInt, maxYearRoundedAxisInt, Utils.getAxisInterval(minYearRoundedAxisInt, maxYearRoundedAxisInt));
        chartTwelveMonths.setXLabels(AxisController.LabelPosition.NONE);
        chartTwelveMonths.setXAxis(false);
        chartTwelveMonths.setAxisColor(getResources().getColor(R.color.material_green_700));
        chartTwelveMonths.setLabelsColor(getResources().getColor(R.color.white));
        chartTwelveMonths.show();


        //Six Month Chart
        dataSetSixMonths.setColor(getResources().getColor(R.color.material_blue_500));
        dataSetSixMonths.setThickness(lineThickness);

        chartSixMonths.addData(dataSetSixMonths);

        chartSixMonths.setXLabels(AxisController.LabelPosition.NONE);
        chartSixMonths.setAxisBorderValues(minSixMonthRoundedAxisInt, maxSixMonthRoundedAxisInt, Utils.getAxisInterval(minSixMonthRoundedAxisInt, maxSixMonthRoundedAxisInt));
        chartSixMonths.setXAxis(false);
        chartSixMonths.setAxisColor(getResources().getColor(R.color.material_green_700));
        chartSixMonths.setLabelsColor(getResources().getColor(R.color.white));
        chartSixMonths.show();
        chartSixMonths.setVisibility(View.INVISIBLE);

        //One Month Chart
        dataSetOneMonth.setColor(getResources().getColor(R.color.material_blue_500));
        dataSetOneMonth.setThickness(lineThickness);

        chartOneMonth.addData(dataSetOneMonth);

        chartOneMonth.setXLabels(AxisController.LabelPosition.NONE);
        chartOneMonth.setAxisBorderValues(minOneMonthRoundedAxisInt, maxOneMonthRoundedAxisInt, Utils.getAxisInterval(minOneMonthRoundedAxisInt, maxOneMonthRoundedAxisInt));
        chartOneMonth.setXAxis(false);
        chartOneMonth.setAxisColor(getResources().getColor(R.color.material_green_700));
        chartOneMonth.setLabelsColor(getResources().getColor(R.color.white));
        chartOneMonth.show();
        chartOneMonth.setVisibility(View.INVISIBLE);

        if (getResources().getBoolean(R.bool.is_right_to_left)){
            chartTwelveMonths.setRotationY(180);
            chartSixMonths.setRotationY(180);
            chartOneMonth.setRotationY(180);
        }

        //Create our a11y integers so they can be entered into the strings for setContentDescription
        int a11y_maxOneMonth = (int) maxOneMonthValue;
        int a11y_minOneMonth = (int) minOneMonthValue;
        int a11y_maxSixMonth = (int) maxSixMonthValue;
        int a11y_minSixMonth = (int) minSixMonthValue;
        int a11y_maxTwelveMonth = (int) maxYearValue;
        int a11y_minTwelveMonth = (int) minYearValue;

        //Graph a11y descriptions
        chartOneMonth.setContentDescription(getString(R.string.a11y_graph_one_month, a11y_maxOneMonth, a11y_minOneMonth));
        chartSixMonths.setContentDescription(getString(R.string.a11y_graph_six_month, a11y_maxSixMonth, a11y_minSixMonth));
        chartTwelveMonths.setContentDescription(getString(R.string.a11y_graph_twelve_month, a11y_maxTwelveMonth, a11y_minTwelveMonth));

    }


    public class GraphDataPull extends AsyncTask<Void, Void, LineSet> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Loading Graph");
            dialog.show();
        }

        @Override
        protected LineSet doInBackground(Void... params) {

            getSymbolFromIntent();
            getCalendarDates();

            Cursor cDataExists = getContentResolver().query(HistoricalProvider.HistoricalData.CONTENT_URI,
                    new String[]{HistoricalColumns.SYMBOL, HistoricalColumns.DATE}, HistoricalColumns.SYMBOL + "= ?", new String[]{symbol}, null);

            String databaseLatestDate;
            String databaseDay = strCurrentDay;
            String databaseMonth = strCurrentMonth;
            String databaseYear = strCurrentYear;
            String databaseFullDate = "";

            if (cDataExists.moveToFirst()) {
                try {
                    cDataExists.moveToFirst();
                    databaseLatestDate = cDataExists.getString(cDataExists.getColumnIndex("date"));

                    StringTokenizer tokens = new StringTokenizer(databaseLatestDate, "-");
                    databaseYear = tokens.nextToken();
                    databaseMonth = tokens.nextToken();
                    databaseDay = tokens.nextToken();

                } catch (Exception e) {
                    Log.v(LOG_TAG, "Exception e in trying to moveToLast in the cClosePrices cursor");
                }
                databaseFullDate = databaseYear + "-" + databaseMonth + "-" + databaseDay;
            }

            Log.v(LOG_TAG, "THIS IS THE DATABASE Date = " + databaseFullDate + " And this is the Current Day" + strCurrentFull);

            if (!cDataExists.moveToFirst() || !databaseFullDate.equals(strYesterdayFull) || databaseFullDate.equals("")) {

                String historicalRawData = null;

                try {
                    historicalRawData = fetchData("https://query.yahooapis.com/v1/public/yql?q=select+*+from+yahoo.finance.historicaldata+where+symbol+in+%28%27" + symbol + "%27%29%20and%20startDate%20=%20%27" + strPastYearFull + "%27%20and%20endDate%20=%20%27" + strCurrentFull + "%27&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys");
                    getContentResolver().delete(HistoricalProvider.HistoricalData.withSymbol(symbol), null, null);

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                try {
                    getContentResolver().applyBatch(HistoricalProvider.AUTHORITY,
                            Utils.historicalJsonToContentVals(historicalRawData));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
            }
            cDataExists.close();
            try {
                return setGraphDataSets();
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LineSet dataSet) {
            super.onPostExecute(dataSet);

            mTitle = symbol.toUpperCase();
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(mTitle);

            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e){
                }
            }
            if (dataSet != null) {
                dataPull = DATA_PULL_SUCCESS;
                createGraphs(dataSet);
            } else {
                noGraphToDisplay();
            }
        }

        //Fetch our JSON data
        String fetchData(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        //Use the ID to grab the symbol from the database
        void getSymbolFromIntent() {
            Cursor cSymbol = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL}, QuoteColumns._ID + "= ?", new String[]{id}, null);

            if (cSymbol.moveToFirst()) {
                symbol = cSymbol.getString(cSymbol.getColumnIndex("symbol"));
            }
            cSymbol.close();
        }

        //Get all calendar dates for the 12 month and 6 month and one month
        //Also so we can check if the data has already been pulled we need to account for the previous day so we have to know how many
        //days there were in the previous month
        void getCalendarDates() {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

            strCurrentYear = yearFormat.format(calendar.getTime());
            strCurrentMonth = monthFormat.format(calendar.getTime());
            strCurrentDay = dayFormat.format(calendar.getTime());

            int dayInt = Integer.parseInt(strCurrentDay);
            int monthInt = Integer.parseInt(strCurrentMonth);
            int yearInt = Integer.parseInt(strCurrentYear);

            yesterdayInt = dayInt - 1;
            if (yesterdayInt == 0) {
                switch (monthInt){
                    case 1:
                        yesterdayInt = 31;
                        break;
                    case 2:
                        yesterdayInt = 28;
                        break;
                    case 3:
                        yesterdayInt = 31;
                        break;
                    case 4:
                        yesterdayInt = 30;
                        break;
                    case 5:
                        yesterdayInt = 31;
                        break;
                    case 6:
                        yesterdayInt = 30;
                        break;
                    case 7:
                        yesterdayInt = 31;
                        break;
                    case 8:
                        yesterdayInt = 31;
                        break;
                    case 9:
                        yesterdayInt = 30;
                        break;
                    case 10:
                        yesterdayInt = 31;
                        break;
                    case 11:
                        yesterdayInt = 30;
                        break;
                    case 12:
                        yesterdayInt = 31;
                        break;
                }
            }


            strPastYearFull = formatDatesToStrings(strCurrentDay, monthInt, yearInt, 12);
            strPastSixMonthsFull = formatDatesToStrings(strCurrentDay, monthInt, yearInt, 6);
            strPastOneMonthFull = formatDatesToStrings(strCurrentDay, monthInt, yearInt, 1);
            strCurrentFull = strCurrentYear + "-" + strCurrentMonth + "-" + strCurrentDay;
            strYesterdayFull = strCurrentYear + "-" + strCurrentMonth + "-" + yesterdayInt;
        }

        String formatDatesToStrings(String currentDay, int currentMonth, int currentYear, int monthsBack){

            int subtractMonth = currentMonth - monthsBack;
            int subtractYear = currentYear;
            String strPastMonth = "" + subtractMonth;

            if (subtractMonth <= 0) {
                subtractMonth = subtractMonth + 12;

                strPastMonth = "" + subtractMonth;

                if (subtractMonth < 10) {
                    strPastMonth = "0" + subtractMonth;
                }
                subtractYear = currentYear - 1;
            }
            return subtractYear + "-" + strPastMonth + "-" + currentDay;
        }

        LineSet setGraphDataSets() throws ParseException {
            Cursor cClosePrices = getContentResolver().query(HistoricalProvider.HistoricalData.CONTENT_URI,
                    new String[]{HistoricalColumns.SYMBOL, HistoricalColumns.DATE, HistoricalColumns.CLOSE}, HistoricalColumns.SYMBOL + "= ?", new String[]{symbol}, null);

            float closePrice;
            String dataDate;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date thisDate;
            Date sixMonthDate = sdf.parse(strPastSixMonthsFull);
            Date oneMonthDate = sdf.parse(strPastOneMonthFull);

            cClosePrices.moveToLast();

            //While loop to iterate through all values and set One month, Six Month, and 12 Month values
            while (cClosePrices.moveToPrevious()) {

                closePrice = cClosePrices.getFloat(cClosePrices.getColumnIndex("close"));
                dataDate = cClosePrices.getString(cClosePrices.getColumnIndex("date"));

                thisDate = sdf.parse(dataDate);

                //Set the 12 month values (all values) and the min and max
                if (closePrice < minYearValue) {
                    minYearValue = closePrice;
                }

                if (closePrice > maxYearValue) {
                    maxYearValue = closePrice;
                }

                dataSetTwelveMonths.addPoint("", closePrice);


                //Set the six month values and min max values
                if (thisDate.after(sixMonthDate)){

                    if (closePrice < minSixMonthValue) {
                        minSixMonthValue = closePrice;
                    }

                    if (closePrice > maxSixMonthValue) {
                        maxSixMonthValue = closePrice;
                    }

                    dataSetSixMonths.addPoint("", closePrice);
                }

                //Set the one Month Data and the Min and Max values
                if (thisDate.after(oneMonthDate)){

                    if (closePrice < minOneMonthValue) {
                        minOneMonthValue = closePrice;
                    }

                    if (closePrice > maxOneMonthValue) {
                        maxOneMonthValue = closePrice;
                    }
                    dataSetOneMonth.addPoint("", closePrice);
                }


            }
            cClosePrices.close();

            return dataSetTwelveMonths;
        }

    }
}

