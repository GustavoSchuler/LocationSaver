package br.feevale.locationsaver.db;

import android.provider.BaseColumns;

public class LocationContract {
    public static final String DB_NAME = "locationsaver.db";
    public static final int DB_VERSION = 1;

    public class LocationEntry implements BaseColumns {
        public static final String TABLE = "locations";

        public static final String COL_LOCATION_NAME = "name";
        public static final String COL_LOCATION_LATITUDE = "latitude";
        public static final String COL_LOCATION_LONGITUDE = "longitude";
    }
}
