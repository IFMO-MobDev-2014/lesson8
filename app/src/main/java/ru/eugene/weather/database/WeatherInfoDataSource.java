package ru.eugene.weather.database;

/**
 * Created by eugene on 12/16/14.
 */
public class WeatherInfoDataSource {
    public static final String TABLE_NAME = "weather";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_CITY = "id_city";
    public static final String COLUMN_TEMP_CUR = "temp_cur";
    public static final String COLUMN_TEMP_MIN = "temp_min";
    public static final String COLUMN_TEMP_MAX = "temp_max";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_HUMIDITY = "humidity";
    public static final String COLUMN_VISIBILITY = "visibility";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_PUB_DATE = "pub_date";
    public static final String COLUMN_CHILL = "chill";

    public static final String CREATE_COMMAND = "CREATE TABLE " + TABLE_NAME +
            "( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
            ", " + COLUMN_ID_CITY + " INTEGER NOT NULL" +
            ", " + COLUMN_TEMP_CUR + " INTEGER NOT NULL" +
            ", " + COLUMN_TEMP_MIN + " INTEGER NOT NULL" +
            ", " + COLUMN_TEMP_MAX + " INTEGER NOT NULL" +
            ", " + COLUMN_SPEED + " INTEGER NOT NULL" +
            ", " + COLUMN_HUMIDITY + " INTEGER NOT NULL" +
            ", " + COLUMN_VISIBILITY + " INTEGER NOT NULL" +
            ", " + COLUMN_CODE + " INTEGER NOT NULL" +
            ", " + COLUMN_TEXT + " TEXT NOT NULL" +
            ", " + COLUMN_PUB_DATE + " TEXT NOT NULL" +
            ", " + COLUMN_CHILL + " TEXT NOT NULL);";

    public static String[] getProjection() {
        return new String[]{COLUMN_ID, COLUMN_ID_CITY,
                COLUMN_TEMP_CUR, COLUMN_TEMP_MIN,
                COLUMN_TEMP_MAX, COLUMN_SPEED,
                COLUMN_HUMIDITY, COLUMN_VISIBILITY,
                COLUMN_CODE, COLUMN_PUB_DATE,
                COLUMN_TEXT, COLUMN_CHILL};
    }
}