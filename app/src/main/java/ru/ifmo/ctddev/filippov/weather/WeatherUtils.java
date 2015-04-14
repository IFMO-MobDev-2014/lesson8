package ru.ifmo.ctddev.filippov.weather;

/**
 * Created by Dima_2 on 02.04.2015.
 */
public class WeatherUtils {
    private static String[] windDirections = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    static int getIconForCode(int code, int cloudiness, boolean night) {
        if (200 <= code && code <= 250) {
            return cloudiness < 50 && !night ? R.drawable.sunny_thunderstorm : R.drawable.thunderstorm;
        }
        if (300 <= code && code <= 350) {
            if (cloudiness < 50) {
                return night ? R.drawable.moon_rain : R.drawable.sunny_rain;
            } else {
                return R.drawable.rain;
            }
        }
        if (500 <= code && code <= 550) {
            return R.drawable.heavy_rain;
        }
        if (600 <= code && code <= 602) {
            if (cloudiness < 50) {
                return night ? R.drawable.moon_snow : R.drawable.sunny_snow;
            } else {
                return cloudiness > 75 ? R.drawable.cloud_snow : R.drawable.snow;
            }
        }
        if (610 <= code && code <= 650) {
            return R.drawable.rain_snow;
        }
        if (700 <= code && code < 800) {
            return R.drawable.fog;
        }
        if (code == 800) {
            return night ? R.drawable.moon : R.drawable.sunny;
        }
        if (801 <= code && code <= 802) {
            return night ? R.drawable.moon_cloud : R.drawable.sunny_cloud;
        }
        if (code == 803) {
            return R.drawable.cloud;
        }
        if (code == 804) {
            return R.drawable.many_clouds;
        }
        if (900 <= code && code < 1000) {
            return R.drawable.fog;
        }
        return R.drawable.sunny;
    }

    static String getDescriptionForCode(int code) {
        if (200 <= code && code <= 250) {
            return "Thunderstorm";
        }
        if (300 <= code && code <= 350) {
            return "Drizzle";
        }
        if (500 <= code && code <= 550) {
            return "Rain";
        }
        if (600 <= code && code <= 602) {
            return "Snow";
        }
        if (610 <= code && code <= 650) {
            return "Sleet";
        }
        if (700 <= code && code < 800) {
            return "Fog";
        }
        if (code == 800) {
            return "Clear";
        }
        if (801 <= code && code <= 802) {
            return "Few clouds";
        }
        if (code == 803) {
            return "Clouds";
        }
        if (code == 804) {
            return "Many clouds";
        }
        if (900 <= code && code < 1000) {
            return "Disaster";
        }
        return "Sunny";
    }

    static String getWindDirection(int degrees) {
        int index = (int) ((degrees + 22.5f) / 45.0f) % windDirections.length;
        return windDirections[index];
    }
}
