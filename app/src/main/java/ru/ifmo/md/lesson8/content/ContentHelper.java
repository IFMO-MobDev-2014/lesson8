package ru.ifmo.md.lesson8.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.weather.Forecast;
import ru.ifmo.md.lesson8.weather.Temperature;
import ru.ifmo.md.lesson8.weather.Weather;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class ContentHelper {
    private final ContentResolver contentResolver;

    public ContentHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ContentHelper(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    public static Date getCurrentDate() {
        GregorianCalendar current = new GregorianCalendar();
        GregorianCalendar calendar = new GregorianCalendar(
                current.get(GregorianCalendar.YEAR),
                current.get(GregorianCalendar.MONTH),
                current.get(GregorianCalendar.DAY_OF_MONTH));
        return calendar.getTime();
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
        Cursor cursor = contentResolver.query(Places.URI, projection, null, null, null);
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
        contentResolver.insert(Places.URI, values);
    }

    public Weather getWeatherInPlace(Place place) {
        String[] projection = {
                WeatherInfo.TEMPERATURE_COLUMN,
                WeatherInfo.DESCRIPTION_COLUMN,
                WeatherInfo.WIND_COLUMN,
                WeatherInfo.HUMIDITY_COLUMN
        };
        String selection = WeatherInfo.WOEID_COLUMN + "=?"
                + " and " + WeatherInfo.DATE_COLUMN + "=?";
        String[] selectionArgs = {"" + place.getWoeid(), "" + getCurrentDate().getTime()};
        Cursor cursor = contentResolver.query(
                WeatherInfo.URI, projection, selection, selectionArgs, null);

        Weather result;
        if (!cursor.moveToFirst()) {
            result = null;
        } else {
            result = new Weather.Builder()
                    .setCurrent(new Temperature(Integer.parseInt(cursor.getString(0)),
                            Temperature.fahrenheit()))
                    .setDescription(cursor.getString(1))
                    .setWindSpeed(Integer.parseInt(cursor.getString(2)))
                    .setHumidity(Integer.parseInt(cursor.getString(3)))
                    .createWeather();
        }
        return result;
    }

    public void setForecasts(Place place, List<Forecast> forecasts) {
        String selection = WeatherInfo.WOEID_COLUMN + "=?";
        String[] selectionArgs = {"" + place.getWoeid()};
        contentResolver.delete(WeatherInfo.URI, selection, selectionArgs);

        for (Forecast forecast : forecasts) {
            Weather weather = forecast.getWeather();
            Date date = forecast.getDate();
            ContentValues values = weather.toContentValues();
            values.put(WeatherInfo.WOEID_COLUMN, place.getWoeid());
            values.put(WeatherInfo.DATE_COLUMN, date.getTime());
            contentResolver.insert(WeatherInfo.URI, values);
        }
    }

    public Place getPlaceByWoeid(int woeid) {
        String[] projection = {Places.NAME_COLUMN, Places.COUNTRY_COLUMN};
        String selection = Places.WOEID_COLUMN + "=?";
        String[] selectionArgs = {woeid + ""};
        Cursor cursor = contentResolver.query(Places.URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        return new Place(cursor.getString(cursor.getColumnIndex(Places.COUNTRY_COLUMN)),
                cursor.getString(cursor.getColumnIndex(Places.NAME_COLUMN)),
                woeid);
    }

    public List<Forecast> getForecasts(int woeid) {
        List<Forecast> forecasts = new ArrayList<>();

        Cursor cursor = getForecastsCursor(woeid);
        if (cursor.moveToFirst()) {
            do {
                Forecast forecast = getForecast(cursor);
                forecasts.add(forecast);
            } while (cursor.moveToNext());
        }

        return forecasts;
    }

    public Cursor getForecastsCursor(int woeid) {
        String[] projection = {
                WeatherInfo._ID,
                WeatherInfo.DATE_COLUMN,
                WeatherInfo.TEMPERATURE_COLUMN,
                WeatherInfo.LOW_COLUMN,
                WeatherInfo.HIGH_COLUMN,
                WeatherInfo.DESCRIPTION_COLUMN};
        String selection = WeatherInfo.WOEID_COLUMN + " =?";
        String[] selectionArgs = {"" + woeid};
        return contentResolver.query(WeatherInfo.URI, projection, selection, selectionArgs,
                WeatherInfo.DATE_COLUMN);
    }

    public static Forecast getForecast(Cursor cursor) {
        Temperature current;
        Temperature high;
        Temperature low;
        Integer windSpeed;
        Integer humidity;

        long time = cursor.getLong(cursor.getColumnIndex(WeatherInfo.DATE_COLUMN));
        String desc = cursor.getString(cursor.getColumnIndex(WeatherInfo.DESCRIPTION_COLUMN));

        try {
            current = new Temperature(
                    cursor.getInt(cursor.getColumnIndex(WeatherInfo.TEMPERATURE_COLUMN)),
                    Temperature.fahrenheit()
            );
        } catch (Exception e) {
            current = null;
        }

        try {
            high = new Temperature(
                    cursor.getInt(cursor.getColumnIndex(WeatherInfo.TEMPERATURE_COLUMN)),
                    Temperature.fahrenheit()
            );
        } catch (Exception e) {
            high = null;
        }

        try {
            low = new Temperature(
                    cursor.getInt(cursor.getColumnIndex(WeatherInfo.TEMPERATURE_COLUMN)),
                    Temperature.fahrenheit()
            );
        } catch (Exception e) {
            low = null;
        }

        try {
            windSpeed = cursor.getInt(cursor.getColumnIndex(WeatherInfo.WIND_COLUMN));
        } catch (Exception ignored) {
            windSpeed = null;
        }

        try {
            humidity = cursor.getInt(cursor.getColumnIndex(WeatherInfo.HUMIDITY_COLUMN));
        } catch (Exception e) {
            humidity = null;
        }

        Weather.Builder weatherBuilder = new Weather.Builder();

        return new Forecast(new Date(time),
                weatherBuilder
                .setLow(low)
                .setHigh(high)
                .setCurrent(current)
                .setDescription(desc)
                .setWindSpeed(windSpeed)
                .setHumidity(humidity)
                .createWeather());
    }

    public static int getResourceForConditionString(String condition) {
        condition = condition.toLowerCase();
        if (condition.contains("sun")
                || condition.contains("clea")) {
            return R.drawable.sunny;
        } else if (condition.contains("snow")) {
            return R.drawable.snow;
        } else if (condition.contains("rain")) {
            return R.drawable.rain;
        } else if (condition.contains("cloud")) {
            return R.drawable.cloudy;
        } else { // wind r
            return R.drawable.wind;
        }
    }
}
