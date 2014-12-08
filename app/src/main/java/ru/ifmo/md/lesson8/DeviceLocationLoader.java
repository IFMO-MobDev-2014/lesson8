package ru.ifmo.md.lesson8;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created by dimatomp on 08.12.14.
 */
public class DeviceLocationLoader extends AsyncTaskLoader<String> {
    public DeviceLocationLoader(Context context) {
        super(context);
    }

    @Override
    public String loadInBackground() {
        LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location current = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getContext().getResources().openRawResource(R.raw.city_list_sorted)));
            reader.readLine();
            double lat = current.getLatitude(), lon = current.getLongitude(), cDistance = Double.POSITIVE_INFINITY;
            String currentName = null;
            String line;
            while ((line = reader.readLine()) != null) {
                String cityName;
                double cLat, cLon;
                try {
                    StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                    cityName = tokenizer.nextToken();
                    cLat = Double.parseDouble(tokenizer.nextToken()) - lat;
                    cLon = Double.parseDouble(tokenizer.nextToken()) - lon;
                } catch (RuntimeException parseError) {
                    continue;
                }
                if (-cLat > cDistance)
                    break;
                if (cDistance > Math.sqrt(cLat * cLat + cLon * cLon)) {
                    cDistance = Math.sqrt(cLat * cLat + cLon * cLon);
                    currentName = cityName;
                }
            }
            return currentName;
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read city list", e);
        }
    }
}
