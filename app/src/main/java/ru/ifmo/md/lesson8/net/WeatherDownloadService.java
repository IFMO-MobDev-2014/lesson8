package ru.ifmo.md.lesson8.net;

import android.app.IntentService;
import android.content.Intent;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentHelper contentHelper = new ContentHelper(getContentResolver());
        for (Place place : contentHelper.getPlaces()) {
            List<Forecast> forecasts = YahooQuery.fetchWeatherInPlace(place);
            contentHelper.setForecasts(place, forecasts);
        }
    }
}
