package weathertogo.sergeybudkov.ru.weathertogo;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ser on 21.01.2015.
 */
public class ShowBuilder {


    public static String[] finalWeather = new String[10];
    int inFinalWeather = 0;
    private static Context context;
    public static WeatherDataBase wBase;
    public ArrayList<Integer> addedCitiesID;
    public ShowBuilder() {
         addedCitiesID= new ArrayList<Integer>();
         wBase= MyContentProvider.database;
        preCreate();
    }
    public void composeNextDays(int index, String mainCity) {
        String tmp = "";
        boolean isEmpty = true;
        Cursor cursor = wBase.sqLiteDatabase.query(WeatherDataBase.TABLE_WEATHER_NAME, new String[]{
                        WeatherDataBase.ID_WEATHER, WeatherDataBase.CITY, WeatherDataBase.COUNTRY, WeatherDataBase.YANDEX_ID,
                        WeatherDataBase.TEMPERATURE, WeatherDataBase.DESCRIPTION, WeatherDataBase.PRESSURE, WeatherDataBase.WIND_DIRECTION,
                        WeatherDataBase.WIND_SPEED, WeatherDataBase.HUMIDITY, WeatherDataBase.DAY_PART, WeatherDataBase.WEATHER_NOW, WeatherDataBase.DATA},
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));
            if (weather_now_from_table.equals(MainActivity.YES)) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty == true) return;

        cursor.moveToLast();
        while (cursor.moveToPrevious()) {
            String day_part_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART));
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));

            if (day_part_from_table.equals(MainActivity.NOW)) {
                if (mainCity.equals(MainActivity.LAST)) {
                    if (weather_now_from_table.equals(MainActivity.YES))
                        break;
                } else {
                    if (city_from_table.equals(mainCity))
                        break;
                }
            }
        }
        int stop = 1;
        while (cursor.moveToNext()) {
            String day_part_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART));
            if (day_part_from_table.equals("morning")) {
                ++stop;
                if (stop == index) break;
            }
        }

        if (index == 3)
            tmp += "ПОГОДА НА ЗАВТРА \n \n";
        if (index == 4)
            tmp += "ПОГОДА НА ПОСЛЕЗАВТРА \n \n";
        if (index == 5)
            tmp += "ПОГОДА ЧЕРЕЗ 2 ДНЯ \n \n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)) + "\n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY)) + "\n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART)) + "\n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.TEMPERATURE)) + "°C \n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.DESCRIPTION)) + "\n";
        tmp += "Давление: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.PRESSURE)) + "mm \n";
        tmp += "Влажность: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.HUMIDITY)) + "% \n";
        tmp += "Направление ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_DIRECTION)) + "\n";
        tmp += "Скорость ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_SPEED)) + "m/s \n";

        finalWeather[inFinalWeather] = tmp;
        inFinalWeather++;
        tmp = "";

        cursor.moveToNext();
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART)) + "\n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.TEMPERATURE)) + "°C \n";
        tmp += "Давление: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.PRESSURE)) + "mm \n";
        tmp += "Влажность: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.HUMIDITY)) + "% \n";
        tmp += "Направление ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_DIRECTION)) + "\n";
        tmp += "Скорость ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_SPEED)) + "m/s\n";

        finalWeather[inFinalWeather] = tmp;
        inFinalWeather++;
        tmp = "";

        cursor.moveToNext();
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART)) + "\n";
        tmp += cursor.getString(cursor.getColumnIndex(WeatherDataBase.TEMPERATURE)) + "°C\n";
        tmp += "Давление: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.PRESSURE)) + "mm\n";
        tmp += "Влажность: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.HUMIDITY)) + "%\n";
        tmp += "Направление ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_DIRECTION)) + "\n";
        tmp += "Скорость ветра: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_SPEED)) + "m/s\n";

        finalWeather[inFinalWeather] = tmp;
        inFinalWeather++;
    }
    public void composeToday(String mainCity) {
        changeFlags(mainCity);
        boolean isEmpty = true;
        Cursor cursor = wBase.sqLiteDatabase.query(WeatherDataBase.TABLE_WEATHER_NAME, new String[] {
                        WeatherDataBase.ID_WEATHER, WeatherDataBase.CITY, WeatherDataBase.COUNTRY, WeatherDataBase.YANDEX_ID,
                        WeatherDataBase.TEMPERATURE, WeatherDataBase.DESCRIPTION, WeatherDataBase.PRESSURE, WeatherDataBase.WIND_DIRECTION,
                        WeatherDataBase.WIND_SPEED, WeatherDataBase.HUMIDITY, WeatherDataBase.DAY_PART, WeatherDataBase.WEATHER_NOW, WeatherDataBase.DATA},
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));

            if (weather_now_from_table.equals(MainActivity.YES)) {
                isEmpty = false;
                MainActivity.CITY_YES = city_from_table;
                break;
            }
        }
        if (isEmpty == true) return;

        cursor.moveToLast();
        while (cursor.moveToPrevious()) {
            String day_part_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART));
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));

            if (day_part_from_table.equals(MainActivity.NOW)) {
                if (mainCity.equals(MainActivity.LAST)) {
                    if (weather_now_from_table.equals(MainActivity.YES))
                        break;
                } else {
                    if (city_from_table.equals(mainCity))
                        break;
                }
            }
        }
        String tmp = "";

        tmp+="ПОГОДА СЕЙЧАС \n \n";
        tmp+=cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY)) + "\n";
        tmp+=cursor.getString(cursor.getColumnIndex(WeatherDataBase.TEMPERATURE))+"°C \n";
        tmp+=cursor.getString(cursor.getColumnIndex(WeatherDataBase.DESCRIPTION))+"\n";
        tmp+="Давление: "+cursor.getString(cursor.getColumnIndex(WeatherDataBase.PRESSURE))+"mm \n";
        tmp+="Влажность: "+ cursor.getString(cursor.getColumnIndex(WeatherDataBase.HUMIDITY))+"%\n";
        tmp+="Направление ветра: "+ cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_DIRECTION))+"\n";
        tmp+="Скорость ветра: "+cursor.getString(cursor.getColumnIndex(WeatherDataBase.WIND_SPEED))+"m/s\n";
        tmp+="Последнее обновление: " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)).substring(0, cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)).indexOf("T")) +
                " " + cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)).substring(cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)).indexOf("T") + 1, cursor.getString(cursor.getColumnIndex(WeatherDataBase.DATA)).length())+"\n";
        finalWeather[0] = tmp;
    }

    private void changeFlags(String city) {
        if (city.equals(MainActivity.LAST)) return;
        Cursor cursor = wBase.sqLiteDatabase.query(WeatherDataBase.TABLE_WEATHER_NAME, new String[] {
                        WeatherDataBase.ID_WEATHER, WeatherDataBase.CITY, WeatherDataBase.COUNTRY, WeatherDataBase.YANDEX_ID,
                        WeatherDataBase.TEMPERATURE, WeatherDataBase.DESCRIPTION, WeatherDataBase.PRESSURE, WeatherDataBase.WIND_DIRECTION,
                        WeatherDataBase.WIND_SPEED, WeatherDataBase.HUMIDITY, WeatherDataBase.DAY_PART, WeatherDataBase.WEATHER_NOW, WeatherDataBase.DATA},
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String day_part_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART));

            if (weather_now_from_table.equals(MainActivity.YES)) {
                wBase.changeYesOrNo(cursor.getString(cursor.getColumnIndex(WeatherDataBase.ID_WEATHER)), MainActivity.NO);
            }
            if (city_from_table.equals(city) == true && day_part_from_table.equals(MainActivity.NOW) == true) {
                wBase.changeYesOrNo(cursor.getString(cursor.getColumnIndex(WeatherDataBase.ID_WEATHER)), MainActivity.YES);
            }
        }
    }

    public ArrayList<Integer> takeBig(){
        return addedCitiesID;
    }

    public void preCreate(){
        finalWeather[0] = "Что-то пошло не так.. Пожалуйста выберете город еще раз. Мне действительно очень жаль :(";
        for(int i = 1 ; i < 10;i++){
            finalWeather[i] = ""+i;
        }
    }

    public String[] compose() {
        String[] f = new String[16];
        f[0] = "----------------------------------";
        f[1] = finalWeather[0];
        f[2] = "----------------------------------";
        for (int i = 0; i < 3; i++) {
            f[i + 3] = finalWeather[i + 1];
        }
        f[6] = "----------------------------------";
        for (int i = 0; i < 3; i++) {
            f[i + 7] = finalWeather[i + 4];
        }
        f[10] = "----------------------------------";
        for (int i = 0; i < 3; i++) {
            f[i + 11] = finalWeather[i + 7];
        }
        f[14] = "----------------------------------";
        f[15] = "Created by Sergey Budkov 2536 \n >----(^_^)----<";
        return f;
    }


    public ArrayList<HashMap<String, String>> takeSmall() {

        ArrayList<HashMap<String, String>> addedCities = new ArrayList<HashMap<String, String>>();
        Cursor cursor = wBase.sqLiteDatabase.query(WeatherDataBase.TABLE_WEATHER_NAME, new String[]{
                        WeatherDataBase.ID_WEATHER, WeatherDataBase.CITY, WeatherDataBase.COUNTRY, WeatherDataBase.YANDEX_ID,
                        WeatherDataBase.TEMPERATURE, WeatherDataBase.DESCRIPTION, WeatherDataBase.PRESSURE, WeatherDataBase.WIND_DIRECTION,
                        WeatherDataBase.WIND_SPEED, WeatherDataBase.HUMIDITY, WeatherDataBase.DAY_PART, WeatherDataBase.WEATHER_NOW, WeatherDataBase.DATA},
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String country_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.COUNTRY));
            int id_from_table = Integer.parseInt(cursor.getString(cursor.getColumnIndex(WeatherDataBase.YANDEX_ID)));
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(MainActivity.CITY, city_from_table);
            map.put(MainActivity.COUNTRY, country_from_table);
            if (addedCities.contains(map) == false) {
                addedCities.add(map);
                addedCitiesID.add(id_from_table);
            }
        }

        return addedCities;

    }



}
