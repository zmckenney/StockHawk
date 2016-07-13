package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Zac on 6/9/16.
 */

@Database(version = HistoricalDatabase.VERSION)
public class HistoricalDatabase {
    private HistoricalDatabase(){}

    public static final int VERSION = 7;

    @Table(HistoricalColumns.class) public static final String HISTORICAL_DATA = "historical_data";
}
