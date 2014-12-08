package ru.ifmo.md.lesson8.net;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.util.List;

import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.weather.Forecast;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class WeatherDownloadService extends IntentService {

    public WeatherDownloadService() {
        super("ru.ifmo.md.net.WeatherDownload.Service");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, WeatherDownloadService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentHelper contentHelper = new ContentHelper(getContentResolver());
        for (Place place : contentHelper.getPlaces()) {
            List<Forecast> forecasts = null;
            try {
                forecasts = YahooQuery.fetchWeatherInPlace(place);
            } catch (IOException ignore) {

            }
            contentHelper.setForecasts(place, forecasts);
        }
    }
}
