package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class CityNameGetService extends IntentService {
    public static final String API_KEY = "af1fd362b895c36e1a37e182defcf246";
    private static Handler handler;
    public static final String LAT_EXTRA = "LAT_EXTRA";
    public static final String LON_EXTRA = "LON_EXTRA";

    public CityNameGetService() {
        super(CityNameGetService.class.getSimpleName());
    }

    public static void setHandler(Handler handler) {
        CityNameGetService.handler = handler;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double lat = intent.getDoubleExtra(LAT_EXTRA, 0);
        double lon = intent.getDoubleExtra(LON_EXTRA, 0);
        try {
            City city = FetchWeatherTask.fetchCity(lat, lon, API_KEY);
            Message message = handler.obtainMessage();
            message.obj = city;
            message.sendToTarget();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}