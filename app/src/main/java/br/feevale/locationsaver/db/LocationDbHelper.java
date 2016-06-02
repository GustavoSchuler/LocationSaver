package br.feevale.locationsaver.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDbHelper extends SQLiteOpenHelper {

    public LocationDbHelper(Context context) {
        super(context, LocationContract.DB_NAME, null, LocationContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + LocationContract.LocationEntry.TABLE + " ( " +
                LocationContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationContract.LocationEntry.COL_LOCATION_NAME + " TEXT NOT NULL, " +
                LocationContract.LocationEntry.COL_LOCATION_LATITUDE + " TEXT NOT NULL, " +
                LocationContract.LocationEntry.COL_LOCATION_LONGITUDE + " TEXT NOT NULL );";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationContract.LocationEntry.TABLE);
        onCreate(db);
    }
}
