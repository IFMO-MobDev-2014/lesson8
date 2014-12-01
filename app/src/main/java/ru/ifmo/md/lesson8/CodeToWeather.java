package ru.ifmo.md.lesson8;

public class CodeToWeather {
    private static final String[] weatherTypes = new String[]{"Умеренный или сильный снегопад с громом",
            "Местами небольшой снегопад с громом",
            "Умеренный или сильный дождь с громом",
            "Местами небольшой дождь с громом",
            "Умеренный или сильный град",
            "Легкий град",
            "Умеренные или сильный снегопад",
            "Небольшой снегопад",
            "Умеренный или сильный снегопад с дождем",
            "Легкий снегопад с дождем",
            "Проливной дождь",
            "Умеренный или сильный дождь",
            "Легкий дождь",
            "Град",
            "Сильный снегопад",
            "Местами сильный снегопад",
            "Умеренный снегопад",
            "Местами умеренный снегопад",
            "Небольшой снегопад",
            "Местами небольшой снегопад",
            "Умеренный или сильный дождь со снегом",
            "Легкий снег с дождем",
            "Облачно, дождь со снегом",
            "Умеренный или сильный дождь с градом",
            "Сильный дождь",
            "Временами сильный дождь",
            "Умеренный дождь",
            "Местами умеренный дождь",
            "Небольшой дождь",
            "Местами небольшой дождь",
            "Сильный снег с дождем",
            "Снег с дождем",
            "Легкий дождь",
            "Местами моросящий дождь",
            "Холодный туман",
            "Густой туман",
            "Метель с сильным снегопадом",
            "Метель с небольшим снегопадом",
            "Гроза неподалеку",
            "Изморозь",
            "Местами снег с дождем",
            "Местами снегопад",
            "Местами легкий дождь",
            "Легкий туман",
            "Ясно",
            "Облачно",
            "Небольшая облачность",
            "Солнечно"};
    private static final String[] codes = new String[]{"395", "392", "389", "386", "377", "374", "371", "368", "365", "362", "359", "356",
            "353", "350", "338", "335", "332", "329", "326", "323", "320", "317", "314", "311", "308", "305", "302", "299",
            "296", "293", "284", "281", "266", "263", "260", "248", "230", "227", "200", "185", "182", "179", "176", "143", "122",
            "119", "116", "113"};
    private static final int[] images = new int[]{R.drawable.snow_thunder_sun, R.drawable.snow_thunder_sun, R.drawable.rain_thunder,
            R.drawable.rain_thunder, R.drawable.ice, R.drawable.ice_snow,
            R.drawable.snow, R.drawable.snow, R.drawable.rain_snow, R.drawable.rain_snow, R.drawable.heavy_rain, R.drawable.rain,
            R.drawable.rain_sun, R.drawable.ice, R.drawable.heavysnow,
            R.drawable.heavysnow, R.drawable.snow, R.drawable.snow, R.drawable.snow_sun, R.drawable.snow_sun, R.drawable.rain_snow,
            R.drawable.rain_snow, R.drawable.rain_snow, R.drawable.ice_snow, R.drawable.heavy_rain,
            R.drawable.heavy_rain, R.drawable.rain,
            R.drawable.rain, R.drawable.rain, R.drawable.rain, R.drawable.heavysnow,
            R.drawable.heavysnow, R.drawable.rain_sun, R.drawable.rain_sun, R.drawable.foggy, R.drawable.foggy, R.drawable.heavysnow,
            R.drawable.snow, R.drawable.rain_thunder_sun, R.drawable.cold, R.drawable.rain_snow, R.drawable.snow,
            R.drawable.rain, R.drawable.foggy, R.drawable.sunny, R.drawable.overcast, R.drawable.cloudy, R.drawable.sunny};

    public static String getWeatherType(String code) {
        for (int i = 0; i < codes.length; i++)
            if (codes[i].equals(code)) {
                return weatherTypes[i];
            }
        return "O_O";
    }

    public static int getWeatherImage(String code) {
        for (int i = 0; i < codes.length; i++)
            if (codes[i].equals(code)) {
                return images[i];
            }
        return R.drawable.sunny;
    }
}
