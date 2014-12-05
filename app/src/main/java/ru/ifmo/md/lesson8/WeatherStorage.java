package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

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
    static final String CITY_CONDITION = "(SELECT " + CITY_NAME + " FROM " + CITIES + " WHERE " + _ID + " = " + CITY_ID + ") = ?";
    private static ObjectMapper mapper;
    private WeatherDatabase database;
    static {
        WeatherStorage.mapper = new ObjectMapper();
        WeatherStorage.mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    private static <T> T downloadFromUrl(String url, Class<T> datatype) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        return mapper.readValue(connection.getInputStream(), datatype);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri.getPathSegments().size() == 1 && uri.getLastPathSegment().equals("city")
                && uri.getQueryParameterNames().contains("name"))
            return database.getWritableDatabase().delete(CITIES, CITY_NAME + " = ?", new String[]{uri.getQueryParameter("name")});
        throw new IllegalArgumentException("Wrong delete URI");
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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (uri.getPathSegments().size() == 1)
            switch (uri.getLastPathSegment()) {
                case "weather":
                    String city = uri.getQueryParameter("city");
                    long[] period = WeatherActivity.getDayRange(Long.parseLong(uri.getQueryParameter("time")));
                    Log.v("WeatherStorage", "Loading from period [" + period[0] + ", " + period[1] + "]");
                    return database.getReadableDatabase().query(WEATHER_DATA, null, CITY_CONDITION +
                            " AND " + TIME + " >= " + period[0] + " AND " + TIME + " <= " + period[1], new String[]{city}, null, null, TIME + " ASC");
                case "cities":
                    return database.getReadableDatabase().query(CITIES, new String[]{CITY_NAME}, null, null, null, null, null);
            }
        throw new IllegalArgumentException("Wrong query URI");
    }

    @Override
    public int update(Uri uri, ContentValues v, String selection,
                      String[] selectionArgs) {
        if (uri.getPathSegments().size() == 1 && uri.getLastPathSegment().equals("weather")) {
            String cityName = uri.getQueryParameter("city");
            String city = "q=" + Uri.encode(cityName);
            long[] period = WeatherActivity.getDayRange(Long.parseLong(uri.getQueryParameter("time")));
            ContentValues past[];
            try {
                past = downloadFromUrl("http://api.openweathermap.org/data/2.5/history/city?" +
                        city + "&type=hour&start=" + Uri.encode(Long.toString(period[0])) +
                        "&end=" + Uri.encode(Long.toString(period[1])), WeatherHistory.class).getWeatherInfo();
                if (period[1] >= System.currentTimeMillis() / 1000) {
                    ContentValues future[] = downloadFromUrl("http://api.openweathermap.org/data/2.5/forecast?" + city, WeatherForecast.class).getWeatherInfo();
                    past = Arrays.copyOf(past, past.length + future.length);
                    System.arraycopy(future, 0, past, past.length - future.length, future.length);
                }
            } catch (IOException e) {
                return 0;
            }
            Arrays.sort(past, new Comparator<ContentValues>() {
                @Override
                public int compare(ContentValues lhs, ContentValues rhs) {
                    return Long.compare(lhs.getAsLong(TIME), rhs.getAsLong(TIME));
                }
            });
            Log.d("WeatherStorage", "Queried period [" + period[0] + ", " + period[1] + "]");
            if (past.length > 0) {
                Log.d("WeatherStorage", "Loaded period [" + past[0].getAsLong(TIME) + ", " + past[past.length - 1].getAsLong(TIME) + "]");
                Log.v("WeatherStorage", "(" + past.length + " entries)");
            } else
                Log.d("WeatherStorage", "Loaded no data");
            SQLiteDatabase db = database.getWritableDatabase();
            int result = 0;
            boolean cityExists = db.query(CITIES, null, CITY_NAME + " = ?", new String[]{cityName}, null, null, null).getCount() == 1;
            if (!cityExists) {
                ContentValues cityData = new ContentValues(2);
                cityData.put(_ID, past[0].getAsLong(CITY_ID));
                cityData.put(CITY_NAME, cityName);
                result += db.insert(CITIES, null, cityData);
            }
            db.beginTransaction();
            try {
                result += db.delete(WEATHER_DATA, CITY_ID + " = " + past[0].getAsLong(CITY_ID)
                        + " AND " + TIME + " >= " + past[0].getAsLong(TIME)
                        + " AND " + TIME + " <= " + past[past.length - 1].getAsLong(TIME), null);
                for (ContentValues values : past)
                    result += db.insert(WEATHER_DATA, null, values);
                db.setTransactionSuccessful();
                Log.v("WeatherStorage", "Transation successful");
            } catch (RuntimeException ignore) {
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
