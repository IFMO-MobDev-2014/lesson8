package weathertogo.sergeybudkov.ru.weathertogo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//Sergey Budkov 2536

public class WeatherDataBase {
    public SQLiteDatabase sqLiteDatabase;
    private Context context;
    public DatabaseHelper databaseHelper;
    public static WeatherDataBase dataBase = null;
    public static final String DATABASE_NAME = "WeatherToGo";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_WEATHER_NAME = "AddedCitiesWeather";
    public static final String ID_WEATHER = "_id";

    public static final String WEATHER_NOW = "weather_now";
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String YANDEX_ID = "id";
    public static final String TEMPERATURE = "temperature";
    public static final String DESCRIPTION = "description";
    public static final String PRESSURE = "pressure";
    public static final String WIND_DIRECTION = "wind_direction";
    public static final String WIND_SPEED = "wind_speed";
    public static final String DAY_PART = "day_part";
    public static final String DATA = "data";
    public static final String HUMIDITY = "humidity";

    private static final String WEATHER_DATABASE_CREATE = "CREATE TABLE " + TABLE_WEATHER_NAME + " (" + ID_WEATHER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CITY + " TEXT NOT NULL, " + COUNTRY + " TEXT NOT NULL, " + YANDEX_ID + " TEXT NOT NULL, " + TEMPERATURE + " TEXT NOT NULL, " +
            DESCRIPTION + " TEXT NOT NULL, " + PRESSURE + " TEXT NOT NULL, " + WIND_DIRECTION + " TEXT NOT NULL, " +
            WIND_SPEED + " TEXT NOT NULL, " + HUMIDITY + " TEXT NOT NULL, " + DAY_PART + " TEXT NOT NULL, " + WEATHER_NOW + " TEXT NOT NULL, " + DATA + " TEXT NOT NULL);";

    public WeatherDataBase(Context context) {
        this.context = context;
    }

    public WeatherDataBase open() throws SQLiteException {
        databaseHelper = new DatabaseHelper(context);
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        return this;

    }

    public void close() {
        databaseHelper.close();
    }

    public long insertWeather(String city, String country, String yandex_id, String temperature_avg,
                              String weather_type, String pressure, String wind_direction, String wind_speed,
                              String day_part, String data, String humidity) {
        ContentValues values = new ContentValues();
        values.put(CITY, city);
        values.put(COUNTRY, country);
        values.put(YANDEX_ID, yandex_id);
        values.put(TEMPERATURE, temperature_avg);
        values.put(DESCRIPTION, weather_type);
        values.put(PRESSURE, pressure);
        values.put(WIND_DIRECTION, wind_direction);
        values.put(WIND_SPEED, wind_speed);
        values.put(DAY_PART, day_part);
        values.put(DATA, data);
        values.put(HUMIDITY, humidity);
        if (MainActivity.CITY_YES.equals(city) && day_part.equals(MainActivity.NOW)) values.put(WEATHER_NOW, MainActivity.YES);
        else values.put(WEATHER_NOW, MainActivity.NO);
        return sqLiteDatabase.insert(TABLE_WEATHER_NAME, null, values);
    }

    public void deleteWeatherInCity(String city, String country) {
        Cursor cursor = sqLiteDatabase.query(WeatherDataBase.TABLE_WEATHER_NAME, new String[] {
                        WeatherDataBase.ID_WEATHER, WeatherDataBase.CITY, WeatherDataBase.COUNTRY, WeatherDataBase.YANDEX_ID,
                        WeatherDataBase.TEMPERATURE, WeatherDataBase.DESCRIPTION, WeatherDataBase.PRESSURE, WeatherDataBase.WIND_DIRECTION,
                        WeatherDataBase.WIND_SPEED, WeatherDataBase.HUMIDITY, WeatherDataBase.DAY_PART, WeatherDataBase.WEATHER_NOW, WeatherDataBase.DATA},
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            if (city.equals(cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY))) &&
                    country.equals(cursor.getString(cursor.getColumnIndex(WeatherDataBase.COUNTRY))))
                sqLiteDatabase.delete(TABLE_WEATHER_NAME, ID_WEATHER + "=" + (new Integer(cursor.getInt(cursor.getColumnIndex(WeatherDataBase.ID_WEATHER)))).toString(), null);
        }
    }

    public int deleteWeatherTable() {
        return sqLiteDatabase.delete(TABLE_WEATHER_NAME, null, null);
    }

    public static WeatherDataBase getDataBase(Context context) {
        if (dataBase == null)
            dataBase = new WeatherDataBase(context.getApplicationContext()).open();
        return dataBase;
    }

    public void changeYesOrNo(String id, String flag) {
        ContentValues values = new ContentValues();
        values.put(WEATHER_NOW, flag);
        sqLiteDatabase.update(TABLE_WEATHER_NAME, values, ID_WEATHER + "=" + id, null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("WEATHER TABLE CREATE", WEATHER_DATABASE_CREATE);
            db.execSQL(WEATHER_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_WEATHER_NAME);
            onCreate(db);
        }
    }
}