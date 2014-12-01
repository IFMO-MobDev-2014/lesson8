package ru.ifmo.md.lesson8;

/**
 * Created by kna on 01.12.2014.
 */
public class WeatherDataUtils {
    static int getIconForCode(int code, int cloudiness, boolean night) {
        switch(code) {
            case 200: // 	thunderstorm with light rain 	[[file:11d.png]]
            case 201: // 	thunderstorm with rain 	[[file:11d.png]]
            case 202: // 	thunderstorm with heavy rain 	[[file:11d.png]]
            case 210: // 	light thunderstorm 	[[file:11d.png]]
            case 211: // 	thunderstorm 	[[file:11d.png]]
            case 212: // 	heavy thunderstorm 	[[file:11d.png]]
            case 221: // 	ragged thunderstorm 	[[file:11d.png]]
            case 230: // 	thunderstorm with light drizzle 	[[file:11d.png]]
            case 231: // 	thunderstorm with drizzle 	[[file:11d.png]]
            case 232: // 	thunderstorm with heavy drizzle 	[[file:11d.png]]
                return cloudiness < 50 && !night ? R.drawable.sunny_thunderstorm : R.drawable.thunderstorm;

            case 300: // 	light intensity drizzle 	[[file:09d.png]]
            case 301: // 	drizzle 	[[file:09d.png]]
            case 302: // 	heavy intensity drizzle 	[[file:09d.png]]
            case 310: // 	light intensity drizzle rain 	[[file:09d.png]]
            case 311: // 	drizzle rain 	[[file:09d.png]]
            case 312: // 	heavy intensity drizzle rain 	[[file:09d.png]]
            case 313: // 	shower rain and drizzle 	[[file:09d.png]]
            case 314: // 	heavy shower rain and drizzle 	[[file:09d.png]]
            case 321: // 	shower drizzle 	[[file:09d.png]]
                if(cloudiness < 50) {
                    return night ? R.drawable.moon_rain : R.drawable.sunny_rain;
                } else {
                    return R.drawable.rain;
                }
            case 500: // 	light rain 	[[file:10d.png]]
            case 501: // 	moderate rain 	[[file:10d.png]]
            case 502: // 	heavy intensity rain 	[[file:10d.png]]
            case 503: // 	very heavy rain 	[[file:10d.png]]
            case 504: // 	extreme rain 	[[file:10d.png]]
            case 511: // 	freezing rain 	[[file:13d.png]]
            case 520: // 	light intensity shower rain 	[[file:09d.png]]
            case 521: // 	shower rain 	[[file:09d.png]]
            case 522: // 	heavy intensity shower rain 	[[file:09d.png]]
            case 531: // 	ragged shower rain 	[[file:09d.png]]
                return R.drawable.rain_rain;
            case 600: // 	light snow 	[[file:13d.png]]
            case 601: // 	snow 	[[file:13d.png]]
            case 602: // 	heavy snow 	[[file:13d.png]]
                if(cloudiness < 50) {
                    return night ? R.drawable.moon_snow : R.drawable.sunny_snow;
                } else {
                    return cloudiness > 75 ? R.drawable.snow_cloud : R.drawable.snow;
                }
            case 611: // 	sleet 	[[file:13d.png]]
            case 612: // 	shower sleet 	[[file:13d.png]]
            case 615: // 	light rain and snow 	[[file:13d.png]]
            case 616: // 	rain and snow 	[[file:13d.png]]
            case 620: // 	light shower snow 	[[file:13d.png]]
            case 621: // 	shower snow 	[[file:13d.png]]
            case 622: // 	heavy shower snow 	[[file:13d.png]]
                return R.drawable.rain_snow;
            case 701: // 	mist 	[[file:50d.png]]
            case 711: // 	smoke 	[[file:50d.png]]
            case 721: // 	haze 	[[file:50d.png]]
            case 731: // 	sand, dust whirls 	[[file:50d.png]]
            case 741: // 	fog 	[[file:50d.png]]
            case 751: // 	sand 	[[file:50d.png]]
            case 761: // 	dust 	[[file:50d.png]]
            case 762: // 	volcanic ash 	[[file:50d.png]]
            case 771: // 	squalls 	[[file:50d.png]]
            case 781: // 	tornado 	[[file:50d.png]]
                return R.drawable.mist; // well, we don't have icons that actually make sense, do we? and OWM has same icons for the last two as for the previous all. weird.
            case 800: // 	clear sky 	[[file:01d.png]] [[file:01n.png]]
                return night ? R.drawable.moon : R.drawable.sunny;
            case 801: // 	few clouds 	[[file:02d.png]] [[file:02n.png]]
            case 802: // 	scattered clouds 	[[file:03d.png]] [[file:03d.png]]
                return night ? R.drawable.moon_cloud : R.drawable.sunny_cloud;
            case 803: // 	broken clouds 	[[file:04d.png]] [[file:03d.png]]
                return R.drawable.cloud;
            case 804: // 	overcast clouds 	[[file:04d.png]] [[file:04d.png]]
                return R.drawable.cloud_cloud;
            case 900: // 	tornado
            case 901: // 	tropical storm
            case 902: // 	hurricane
            case 903: // 	cold
            case 904: // 	hot
            case 905: // 	windy
            case 906: // 	hail
            case 951: // 	calm
            case 952: // 	light breeze
            case 953: // 	gentle breeze
            case 954: // 	moderate breeze
            case 955: // 	fresh breeze
            case 956: // 	strong breeze
            case 957: // 	high wind, near gale
            case 958: // 	gale
            case 959: // 	severe gale
            case 960: // 	storm
            case 961: // 	violent storm
            case 962: // 	hurricane
                return R.drawable.mist; // have tornado here and tornado in "mist" category, totally makes sense
        }
        return R.drawable.sunny;
    }

    static String getDescriptionForCode(int code) { // this is currently anything but internationalization-friendly. as if I care.
        switch(code) {
            case 200: // 	thunderstorm with light rain 	[[file:11d.png]]
            case 201: // 	thunderstorm with rain 	[[file:11d.png]]
            case 202: // 	thunderstorm with heavy rain 	[[file:11d.png]]
            case 210: // 	light thunderstorm 	[[file:11d.png]]
            case 211: // 	thunderstorm 	[[file:11d.png]]
            case 212: // 	heavy thunderstorm 	[[file:11d.png]]
            case 221: // 	ragged thunderstorm 	[[file:11d.png]]
            case 230: // 	thunderstorm with light drizzle 	[[file:11d.png]]
            case 231: // 	thunderstorm with drizzle 	[[file:11d.png]]
            case 232: // 	thunderstorm with heavy drizzle 	[[file:11d.png]]
                return "Thunderstorm";
            case 300: // 	light intensity drizzle 	[[file:09d.png]]
            case 301: // 	drizzle 	[[file:09d.png]]
            case 302: // 	heavy intensity drizzle 	[[file:09d.png]]
            case 310: // 	light intensity drizzle rain 	[[file:09d.png]]
            case 311: // 	drizzle rain 	[[file:09d.png]]
            case 312: // 	heavy intensity drizzle rain 	[[file:09d.png]]
            case 313: // 	shower rain and drizzle 	[[file:09d.png]]
            case 314: // 	heavy shower rain and drizzle 	[[file:09d.png]]
            case 321: // 	shower drizzle 	[[file:09d.png]]
                return "Drizzle"; // wtf is a drizzle?
            case 500: // 	light rain 	[[file:10d.png]]
            case 501: // 	moderate rain 	[[file:10d.png]]
            case 502: // 	heavy intensity rain 	[[file:10d.png]]
            case 503: // 	very heavy rain 	[[file:10d.png]]
            case 504: // 	extreme rain 	[[file:10d.png]]
            case 511: // 	freezing rain 	[[file:13d.png]]
            case 520: // 	light intensity shower rain 	[[file:09d.png]]
            case 521: // 	shower rain 	[[file:09d.png]]
            case 522: // 	heavy intensity shower rain 	[[file:09d.png]]
            case 531: // 	ragged shower rain 	[[file:09d.png]]
                return "Rain";
            case 600: // 	light snow 	[[file:13d.png]]
            case 601: // 	snow 	[[file:13d.png]]
            case 602: // 	heavy snow 	[[file:13d.png]]
                return "Snow";
            case 611: // 	sleet 	[[file:13d.png]]
            case 612: // 	shower sleet 	[[file:13d.png]]
            case 615: // 	light rain and snow 	[[file:13d.png]]
            case 616: // 	rain and snow 	[[file:13d.png]]
            case 620: // 	light shower snow 	[[file:13d.png]]
            case 621: // 	shower snow 	[[file:13d.png]]
            case 622: // 	heavy shower snow 	[[file:13d.png]]
                return "Sleet";
            case 701: // 	mist 	[[file:50d.png]]
            case 711: // 	smoke 	[[file:50d.png]]
            case 721: // 	haze 	[[file:50d.png]]
            case 731: // 	sand, dust whirls 	[[file:50d.png]]
            case 741: // 	fog 	[[file:50d.png]]
            case 751: // 	sand 	[[file:50d.png]]
            case 761: // 	dust 	[[file:50d.png]]
            case 762: // 	volcanic ash 	[[file:50d.png]]
            case 771: // 	squalls 	[[file:50d.png]]
            case 781: // 	tornado 	[[file:50d.png]]
                return "Fog"; // most generic one
            case 800: // 	clear sky 	[[file:01d.png]] [[file:01n.png]]
                return "Clear";
            case 801: // 	few clouds 	[[file:02d.png]] [[file:02n.png]]
            case 802: // 	scattered clouds 	[[file:03d.png]] [[file:03d.png]]
                return "Few Clouds";
            case 803: // 	broken clouds 	[[file:04d.png]] [[file:03d.png]]
                return "Broken Clouds";
            case 804: // 	overcast clouds 	[[file:04d.png]] [[file:04d.png]]
                return "Overcast";
            case 900: // 	tornado
            case 901: // 	tropical storm
            case 902: // 	hurricane
            case 903: // 	cold
            case 904: // 	hot
            case 905: // 	windy
            case 906: // 	hail
            case 951: // 	calm
            case 952: // 	light breeze
            case 953: // 	gentle breeze
            case 954: // 	moderate breeze
            case 955: // 	fresh breeze
            case 956: // 	strong breeze
            case 957: // 	high wind, near gale
            case 958: // 	gale
            case 959: // 	severe gale
            case 960: // 	storm
            case 961: // 	violent storm
            case 962: // 	hurricane
                return "Disaster"; // can be more specific, but this shouldn't happen (generally)
        }
        return "Clear";
    }

    private static String[] windDirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    static String getWindDirByDegs(int degs) {
        float degsF = (degs + 22.5f) / 45.0f;
        return windDirs[((int) degsF) % windDirs.length];
    }
}
