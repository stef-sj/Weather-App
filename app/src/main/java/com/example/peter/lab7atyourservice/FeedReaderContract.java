package com.example.peter.lab7atyourservice;
import android.provider.BaseColumns;
public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {}
    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_TEMP = "temp";
        public static final String COLUMN_NAME_WIND = "wind";
        public static final String COLUMN_NAME_DES = "des";
        public static final String COLUMN_NAME_CITY = "city";
    }
}
