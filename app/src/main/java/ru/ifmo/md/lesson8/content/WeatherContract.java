package ru.ifmo.md.lesson8.content;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class WeatherContract {
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "ru.ifmo.md.lesson8.WeatherDatabase";
    public static final String AUTHORITY = "ru.ifmo.md.lesson8.contentprovider";

    public static final class Places implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String WOEID_COLUMN = "woeid";
        public static final String NAME_COLUMN = "name";
        public static final String COUNTRY_COLUMN = "country";
        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + "("
                + _ID + " integer primary key, "
                + WOEID_COLUMN + " integer, "
                + NAME_COLUMN + " text, "
                + COUNTRY_COLUMN + " text"
                + ")";
        public static final String DELETE_TABLE = "drop table if exists " + Places.TABLE_NAME;

        public static final String PATH = "places";
        public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        public static final String DIR_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PATH;
        public static final String ITEM_CONTENT_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PATH;

        public static final int LIST_ID = 1;
        public static final int ITEM_ID = 2;
    }

    public static final class WeatherInfo implements BaseColumns {
        public static final String TABLE_NAME = "weather_info";
        public static final String WOEID_COLUMN = "woeid";
        public static final String DATE_COLUMN = "date";
        /**
         * Temperature stored in db is represented using Fahrenheit scale.
         */
        public static final String TEMPERATURE_COLUMN = "temperature";
        public static final String HIGH_COLUMN = "high";
        public static final String LOW_COLUMN = "low";
        public static final String WIND_COLUMN = "wind";
        public static final String HUMIDITY_COLUMN = "humidity";
        public static final String DESCRIPTION_COLUMN = "description";
        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + "("
                + _ID + " integer primary key, "
                + WOEID_COLUMN + " integer, "
                + DATE_COLUMN + " integer, "
                + TEMPERATURE_COLUMN + " integer, "
                + HIGH_COLUMN + " integer, "
                + LOW_COLUMN + " integer, "
                + WIND_COLUMN + " integer, "
                + HUMIDITY_COLUMN + " integer, "
                + DESCRIPTION_COLUMN + " text "
                + ")";

        public static final String DELETE_TABLE = "drop table if exists " + WeatherInfo.TABLE_NAME;

        public static final String PATH = "weather_info";
        public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        public static final String DIR_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PATH;
        public static final String ITEM_CONTENT_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PATH;

        public static final int LIST_ID = 3;
        public static final int ITEM_ID = 4;
    }

    private WeatherContract() {
    }
}
