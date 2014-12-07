package ru.ifmo.md.lesson8.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ru.ifmo.md.lesson8.places.Place;

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
        final String[] projection = {Cities.COUNTRY_COLUMN, Cities.NAME_COLUMN, Cities.WOEID_COLUMN};
        final Cursor cursor = context.getContentResolver().query(Cities.URI, projection, null, null, null);
        cursor.moveToFirst();

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
                        Place place = new Place.Builder()
                                .setCountry(cursor.getString(0))
                                .setName(cursor.getString(1))
                                .setWoeid(Long.parseLong(cursor.getString(2)))
                                .createPlace();
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

    public void addPlace(Place place) {
        ContentValues values = new ContentValues();
        values.put(Cities.COUNTRY_COLUMN, place.getCountry());
        values.put(Cities.NAME_COLUMN, place.getName());
        values.put(Cities.WOEID_COLUMN, place.getWoeid());
        context.getContentResolver().insert(Cities.URI, values);
    }
}
