package ru.ifmo.md.lesson8.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.weather.Temperature;
import ru.ifmo.md.lesson8.weather.Weather;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class ContentHelper {
    private final Context context;

    public ContentHelper(Context context) {
        this.context = context;
    }

    public Iterable<Place> getPlaces() {
        final Cursor cursor = getPlacesCursor();

        return new Iterable<Place>() {
            @Override
            public Iterator<Place> iterator() {
                return new Iterator<Place>() {
                    @Override
                    public boolean hasNext() {
                        return !cursor.isAfterLast();
                    }

                    @Override
                    public Place next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        Place place = getPlace(cursor);
                        cursor.moveToNext();
                        return place;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public Cursor getPlacesCursor() {
        String[] projection = {Places._ID, Places.COUNTRY_COLUMN, Places.NAME_COLUMN, Places.WOEID_COLUMN};
        Cursor cursor = context.getContentResolver().query(Places.URI, projection, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Place getPlace(Cursor cursor) {
        return new Place(
                cursor.getString(cursor.getColumnIndex(Places.COUNTRY_COLUMN)),
                cursor.getString(cursor.getColumnIndex(Places.NAME_COLUMN)),
                Long.parseLong(cursor.getString(cursor.getColumnIndex(Places.WOEID_COLUMN))));
    }

    public void addPlace(Place place) {
        ContentValues values = new ContentValues();
        values.put(Places.COUNTRY_COLUMN, place.getCountry());
        values.put(Places.NAME_COLUMN, place.getName());
        values.put(Places.WOEID_COLUMN, place.getWoeid());
        context.getContentResolver().insert(Places.URI, values);
    }

    public Weather getWeatherInPlace(Place place) {
        String[] projection = {WeatherInfo.TEMPERATURE_COLUMN};
        String selection = WeatherInfo.WOEID_COLUMN + "=?";
        String[] selectionArgs = {"" + place.getWoeid()};
        Cursor cursor = context.getContentResolver().query(
                WeatherInfo.URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        return new Weather(
                new Temperature(Integer.parseInt(cursor.getString(0)),
                        Temperature.fahrenheit()));
    }
}
