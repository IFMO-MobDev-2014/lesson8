package weathertogo.sergeybudkov.ru.weathertogo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private LocationManager locationManager;
    SharedPreferences sPref;
    TextView loc;
    boolean noNeed = false;
    public static String NOW = "NOW",
            LAST = "LAST",
            INTENT = "key_intent",
            CITY = "city",
            COUNTRY = "country",
            NO = "NO",
            YES = "YES",
            YANDEX_ID = "yandex_id",
            CITY_YES = "";
    static ShowBuilder wt ;
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
    boolean isConnect = false,
            isDialog = false,
            isFirst = false,
            needLoc = false;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        wt=new ShowBuilder();
        needLoc = true;
        getLoaderManager().initLoader(0, null, this);
        tutorialShow();
        buildingViewPages();
        loc = (TextView) screens.get(1).findViewById(R.id.textView);
        currentCitiesListView = (ListView) screens.get(0).findViewById(R.id.all_cities);
        addedCitiesListView = (ListView) screens.get(1).findViewById(R.id.added_cities);
        openFileAndPrintAllCities();
        printCityFromTable();
        compose(LAST);
        showWeather();
        printCityFromTable();

        if (addedCities.isEmpty() == true) {
            isFirst = true;
        } else {
            viewScreen.setCurrentItem(1);
        }
        addedCitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long number) {
                compose(addedCities.get(index).get(CITY));
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

                ContentValues cv = new ContentValues();
                cv.put("city",currentCities.get(index).get(CITY));
                cv.put("country",currentCities.get(index).get(COUNTRY));
                getContentResolver().delete(Uri.parse("content://ru.sergeybudkov.weathertogo/cities"),currentCities.get(index).get(CITY)+" "+currentCities.get(index).get(COUNTRY),null);

                isConnect = true;
                isDialog = true;
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setCancelable(false);
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



    public void compose(String s){
        wt.inFinalWeather = 1;
        wt.composeToday(s);
        wt.composeNextDays(3, s);
        wt.composeNextDays(4, s);
        wt.composeNextDays(5, s);
    }


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

                getContentResolver().delete(Uri.parse("content://ru.sergeybudkov.weathertogo/cities"),addedCities.get(pozition).get(CITY)+" "+ addedCities.get(pozition).get(COUNTRY) , null);
                printCityFromTable();
                break;
            case 2:
                String city_from_table = addedCities.get(pozition).get(CITY);
                String country_from_table = addedCities.get(pozition).get(COUNTRY);
                String yandex_id_from_table = addedCitiesID.get(pozition).toString();
                getContentResolver().delete(Uri.parse("content://ru.sergeybudkov.weathertogo/cities"),addedCities.get(pozition).get(CITY)+" "+ addedCities.get(pozition).get(COUNTRY) , null);
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
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Обновление завершено", Toast.LENGTH_SHORT);
                toast.show();
                noNeed=true;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDialog == false || flagForContext == true) {
                flagForContext = false;
                compose(LAST);
                showWeather();
                viewScreen.setCurrentItem(1);
            }
            printCityFromTable();
            if (isDialog == true) dialog.dismiss();
            isDialog = false;
        }
    }

    private void printCityFromTable() {
        addedCitiesID = new ArrayList<Integer>();
        addedCities = new ArrayList<HashMap<String, String>>();
        addedCities =wt.takeSmall();
        addedCitiesID=wt.takeBig();
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
        String[] f;
        f=wt.compose();
        if(isFirst){
            isFirst=false;
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,	R.layout.weatherlistitem,f);
        ListView lv = (ListView) screens.get(2).findViewById(R.id.listhard);
        lv.setAdapter(adapter);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnect == true) {
            unregisterReceiver(myBroadcastReceiver);
            isConnect = false;
        }
    }
}