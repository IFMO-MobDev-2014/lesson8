package com.alex700.AWeather;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Алексей on 11.12.2014.
 */
public class CityGetNameService extends IntentService {
    public static final String SERVICE_NAME = CityGetNameService.class.getSimpleName();
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final int OK = 0;
    public static final int ERROR = -1;
    public CityGetNameService() {
        super(SERVICE_NAME);
    }
    private static Handler handler;

    public static void setHandler(Handler handler) {
        CityGetNameService.handler = handler;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double lat = intent.getDoubleExtra(LATITUDE, 0);
        double lon = intent.getDoubleExtra(LONGITUDE, 0);
        Log.d("CityGetNameService", "City service start");
        try {
            String name = WeatherFetcher.fetchCity(lat, lon, MainActivity.API_KEY);
            if (handler != null) {
                handler.obtainMessage(OK, name).sendToTarget();
            }
        } catch (Exception e) {
            if (handler != null) {
                handler.obtainMessage(ERROR).sendToTarget();
            }
        }
    }
}