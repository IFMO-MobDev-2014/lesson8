package ru.ifmo.md.lesson8.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Svet on 30.11.2014.
 */
public class DatabaseHelper  extends SQLiteOpenHelper implements BaseColumns {

    public final String TABLE_NAME = "WheatherData";

    //5 columns
    public final String CITY_NAME = "CityName";
    public final String TEMPERATURE_TODAY = "TemperatureToday";
    public final String TEMPERATURE_TOMORROW  = "TemperatureTomorrow";
    public final String TEMPERATURE_AFTER = "TemperatureAfter";

    public final String DESCRIPTION_TODAY = "DescriptionToday";
    public final String DESCRIPTION_TOMORROW = "DescriptionTomorrow";
    public final String DESCRIPTION_AFTER = "DescriptionAfter";

    public final String HUMIDITY = "Humidity";
    public final String PRESSURE = "Pressure";
    public final String WIND_SPEED = "WindSpeed";

    public final String SRC_TODAY = "SrcToday";
    public final String SRC_TOMORROW = "SrcTomorrow";
    public final String SRC_AFTER = "SrcAfter";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableScript = " create table " + TABLE_NAME + "( " +
                BaseColumns._ID + " integer primary key autoincrement, " +
                CITY_NAME + " text not null, " +

                TEMPERATURE_TODAY + " real, " +
                TEMPERATURE_TOMORROW + " real, " +
                TEMPERATURE_AFTER + " real, " +

                DESCRIPTION_TODAY + " text not null, " +
                DESCRIPTION_TOMORROW + " text not null, " +
                DESCRIPTION_AFTER + " text not null, " +

                HUMIDITY + " integer, " +
                PRESSURE + " integer, " +
                WIND_SPEED + " real, " +

                SRC_TODAY + " text not null, " +
                SRC_TOMORROW + " text not null, " +
                SRC_AFTER + " text not null);";
        sqLiteDatabase.execSQL(createTableScript);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
