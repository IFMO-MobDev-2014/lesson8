package weathertogo.sergeybudkov.ru.weathertogo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


//Sergey Budkov 2536

public class MainActivity extends Activity {
    private LocationManager locationManager;
    SharedPreferences sPref;
    TextView loc;
    String prevLocatedCity = "";
    public static String NOW = "NOW", LAST = "LAST", INTENT = "key_intent", CITY = "city", COUNTRY = "country", NO = "NO", YES = "YES", YANDEX_ID = "yandex_id", CITY_YES = "";
    public String[] finalWeather = new String[10];
    int inFinalWeather = 0;
    public static WeatherDataBase wBase;
    private static final String SAVEDPREVCITY = "savedprevcity";
    ArrayList<Integer> allCitiesID = new ArrayList<Integer>();
    ArrayList<HashMap<String, String>> allCities = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> addedCities = new ArrayList<HashMap<String, String>>();
    public static ArrayList<Integer> addedCitiesID = new ArrayList<Integer>();
    ArrayList<HashMap<String, String>> currentCities = new ArrayList<HashMap<String, String>>();
    ArrayList<Integer> currentCitiesID = new ArrayList<Integer>();
    ListView currentCitiesListView,addedCitiesListView;
    List<View> screens;
    ViewPager viewScreen;
    ProgressDialog dialog;
    MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    boolean isConnect = false, isDialog = false, isFirst = false,needLoc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        needLoc = true;
        preCreate();
        tutorialShow();
        wBase = new WeatherDataBase(this);
        wBase.open();
        buildingViewPages();
        loc = (TextView) screens.get(1).findViewById(R.id.textView);
        currentCitiesListView = (ListView) screens.get(0).findViewById(R.id.all_cities);
        addedCitiesListView = (ListView) screens.get(1).findViewById(R.id.added_cities);
        openFileAndPrintAllCities();
        printCityFromTable();
        inFinalWeather = 1;
        composeToday(LAST);
        composeNextDays(3, LAST);
        composeNextDays(4, LAST);
        composeNextDays(5, LAST);
        showWeather();

        if (addedCities.isEmpty() == true) {
            isFirst = true;
        } else {
            viewScreen.setCurrentItem(2);
        }
        addedCitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long number) {
                composeToday(addedCities.get(index).get(CITY));
                inFinalWeather=1;
                composeNextDays(3, addedCities.get(index).get(CITY));
                composeNextDays(4, addedCities.get(index).get(CITY));
                composeNextDays(5, addedCities.get(index).get(CITY));
                showWeather();
                viewScreen.setCurrentItem(2);
            }
        });
        registerForContextMenu(addedCitiesListView);

        IntentFilter intentFilter = new IntentFilter(WeatherIntent.ACTION_MyIntentService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        currentCitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long number) {

                wBase.deleteWeatherInCity(currentCities.get(index).get(CITY), currentCities.get(index).get(COUNTRY));

                isConnect = true;
                isDialog = true;
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("Загружаем погоду..");
                dialog.setMessage("┏(-_-)┛┗(-_-﻿ )┓┗(-_-)┛┏(-_-)┓");
                dialog.show();
                String urlAdress = "http://export.yandex.ru/weather-ng/forecasts/" + currentCitiesID.get(index).toString() + ".xml";
                Intent intentMyIntentService = new Intent(MainActivity.this, WeatherIntent.class);
                intentMyIntentService.putExtra(INTENT, urlAdress);
                intentMyIntentService.putExtra(CITY, currentCities.get(index).get(CITY));
                intentMyIntentService.putExtra(COUNTRY, currentCities.get(index).get(COUNTRY));
                intentMyIntentService.putExtra(YANDEX_ID, currentCitiesID.get(index).toString());
                startService(intentMyIntentService);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                viewScreen.setCurrentItem(1);
                showWeather();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                printWithSuchPrefix(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        EditText editText = (EditText) screens.get(0).findViewById(R.id.enter_city);
        editText.addTextChangedListener(textWatcher);

        Intent intent = new Intent(MainActivity.this, WeatherUpdate.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 60 * 1000,   60 * 60 * 1000, pendingIntent);
        Log.d("Alaram", "" + System.currentTimeMillis());
    }



    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
    }
    //LOCATION
    private LocationListener locationListener = new LocationListener() {


        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                if(location != null) {
                    //loc.setText("" + location.getLatitude());
                    if(needLoc) {
                        locatiFind(location);
                        needLoc = false;
                    }
                }

            } catch (IOException e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Включите службы геолокации.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    };





    int pozition = -1;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        pozition = aMenuInfo.position;
        menu.add(Menu.NONE, 1, Menu.NONE, "Удалить");
        menu.add(Menu.NONE, 2, Menu.NONE, "Обновить погоду");
        menu.add(Menu.NONE, 3, Menu.NONE, "Посмотреть на кошечку");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case 1:

                wBase.deleteWeatherInCity(addedCities.get(pozition).get(CITY), addedCities.get(pozition).get(COUNTRY));
                printCityFromTable();
                break;
            case 2:
                String city_from_table = addedCities.get(pozition).get(CITY);
                String country_from_table = addedCities.get(pozition).get(COUNTRY);
                String yandex_id_from_table = addedCitiesID.get(pozition).toString();
                wBase.deleteWeatherInCity(addedCities.get(pozition).get(CITY), addedCities.get(pozition).get(COUNTRY));

                IntentFilter intentFilter = new IntentFilter(WeatherIntent.ACTION_MyIntentService);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(myBroadcastReceiver, intentFilter);

                isConnect = true;
                isDialog = true;
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("Обновляем погоду..");
                dialog.setMessage("┏(-_-)┛┗(-_-﻿ )┓┗(-_-)┛┏(-_-)┓");
                dialog.show();
                String urlAdress = "http://export.yandex.ru/weather-ng/forecasts/" + yandex_id_from_table + ".xml";
                Intent intentMyIntentService = new Intent(MainActivity.this, WeatherIntent.class);
                intentMyIntentService.putExtra(INTENT, urlAdress);
                intentMyIntentService.putExtra(CITY, city_from_table);
                intentMyIntentService.putExtra(COUNTRY, country_from_table);
                intentMyIntentService.putExtra(YANDEX_ID, yandex_id_from_table);
                startService(intentMyIntentService);
                flagForContext = true;
                showWeather();
                break;

            case 3:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Расслабьтесь..")
                        .setMessage("./\\…/\\\n" +
                                "(.‘•..•’.)\n" +
                                "..=*=..\n" +
                                "(.\\.||./.)~~**")
                        .setCancelable(false)
                        .setNegativeButton("Спасибо, мне лучше..",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }


    public void tutorialShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Как пользоваться")
                .setMessage("<-:-> Используйте свайпы вправо и влево для перемещения между экранами. \n \n(*) Долгое нажатие на объект открывает дополнительное меню настроек.\n \n<)) Обязательно включите геолокацию, чтобы не выбирать город вручную. ")
                .setCancelable(false)
                .setNegativeButton("Я все понял!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void locatiFind(Location location) throws IOException {


        Geocoder gcd = new Geocoder(this.getBaseContext(), Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        if (addresses.size() > 0) {
            sPref = getPreferences(MODE_PRIVATE);
            String prevCity = sPref.getString(SAVEDPREVCITY, "");
            final String res = addresses.get(0).getLocality();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("А тем временем мы за вами следим.. Вы здесь?")
                    .setMessage(addresses.get(0).getLocality())
                    .setCancelable(false)
                    .setNegativeButton("Неа",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Да, грузите погоду", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            viewScreen.setCurrentItem(0);
                            EditText et = (EditText) screens.get(0).findViewById(R.id.enter_city);
                            et.setText(res);
                            dialog.cancel();
                        }
                    })
            ;
            AlertDialog alert = builder.create();
            alert.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Вы тута?")
                    .setMessage("К сожалению мы не знаем где вы")
                    .setCancelable(false)
                    .setNegativeButton("ага",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }



    boolean flagForContext = false;
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDialog == false || flagForContext == true) {
                flagForContext = false;
                composeToday(LAST);
                inFinalWeather=1;
                composeNextDays(3, LAST);
                composeNextDays(4, LAST);
                composeNextDays(5, LAST);
                showWeather();
                viewScreen.setCurrentItem(2);
            }
            printCityFromTable();
            if (isDialog == true) dialog.dismiss();
            isDialog = false;
        }
    }

    private void composeNextDays(int index, String mainCity) {
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
            if (weather_now_from_table.equals(YES)) {
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

            if (day_part_from_table.equals(NOW)) {
                if (mainCity.equals(LAST)) {
                    if (weather_now_from_table.equals(YES))
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
    private void composeToday(String mainCity) {
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

            if (weather_now_from_table.equals(YES)) {
                isEmpty = false;
                CITY_YES = city_from_table;
                break;
            }
        }
        if (isEmpty == true) return;

        cursor.moveToLast();
        while (cursor.moveToPrevious()) {
            String day_part_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.DAY_PART));
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String weather_now_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.WEATHER_NOW));

            if (day_part_from_table.equals(NOW)) {
                if (mainCity.equals(LAST)) {
                    if (weather_now_from_table.equals(YES))
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
        if (city.equals(LAST)) return;
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

            if (weather_now_from_table.equals(YES)) {
                wBase.changeYesOrNo(cursor.getString(cursor.getColumnIndex(WeatherDataBase.ID_WEATHER)), NO);
            }
            if (city_from_table.equals(city) == true && day_part_from_table.equals(NOW) == true) {
                wBase.changeYesOrNo(cursor.getString(cursor.getColumnIndex(WeatherDataBase.ID_WEATHER)), YES);
            }
        }
    }



    private void printCityFromTable() {
        addedCitiesID = new ArrayList<Integer>();
        addedCities = new ArrayList<HashMap<String, String>>();
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
            String city_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.CITY));
            String country_from_table = cursor.getString(cursor.getColumnIndex(WeatherDataBase.COUNTRY));
            int id_from_table = Integer.parseInt(cursor.getString(cursor.getColumnIndex(WeatherDataBase.YANDEX_ID)));
            HashMap <String, String> map = new HashMap<String, String>();
            map.put(CITY, city_from_table);
            map.put(COUNTRY, country_from_table);
            if (addedCities.contains(map) == false) {
                addedCities.add(map);
                addedCitiesID.add(id_from_table);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, addedCities, R.layout.row, new String[] {CITY, COUNTRY}, new int[] {R.id.ColCity, R.id.ColCountry});
        addedCitiesListView.setAdapter(adapter);
    }

    private void printWithSuchPrefix(String prefix) {
        prefix = prefix.toLowerCase();
        currentCitiesID = new ArrayList<Integer>();
        currentCities = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < allCities.size(); ++i) {
            if (allCities.get(i).get(CITY).toLowerCase().indexOf(prefix) == 0) {
                currentCities.add(allCities.get(i));
                currentCitiesID.add(allCitiesID.get(i));
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, currentCities, R.layout.row, new String[] {CITY, COUNTRY}, new int[] {R.id.ColCity, R.id.ColCountry});
        currentCitiesListView.setAdapter(adapter);

    }

    private void openFileAndPrintAllCities() {
        try {
            InputStream inputstream = getResources().openRawResource(R.raw.cityresource);
            if (inputstream != null) {
                InputStreamReader isr = new InputStreamReader(inputstream);
                BufferedReader reader = new BufferedReader(isr);
                String str;
                HashMap<String, String> map;
                StringBuffer buffer = new StringBuffer();

                while ((str = reader.readLine()) != null) {
                    buffer.append(str + "\n");
                    map = new HashMap<String, String>();
                    map.put(CITY, str.substring(str.indexOf("&lt;city&gt;") + 12, str.indexOf("&lt;/city&gt;")));
                    map.put(COUNTRY, str.substring(str.indexOf("&lt;country&gt;") + 15, str.indexOf("&lt;/country&gt;")));
                    allCitiesID.add(Integer.parseInt(str.substring(str.indexOf("&lt;id&gt;") + 10, str.indexOf("&lt;/id&gt;"))));
                    currentCitiesID.add(Integer.parseInt(str.substring(str.indexOf("&lt;id&gt;") + 10, str.indexOf("&lt;/id&gt;"))));
                    allCities.add(map);
                    currentCities.add(map);
                }
                SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, allCities, R.layout.row, new String[] {CITY, COUNTRY}, new int[] {R.id.ColCity, R.id.ColCountry});
                currentCitiesListView.setAdapter(adapter);
                inputstream.close();
            }
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void buildingViewPages() {
        LayoutInflater inflater = LayoutInflater.from(this);
        screens = new ArrayList<View>();
        View page = inflater.inflate(R.layout.addnewcityactivity, null);
        screens.add(page);
        page = inflater.inflate(R.layout.main, null);
        screens.add(page);
        page = inflater.inflate(R.layout.weathershowactivity, null);
        screens.add(page);
        PageAdapter pagerAdapter = new PageAdapter(screens);
        viewScreen = new ViewPager(this);
        viewScreen.setAdapter(pagerAdapter);
        setContentView(viewScreen);
    }


    public void showWeather()
    {
        String[] f = new String[16];
        f[0] = "----------------------------------";
        f[1] = finalWeather[0];
        f[2] = "----------------------------------";
        for(int i = 0 ; i < 3 ; i++)
        {
            f[i+3] = finalWeather[i+1];
        }
        f[6] = "----------------------------------";
        for(int i = 0 ; i < 3 ; i++)
        {
            f[i+7] = finalWeather[i+4];
        }
        f[10] = "----------------------------------";
        for(int i = 0 ; i < 3 ; i++)
        {
            f[i+11] = finalWeather[i+7];
        }
        f[14] = "----------------------------------";
        f[15] = "Created by Sergey Budkov 2536 \n >----(^_^)----<";
        if(isFirst){
            isFirst=false;
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,	R.layout.weatherlistitem,f);
        ListView lv = (ListView) screens.get(2).findViewById(R.id.listhard);
        lv.setAdapter(adapter);
    }

    public void preCreate(){
        finalWeather[0] = "Что-то пошло не так.. Пожалуйста выберете город еще раз. Мне действительно очень жаль :("; 
        for(int i = 1 ; i < 10;i++){
            finalWeather[i] = ""+i;
        }
    }

    @Override
    protected void onDestroy() {
        wBase.close();
        super.onDestroy();
        if (isConnect == true) {
            unregisterReceiver(myBroadcastReceiver);
            isConnect = false;
        }
    }
}