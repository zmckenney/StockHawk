package com.sam_chordas.android.stockhawk.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Zac on 6/9/16.
 */

@ContentProvider(authority = HistoricalProvider.AUTHORITY, database = HistoricalDatabase.class)
public class HistoricalProvider {
    public static final String AUTHORITY = "com.sam_chordas.android.stockhawk.data.HistoricalProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String HISTORICAL_DATA = "historical_data";
    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path:paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = HistoricalDatabase.HISTORICAL_DATA)
    public static class HistoricalData{
        @ContentUri(
                path = Path.HISTORICAL_DATA,
                type = "vnd.android.cursor.dir/historical"
        )
        public static final Uri CONTENT_URI = buildUri(Path.HISTORICAL_DATA);

        @InexactContentUri(
                name = "HISTORICAL_ID",
                path = Path.HISTORICAL_DATA + "/*",
                type = "vnd.android.cursor.item/historical",
                whereColumn = HistoricalColumns.SYMBOL,
                pathSegment = 1
        )
        public static Uri withSymbol(String symbol){
            return buildUri(Path.HISTORICAL_DATA, symbol);
        }


    }

}
