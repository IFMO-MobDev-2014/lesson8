package ru.ifmo.md.lesson8.DataClasses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import ru.ifmo.md.lesson8.R;

public class WeatherManager {
    private static String LogMessage = "Weather Manager";

    public static int getCityId(ContentResolver resolver, City city) {
        assert(resolver != null && city != null);
        Cursor c = resolver.query(
                WeatherContentProvider.CITIES_CONTENT,
                new String[]{
                        WeatherContentProvider.CITY_ID
                },
                WeatherContentProvider.WOEID + " = ? ",
                new String[]{
                        String.valueOf(city.getWoeid())
                },
                null);
        int cityId;
        if (c.getCount() == 0) {
            cityId = -1;
        } else {
            c.moveToFirst();
            cityId = c.getInt(c.getColumnIndexOrThrow(WeatherContentProvider.CITY_ID));
        }
        c.close();
        Log.i(LogMessage, "got id of " + city.toString() + " = " + cityId);
        return cityId;
    }

    public static int addCity(ContentResolver resolver, City city, String importantly) {
        assert(resolver != null && city != null && importantly != null);
        assert(importantly.equals(WeatherContentProvider.isImportant)
                || importantly.equals(WeatherContentProvider.isNotImportant));
        int cityId = getCityId(resolver, city);
        if (cityId == -1) {
            ContentValues cv = new ContentValues();
            cv.put(WeatherContentProvider.CITY_NAME, city.getCityName());
            cv.put(WeatherContentProvider.COUNTRY_NAME, city.getCountryName());
            cv.put(WeatherContentProvider.WOEID, city.getWoeid());
            cv.put(WeatherContentProvider.IS_IMPORTANT, importantly);
            Uri uri = resolver.insert(WeatherContentProvider.CITIES_CONTENT, cv);
            cityId = Integer.parseInt(uri.getLastPathSegment());
            Log.i(LogMessage, "Inserted " + city.toString() + " in " + uri);
        }
        Log.i(LogMessage, "Inserted id of " + city.toString() + " with importantly " + importantly + " = " + cityId);
        return cityId;
    }
    public static int addCity(ContentResolver resolver, City city) {
        int cityId = addCity(resolver, city, WeatherContentProvider.isNotImportant);
        return cityId;
    }

    public static void setCurrentWeather(ContentResolver resolver, Weather weather) {
        deleteCurWeatherByCity(resolver, weather.city);
        int cityId = addCity(resolver, weather.city);
        ContentValues curWeather = weather.toCurWeatherContentValues(cityId);
        Uri uri = resolver.insert(WeatherContentProvider.CUR_WEATHER_CONTENT, curWeather);
        Log.i(LogMessage, "inserted curweather in " + weather.city + " " + uri);
    }

    public static void addForecast(ContentResolver resolver, Weather weather) {
        int cityId = addCity(resolver, weather.city);
        ContentValues forecast = weather.toForecastContentValues(cityId);
        Cursor c = resolver.query(
                WeatherContentProvider.FORECAST_CONTENT,
                new String[] {
                        WeatherContentProvider.FORECAST_ID
                },
                WeatherContentProvider.FORECAST_CITY_ID + " = ? AND "
                + WeatherContentProvider.FORECAST_DATE + " = ? ",
                new String[] {
                        Integer.toString(cityId),
                        weather.date
                },
                null);
        if (c.getCount() == 0) {
            Uri uri = resolver.insert(WeatherContentProvider.FORECAST_CONTENT, forecast);
            Log.i(LogMessage, "inserted forecast in " + weather.city.toString() + ", " + uri);
        } else {
            c.moveToFirst();
            int forecastId = c.getInt(c.getColumnIndexOrThrow(WeatherContentProvider.FORECAST_ID));
            int updated = resolver.update(
                    WeatherContentProvider.FORECAST_CONTENT,
                    forecast,
                    WeatherContentProvider.FORECAST_ID + " = ? ",
                    new String[] {
                            Integer.toString(forecastId)
                    }
            );
            Log.i(LogMessage, "updated forecast in " + weather.city.toString() + ", updated " + updated);
        }
    }

    public static Cursor getForecastByCity(ContentResolver resolver, City city) {
        assert(resolver != null && city != null);
        int cityId = getCityId(resolver, city);
        Cursor c = resolver.query(
                WeatherContentProvider.FORECAST_CONTENT,
                null,
                WeatherContentProvider.CUR_WEATHER_CITY_ID + " = ? ",
                new String[] {
                        String.valueOf(cityId)
                },
                null
        );
        Log.i(LogMessage, "got forecast from " + city.toString() + ", got " + c.getCount());
        return c;
    }

    public static void deleteForecastByCity(ContentResolver resolver, City city) {
        if (city == null) {
            return;
        }
        assert(resolver != null);
        int cityId = getCityId(resolver, city);
        int deleted = resolver.delete(
                WeatherContentProvider.FORECAST_CONTENT,
                WeatherContentProvider.FORECAST_CITY_ID + " = ? ",
                new String[] {
                        String.valueOf(cityId)
                }
        );
        Log.i(LogMessage, "deleted forecast from " + city.toString() + ", count = " + deleted);
    }

    public static void deleteCurWeatherByCity(ContentResolver resolver, City city) {
        if (city == null) {
            return;
        }
        assert(resolver != null);

        int cityId = getCityId(resolver, city);
        int deleted = resolver.delete(
                WeatherContentProvider.CUR_WEATHER_CONTENT,
                WeatherContentProvider.CUR_WEATHER_CITY_ID + " = ? ",
                new String[] {
                        String.valueOf(cityId)
                }
        );
        Log.i(LogMessage, "deleted curweather from " + city.toString() + ", count = " + deleted);
    }

    public static void setImportantly(ContentResolver resolver, City city, String importantly) {
        assert(resolver != null && city != null && importantly != null);
        assert(importantly.equals(WeatherContentProvider.isImportant)
                || importantly.equals(WeatherContentProvider.isNotImportant));
        Log.i(LogMessage, "setting importantly of " + city.toString());
        int cityId = getCityId(resolver, city);
        if (cityId == -1) {
            addCity(resolver, city, importantly);
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(WeatherContentProvider.CITY_NAME, city.getCityName());
        cv.put(WeatherContentProvider.COUNTRY_NAME, city.getCountryName());
        cv.put(WeatherContentProvider.WOEID, city.getWoeid());
        cv.put(WeatherContentProvider.IS_IMPORTANT, importantly);
        int updated = resolver.update(
                WeatherContentProvider.CITIES_CONTENT,
                cv,
                WeatherContentProvider.CITY_ID + " = ? ",
                new String[] {
                        String.valueOf(cityId)
                }
        );
        Log.i(LogMessage, "Set importantly to" + importantly + ", updated: " + updated);
    }
    public static String getImportantly(ContentResolver resolver, int cityId) {
        Cursor c = resolver.query(
                WeatherContentProvider.CITIES_CONTENT,
                new String[] {
                        WeatherContentProvider.IS_IMPORTANT
                },
                WeatherContentProvider.CITY_ID + " = ? ",
                new String[] {
                        String.valueOf(cityId)
                },
                null);
        if (c.getCount() == 0) {
            c.close();
            return WeatherContentProvider.isNotImportant;
        }
        c.moveToFirst();
        String res = c.getString(c.getColumnIndexOrThrow(WeatherContentProvider.IS_IMPORTANT));
        c.close();
        assert(res.equals(WeatherContentProvider.isImportant) || res.equals(WeatherContentProvider.isNotImportant));
        Log.i(LogMessage, "Got importantly of city with cityId" + cityId + " = " + res);
        return res;
    }

    public static int getCloudyId(int code) {
        switch (code) {
            case 13:case 14:case 15:case 16:
                return R.drawable.snow;
            case 41:case 42:case 43:case 46:
                return R.drawable.snow_showers;
            case 35:case 6:
                return R.drawable.rain_and_hail;
            case 11:case 12:case 39:case 40:
                return R.drawable.showers;
            case 5:
                return R.drawable.rain_and_snow;
            case 10:
                return R.drawable.freezing_rain;
            case 31:case 33:
                return R.drawable.fair_night;
            case 32:case 34:
                return R.drawable.fair_day;
            case 24:
                return R.drawable.windy;
            case 26:
                return R.drawable.cloudy;
            case 27:
                return R.drawable.mostly_cloudy_night;
            case 28:
                return R.drawable.mostly_cloudy_day;
            case 29:
                return R.drawable.partly_cloudy_night;
            case 30:
                return R.drawable.partly_cloudy_day;
            case 20:case 22:
                return R.drawable.foggy;
            case 3:case 4:case 37:case 38:
                return R.drawable.thunderstorms;
            case 36:
                return R.drawable.hot;
            default:
                Log.i(LogMessage, "Unknown weather code: " + code);
                return R.drawable.ic_launcher;
        }
    }

    public static int toCelsius(int fahrTemp) {
        double dCelsTemp = (fahrTemp - 32.0) / 1.8;
        return (int) dCelsTemp;
    }
}
