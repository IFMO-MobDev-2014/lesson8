package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

import ru.ifmo.md.lesson8.json.WeatherDownloader;
import ru.ifmo.md.lesson8.json.WeatherForecast;
import ru.ifmo.md.lesson8.json.WeatherHistory;

import static ru.ifmo.md.lesson8.WeatherColumns.CITIES;
import static ru.ifmo.md.lesson8.WeatherColumns.CITY_ID;
import static ru.ifmo.md.lesson8.WeatherColumns.CITY_NAME;
import static ru.ifmo.md.lesson8.WeatherColumns.DESCRIPTION;
import static ru.ifmo.md.lesson8.WeatherColumns.HUMIDITY;
import static ru.ifmo.md.lesson8.WeatherColumns.IN_BRIEF;
import static ru.ifmo.md.lesson8.WeatherColumns.PRESSURE;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_CUR;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MAX;
import static ru.ifmo.md.lesson8.WeatherColumns.TEMP_MIN;
import static ru.ifmo.md.lesson8.WeatherColumns.TIME;
import static ru.ifmo.md.lesson8.WeatherColumns.WEATHER_DATA;
import static ru.ifmo.md.lesson8.WeatherColumns.WIND_ANGLE;
import static ru.ifmo.md.lesson8.WeatherColumns.WIND_SPEED;
import static ru.ifmo.md.lesson8.WeatherColumns._ID;

public class WeatherStorage extends ContentProvider {
    private WeatherDatabase database;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        database = new WeatherDatabase(getContext());
        return true;
    }

    long[] getDayRange(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long result[] = new long[2];
        result[0] = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DATE, 1);
        result[1] = calendar.getTimeInMillis() / 1000;
        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (uri.getPathSegments().size() == 1 && uri.getLastPathSegment().equals("weather")) {
            String city, cityCondition;
            if (uri.getQueryParameterNames().contains("city")) {
                city = uri.getQueryParameter("city");
                cityCondition = "(SELECT " + CITY_NAME + " FROM " + CITIES + " WHERE " + _ID + " = " + CITY_ID + ") = ?";
            } else {
                city = uri.getQueryParameter("id");
                cityCondition = CITY_ID + " = ?";
            }
            long[] period = getDayRange(Long.parseLong(uri.getQueryParameter("time")));
            Cursor result = null;
            {
                SQLiteDatabase db = database.getReadableDatabase();
                Cursor probe = db.query(WEATHER_DATA, null, cityCondition +
                        " AND " + TIME + " >= " + period[0] + " AND " + TIME + " <= " + period[1], new String[]{city}, null, null, TIME + " ASC");
                if (probe.getCount() >= 4)
                    result = probe;
            }
            if (result == null) {
                update(uri, null, null, null);
                return query(uri, projection, selection, selectionArgs, sortOrder);
            }
            return result;
        }
        throw new IllegalArgumentException("Wrong query URI");
    }

    @Override
    public int update(Uri uri, ContentValues v, String selection,
                      String[] selectionArgs) {
        if (uri.getPathSegments().size() == 1 && uri.getLastPathSegment().equals("weather")) {
            String cityName = null;
            String city;
            if (uri.getQueryParameterNames().contains("city")) {
                cityName = uri.getQueryParameter("city");
                city = "q=" + Uri.encode(cityName);
            } else
                city = "id=" + Uri.encode(uri.getQueryParameter("id"));
            long[] period = getDayRange(Long.parseLong(uri.getQueryParameter("time")));
            ContentValues past[] = WeatherDownloader.downloadFromUrl("http://api.openweathermap.org/data/2.5/history/city?" +
                    city + "&type=hour&start=" + Uri.encode(Long.toString(period[0])) +
                    "&end=" + Uri.encode(Long.toString(period[1])), WeatherHistory.class).getWeatherInfo();
            if (period[1] >= System.currentTimeMillis()) {
                ContentValues future[] = WeatherDownloader.downloadFromUrl("http://api.openweathermap.org/data/2.5/forecast?" + city + "&cnt=8", WeatherForecast.class).getWeatherInfo();
                past = Arrays.copyOf(past, past.length + future.length);
                System.arraycopy(future, 0, past, past.length - future.length, future.length);
            }
            Log.d("WeatherStorage", "Queried period [" + period[0] + ", " + period[1] + "]");
            Log.d("WeatherStorage", "Loaded period  [" + past[0].getAsLong(TIME) + ", " + past[past.length - 1].getAsLong(TIME) + "]");
            SQLiteDatabase db = database.getWritableDatabase();
            int result = 0;
            if (cityName != null) {
                boolean cityExists = db.query(CITIES, null, CITY_NAME + " = ?", new String[]{cityName}, null, null, null).getCount() == 1;
                if (!cityExists) {
                    ContentValues cityData = new ContentValues(2);
                    cityData.put(_ID, past[0].getAsLong(CITY_ID));
                    cityData.put(CITY_NAME, cityName);
                    result += db.insert(CITIES, null, cityData);
                }
            }
            db.beginTransaction();
            try {
                result += db.delete(WEATHER_DATA, CITY_ID + " = " + past[0].getAsLong(CITY_ID)
                        + " AND " + TIME + " >= " + past[0].getAsLong(TIME)
                        + " AND " + TIME + " <= " + past[past.length - 1].getAsLong(TIME), null);
                for (ContentValues values : past)
                    result += db.insert(WEATHER_DATA, null, values);
                db.setTransactionSuccessful();
            } catch (RuntimeException ignore) {
                throw ignore;
            } finally {
                db.endTransaction();
            }
            return result;
        }
        throw new IllegalArgumentException("Wrong update URI");
    }

    class WeatherDatabase extends SQLiteOpenHelper {
        WeatherDatabase(Context context) {
            super(context, "weather.db", null, 1);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!db.isReadOnly())
                db.execSQL("PRAGMA foreign_keys=ON;");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CITIES + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    CITY_NAME + " TEXT NOT NULL);");
            db.execSQL("CREATE TABLE " + WEATHER_DATA + " (" +
                    CITY_ID + " INTEGER REFERENCES " + CITIES + " ON DELETE CASCADE," +
                    TIME + " INTEGER NOT NULL," +
                    IN_BRIEF + " TEXT," +
                    DESCRIPTION + " TEXT," +
                    TEMP_MIN + " REAL," +
                    TEMP_CUR + " REAL," +
                    TEMP_MAX + " REAL," +
                    WIND_SPEED + " REAL," +
                    WIND_ANGLE + " REAL," +
                    HUMIDITY + " REAL," +
                    PRESSURE + " REAL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CITIES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + WEATHER_DATA + ";");
            onCreate(db);
        }
    }
}
