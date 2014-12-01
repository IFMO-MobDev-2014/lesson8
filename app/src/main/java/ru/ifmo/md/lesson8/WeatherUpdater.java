package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;

public class WeatherUpdater extends IntentService {
    public WeatherUpdater() {
        super("WeatherUpdater");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentResolver resolver = getContentResolver();
        if (intent != null)
            if (intent.getData().getLastPathSegment().equals("city")) {
                resolver.delete(intent.getData(), null, null);
            } else {
                resolver.update(intent.getData(), null, null, null);
                resolver.notifyChange(intent.getData(), null, false);
            }
    }
}
