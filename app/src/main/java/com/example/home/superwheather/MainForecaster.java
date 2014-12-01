package com.example.home.superwheather;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.CursorLoader;
import android.widget.Toast;


public class MainForecaster extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int sectionNumber;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_forecaster);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        sectionNumber = 1;

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String city = (String) mNavigationDrawerFragment.getAdapter().getItem(sectionNumber - 1);

        getLoaderManager().initLoader(0, null, new LoaderCallbacksHolder(city, null));

        Intent serviceIntent = new Intent(MainForecaster.this, MyService.class);
        startService(serviceIntent.putExtra("city", (String) mNavigationDrawerFragment.getAdapter().getItem(sectionNumber - 1)));

        IntentFilter intentFilter = new IntentFilter(MyService.ACTION_MYSERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        if (myBroadcastReceiver != null)
            unregisterReceiver(myBroadcastReceiver);
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (position == -1) {
            loading = true;
            View rootView = ((PlaceholderFragment) getFragmentManager().findFragmentById(R.id.container)).getRootView();

            String city = (String) mNavigationDrawerFragment.getAdapter().getItem(sectionNumber - 1);

            getLoaderManager().restartLoader(0, null, new LoaderCallbacksHolder(city, rootView));

            ((ImageView) rootView.findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.i96_2));

            Intent serviceIntent = new Intent(MainForecaster.this, MyService.class);
            startService(serviceIntent.putExtra("city", (String) mNavigationDrawerFragment.getAdapter().getItem(sectionNumber - 1)));

            IntentFilter intentFilter = new IntentFilter(MyService.ACTION_MYSERVICE);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            if (myBroadcastReceiver != null)
                unregisterReceiver(myBroadcastReceiver);
            myBroadcastReceiver = new MyBroadcastReceiver();
            registerReceiver(myBroadcastReceiver, intentFilter);
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            PlaceholderFragment fragment = PlaceholderFragment.newInstance(position + 1);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void onSectionAttached(int number) {
        if (mNavigationDrawerFragment != null) {
            mTitle = (String) mNavigationDrawerFragment.getAdapter().getItem(number - 1);
        }
        sectionNumber = number;
    }

    private MyBroadcastReceiver myBroadcastReceiver;
    private Toast prevToast;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private static final String[] months = new String[] {"января", "февраля", "марта", "апреля",
                                                      "мая", "июня", "июля", "августа",
                                                      "сентября", "октября", "ноября", "декабря"};

    private String normalizeDate(String s) {
        String[] parts = s.split("-");
        return parts[2] + " " + months[Integer.valueOf(parts[1]) - 1] + ", " + parts[0];
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                if (intent.getBooleanExtra("succeed", false)) {

                    ((TextView) findViewById(R.id.temperature)).setText(intent.getStringExtra("temp") + "°C");
                    ((TextView) findViewById(R.id.cloudcover)).setText("Облачность: " + intent.getStringExtra("cloud") + "%");
                    ((TextView) findViewById(R.id.humidity)).setText("Влажность: " + intent.getStringExtra("hum") + "%");
                    ((TextView) findViewById(R.id.pressure)).setText("Давление: " + Integer.toString((int) (Integer.valueOf(intent.getStringExtra("press")) / 1.33322368d)) + " мм.рт.ст.");
                    int cloudcover = Integer.valueOf(intent.getStringExtra("cloud"));
                    int id;
                    if (cloudcover < 25) {
                        id = R.drawable.i96_2;
                    } else if (cloudcover < 50) {
                        id = R.drawable.i96_3;
                    } else if (cloudcover < 75) {
                        id = R.drawable.i96_4;
                    } else {
                        id = R.drawable.i96_5;
                    }
                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id));

                    ContentValues values = new ContentValues();

                    String city = intent.getStringExtra("city");

                    values.put(MyTable.COLUMN_T_ID, 2);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringExtra("temp") + "°C" );
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + intent.getStringExtra("cloud") + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + intent.getStringExtra("hum") + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (Integer.valueOf(intent.getStringExtra("press")) / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, "" + id);

                    Cursor cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "2", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "2", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //*******************************************************************************************

                    ((TextView) findViewById(R.id.header1)).setText(normalizeDate(intent.getStringArrayExtra("dates")[0]));
                    ((TextView) findViewById(R.id.temp_1_1)).setText(intent.getStringArrayExtra("1_00:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_1_2)).setText(intent.getStringArrayExtra("1_12:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_1_3)).setText(intent.getStringArrayExtra("1_21:00")[1] + "°C");
                    int cloudcover_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0])) / 3;
                    ((TextView) findViewById(R.id.cloudcover_1)).setText("Облачность: " + cloudcover_1 + "%");
                    int humidity_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_12:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_21:00")[3])) / 3;
                    ((TextView) findViewById(R.id.humidity_1)).setText("Влажность: " + humidity_1 + "%");
                    int pressure_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_12:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("1_21:00")[2])) / 3;
                    ((TextView) findViewById(R.id.pressure_1)).setText("Давление: " + Integer.toString((int) (pressure_1 / 1.33322368d)) + " мм.рт.ст.");
                    int id_1_1;
                    if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 25) {
                        id_1_1 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 50) {
                        id_1_1 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 75) {
                        id_1_1 = R.drawable.s96_4;
                    } else {
                        id_1_1 = R.drawable.s96_5;
                    }
                    int id_1_2;
                    if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 25) {
                        id_1_2 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 50) {
                        id_1_2 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 75) {
                        id_1_2 = R.drawable.s96_4;
                    } else {
                        id_1_2 = R.drawable.s96_5;
                    }
                    int id_1_3;
                    if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 25) {
                        id_1_3 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 50) {
                        id_1_3 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 75) {
                        id_1_3 = R.drawable.s96_4;
                    } else {
                        id_1_3 = R.drawable.s96_5;
                    }

                    ((ImageView) findViewById(R.id.image_line_1_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_1_1));
                    ((ImageView) findViewById(R.id.image_line_1_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_1_2));
                    ((ImageView) findViewById(R.id.image_line_1_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_1_3));

                    values = new ContentValues();

                    values.put(MyTable.COLUMN_T_ID, 2 + 1);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("1_00:00")[1] + "°C&" + intent.getStringArrayExtra("1_12:00")[1] + "°C&" + intent.getStringArrayExtra("1_21:00")[1] + "°C");
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_1 + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_1 + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_1 / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, id_1_1 + "&" + id_1_2 + "&" + id_1_3);
                    values.put(MyTable.COLUMN_DATE, normalizeDate(intent.getStringArrayExtra("dates")[0]));

                    cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "3", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "3", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //***************************************************************************************************

                    ((TextView) findViewById(R.id.header2)).setText(normalizeDate(intent.getStringArrayExtra("dates")[1]));
                    ((TextView) findViewById(R.id.temp_2_1)).setText(intent.getStringArrayExtra("2_00:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_2_2)).setText(intent.getStringArrayExtra("2_12:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_2_3)).setText(intent.getStringArrayExtra("2_21:00")[1] + "°C");
                    int cloudcover_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0])) / 3;
                    ((TextView) findViewById(R.id.cloudcover_2)).setText("Облачность: " + cloudcover_2 + "%");
                    int humidity_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_12:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_21:00")[3])) / 3;
                    ((TextView) findViewById(R.id.humidity_2)).setText("Влажность: " + humidity_2 + "%");
                    int pressure_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_12:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("2_21:00")[2])) / 3;
                    ((TextView) findViewById(R.id.pressure_2)).setText("Давление: " + Integer.toString((int) (pressure_2 / 1.33322368d)) + " мм.рт.ст.");
                    int id_2_1;
                    if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 25) {
                        id_2_1 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 50) {
                        id_2_1 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 75) {
                        id_2_1 = R.drawable.s96_4;
                    } else {
                        id_2_1 = R.drawable.s96_5;
                    }
                    int id_2_2;
                    if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 25) {
                        id_2_2 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 50) {
                        id_2_2 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 75) {
                        id_2_2 = R.drawable.s96_4;
                    } else {
                        id_2_2 = R.drawable.s96_5;
                    }
                    int id_2_3;
                    if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 25) {
                        id_2_3 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 50) {
                        id_2_3 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 75) {
                        id_2_3 = R.drawable.s96_4;
                    } else {
                        id_2_3 = R.drawable.s96_5;
                    }

                    ((ImageView) findViewById(R.id.image_line_2_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_2_1));
                    ((ImageView) findViewById(R.id.image_line_2_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_2_2));
                    ((ImageView) findViewById(R.id.image_line_2_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_2_3));

                    values = new ContentValues();

                    values.put(MyTable.COLUMN_T_ID, 2 + 2);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("2_00:00")[1] + "°C&" + intent.getStringArrayExtra("2_12:00")[1] + "°C&" + intent.getStringArrayExtra("2_21:00")[1] + "°C");
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_2 + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_2 + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_2 / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, id_2_1 + "&" + id_2_2 + "&" + id_2_3);
                    values.put(MyTable.COLUMN_DATE, normalizeDate(intent.getStringArrayExtra("dates")[1]));

                    cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "4", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "4", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //***************************************************************************************************

                    ((TextView) findViewById(R.id.header3)).setText(normalizeDate(intent.getStringArrayExtra("dates")[2]));
                    ((TextView) findViewById(R.id.temp_3_1)).setText(intent.getStringArrayExtra("3_00:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_3_2)).setText(intent.getStringArrayExtra("3_12:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_3_3)).setText(intent.getStringArrayExtra("3_21:00")[1] + "°C");
                    int cloudcover_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0])) / 3;
                    ((TextView) findViewById(R.id.cloudcover_3)).setText("Облачность: " + cloudcover_3 + "%");
                    int humidity_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_12:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_21:00")[3])) / 3;
                    ((TextView) findViewById(R.id.humidity_3)).setText("Влажность: " + humidity_3 + "%");
                    int pressure_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_12:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("3_21:00")[2])) / 3;
                    ((TextView) findViewById(R.id.pressure_3)).setText("Давление: " + Integer.toString((int) (pressure_3 / 1.33322368d)) + " мм.рт.ст.");
                    int id_3_1;
                    if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 25) {
                        id_3_1 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 50) {
                        id_3_1 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 75) {
                        id_3_1 = R.drawable.s96_4;
                    } else {
                        id_3_1 = R.drawable.s96_5;
                    }
                    int id_3_2;
                    if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 25) {
                        id_3_2 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 50) {
                        id_3_2 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 75) {
                        id_3_2 = R.drawable.s96_4;
                    } else {
                        id_3_2 = R.drawable.s96_5;
                    }
                    int id_3_3;
                    if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 25) {
                        id_3_3 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 50) {
                        id_3_3 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 75) {
                        id_3_3 = R.drawable.s96_4;
                    } else {
                        id_3_3 = R.drawable.s96_5;
                    }

                    ((ImageView) findViewById(R.id.image_line_3_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_3_1));
                    ((ImageView) findViewById(R.id.image_line_3_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_3_2));
                    ((ImageView) findViewById(R.id.image_line_3_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_3_3));

                    values = new ContentValues();

                    values.put(MyTable.COLUMN_T_ID, 2 + 3);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("3_00:00")[1] + "°C&" + intent.getStringArrayExtra("3_12:00")[1] + "°C&" + intent.getStringArrayExtra("3_21:00")[1] + "°C");
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_3 + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_3 + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_3 / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, id_3_1 + "&" + id_3_2 + "&" + id_3_3);
                    values.put(MyTable.COLUMN_DATE, normalizeDate(intent.getStringArrayExtra("dates")[2]));

                    cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "5", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "5", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //***************************************************************************************************

                    ((TextView) findViewById(R.id.header4)).setText(normalizeDate(intent.getStringArrayExtra("dates")[3]));
                    ((TextView) findViewById(R.id.temp_4_1)).setText(intent.getStringArrayExtra("4_00:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_4_2)).setText(intent.getStringArrayExtra("4_12:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_4_3)).setText(intent.getStringArrayExtra("4_21:00")[1] + "°C");
                    int cloudcover_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0])) / 3;
                    ((TextView) findViewById(R.id.cloudcover_4)).setText("Облачность: " + cloudcover_4 + "%");
                    int humidity_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_12:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_21:00")[3])) / 3;
                    ((TextView) findViewById(R.id.humidity_4)).setText("Влажность: " + humidity_4 + "%");
                    int pressure_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_12:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("4_21:00")[2])) / 3;
                    ((TextView) findViewById(R.id.pressure_4)).setText("Давление: " + Integer.toString((int) (pressure_4 / 1.33322368d)) + " мм.рт.ст.");
                    int id_4_1;
                    if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 25) {
                        id_4_1 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 50) {
                        id_4_1 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 75) {
                        id_4_1 = R.drawable.s96_4;
                    } else {
                        id_4_1 = R.drawable.s96_5;
                    }
                    int id_4_2;
                    if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 25) {
                        id_4_2 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 50) {
                        id_4_2 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 75) {
                        id_4_2 = R.drawable.s96_4;
                    } else {
                        id_4_2 = R.drawable.s96_5;
                    }
                    int id_4_3;
                    if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 25) {
                        id_4_3 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 50) {
                        id_4_3 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 75) {
                        id_4_3 = R.drawable.s96_4;
                    } else {
                        id_4_3 = R.drawable.s96_5;
                    }

                    ((ImageView) findViewById(R.id.image_line_4_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_4_1));
                    ((ImageView) findViewById(R.id.image_line_4_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_4_2));
                    ((ImageView) findViewById(R.id.image_line_4_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_4_3));

                    values = new ContentValues();

                    values.put(MyTable.COLUMN_T_ID, 2 + 4);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("4_00:00")[1] + "°C&" + intent.getStringArrayExtra("4_12:00")[1] + "°C&" + intent.getStringArrayExtra("4_21:00")[1] + "°C");
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_4 + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_4 + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_4 / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, id_4_1 + "&" + id_4_2 + "&" + id_4_3);
                    values.put(MyTable.COLUMN_DATE, normalizeDate(intent.getStringArrayExtra("dates")[3]));

                    cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "6", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "6", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //***************************************************************************************************

                    ((TextView) findViewById(R.id.header5)).setText(normalizeDate(intent.getStringArrayExtra("dates")[4]));
                    ((TextView) findViewById(R.id.temp_5_1)).setText(intent.getStringArrayExtra("5_00:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_5_2)).setText(intent.getStringArrayExtra("5_12:00")[1] + "°C");
                    ((TextView) findViewById(R.id.temp_5_3)).setText(intent.getStringArrayExtra("5_21:00")[1] + "°C");
                    int cloudcover_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0])) / 3;
                    ((TextView) findViewById(R.id.cloudcover_5)).setText("Облачность: " + cloudcover_5 + "%");
                    int humidity_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_12:00")[3]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_21:00")[3])) / 3;
                    ((TextView) findViewById(R.id.humidity_5)).setText("Влажность: " + humidity_5 + "%");
                    int pressure_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_12:00")[2]) +
                            Integer.valueOf(intent.getStringArrayExtra("5_21:00")[2])) / 3;
                    ((TextView) findViewById(R.id.pressure_5)).setText("Давление: " + Integer.toString((int) (pressure_5 / 1.33322368d)) + " мм.рт.ст.");
                    int id_5_1;
                    if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 25) {
                        id_5_1 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 50) {
                        id_5_1 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 75) {
                        id_5_1 = R.drawable.s96_4;
                    } else {
                        id_5_1 = R.drawable.s96_5;
                    }
                    int id_5_2;
                    if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 25) {
                        id_5_2 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 50) {
                        id_5_2 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 75) {
                        id_5_2 = R.drawable.s96_4;
                    } else {
                        id_5_2 = R.drawable.s96_5;
                    }
                    int id_5_3;
                    if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 25) {
                        id_5_3 = R.drawable.s96_2;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 50) {
                        id_5_3 = R.drawable.s96_3;
                    } else if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 75) {
                        id_5_3 = R.drawable.s96_4;
                    } else {
                        id_5_3 = R.drawable.s96_5;
                    }

                    ((ImageView) findViewById(R.id.image_line_5_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_5_1));
                    ((ImageView) findViewById(R.id.image_line_5_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_5_2));
                    ((ImageView) findViewById(R.id.image_line_5_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), id_5_3));

                    values = new ContentValues();

                    values.put(MyTable.COLUMN_T_ID, 2 + 5);
                    values.put(MyTable.COLUMN_TITLE, city);
                    values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("5_00:00")[1] + "°C&" + intent.getStringArrayExtra("5_12:00")[1] + "°C&" + intent.getStringArrayExtra("5_21:00")[1] + "°C");
                    values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_5 + "%");
                    values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_5 + "%");
                    values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_5 / 1.33322368d)) + " мм.рт.ст.");
                    values.put(MyTable.COLUMN_PIC_ID, id_5_1 + "&" + id_5_2 + "&" + id_5_3);
                    values.put(MyTable.COLUMN_DATE, normalizeDate(intent.getStringArrayExtra("dates")[4]));

                    cursor = MainForecaster.this.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                    "7", city}, null);

                    if (cursor.getCount() > 0) {
                        MainForecaster.this.getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                                MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                        "7", city});
                    } else {
                        MainForecaster.this.getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    }

                    //***************************************************************************************************/

                } else {
                    if (prevToast != null)
                        prevToast.cancel();
                    if (intent.getStringExtra("cause").equals("city fail")) {
                        prevToast = Toast.makeText(MainForecaster.this, "Город не найден", Toast.LENGTH_SHORT);
                    } else {
                        prevToast = Toast.makeText(MainForecaster.this, "Проблемы с интернет-соединением", Toast.LENGTH_SHORT);
                    }
                    prevToast.show();
                }
                loading = false;

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_forecaster, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.refresh_button && !loading) {
            onNavigationDrawerItemSelected(-1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private View rootView;

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main_forecaster, container, false);
            return rootView;
        }

        public View getRootView() {
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainForecaster) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private class LoaderCallbacksHolder implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String city;
        private final View view;

        public LoaderCallbacksHolder(String city, View view) {
            super();
            this.city = city;
            this.view = view;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(MainForecaster.this, MyContentProvider.CONTENT_URI, new String[] {
                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                    "2", city}, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                if (view != null) {
                    ((TextView) view.findViewById(R.id.temperature)).setText(cursor.getString(0));
                    ((TextView) view.findViewById(R.id.cloudcover)).setText(cursor.getString(1));
                    ((TextView) view.findViewById(R.id.humidity)).setText(cursor.getString(2));
                    ((TextView) view.findViewById(R.id.pressure)).setText(cursor.getString(3));
                    ((ImageView) view.findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(cursor.getString(4))));

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "3", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) view.findViewById(R.id.header1)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) view.findViewById(R.id.temp_1_1)).setText(temps[0]);
                        ((TextView) view.findViewById(R.id.temp_1_2)).setText(temps[1]);
                        ((TextView) view.findViewById(R.id.temp_1_3)).setText(temps[2]);
                        ((TextView) view.findViewById(R.id.cloudcover_1)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.humidity_1)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.pressure_1)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) view.findViewById(R.id.image_line_1_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) view.findViewById(R.id.image_line_1_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) view.findViewById(R.id.image_line_1_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "4", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) view.findViewById(R.id.header2)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) view.findViewById(R.id.temp_2_1)).setText(temps[0]);
                        ((TextView) view.findViewById(R.id.temp_2_2)).setText(temps[1]);
                        ((TextView) view.findViewById(R.id.temp_2_3)).setText(temps[2]);
                        ((TextView) view.findViewById(R.id.cloudcover_2)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.humidity_2)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.pressure_2)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) view.findViewById(R.id.image_line_2_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) view.findViewById(R.id.image_line_2_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) view.findViewById(R.id.image_line_2_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "5", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) view.findViewById(R.id.header3)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) view.findViewById(R.id.temp_3_1)).setText(temps[0]);
                        ((TextView) view.findViewById(R.id.temp_3_2)).setText(temps[1]);
                        ((TextView) view.findViewById(R.id.temp_3_3)).setText(temps[2]);
                        ((TextView) view.findViewById(R.id.cloudcover_3)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.humidity_3)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.pressure_3)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) view.findViewById(R.id.image_line_3_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) view.findViewById(R.id.image_line_3_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) view.findViewById(R.id.image_line_3_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "6", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) view.findViewById(R.id.header4)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) view.findViewById(R.id.temp_4_1)).setText(temps[0]);
                        ((TextView) view.findViewById(R.id.temp_4_2)).setText(temps[1]);
                        ((TextView) view.findViewById(R.id.temp_4_3)).setText(temps[2]);
                        ((TextView) view.findViewById(R.id.cloudcover_4)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.humidity_4)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.pressure_4)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) view.findViewById(R.id.image_line_4_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) view.findViewById(R.id.image_line_4_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) view.findViewById(R.id.image_line_4_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "7", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) view.findViewById(R.id.header5)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) view.findViewById(R.id.temp_5_1)).setText(temps[0]);
                        ((TextView) view.findViewById(R.id.temp_5_2)).setText(temps[1]);
                        ((TextView) view.findViewById(R.id.temp_5_3)).setText(temps[2]);
                        ((TextView) view.findViewById(R.id.cloudcover_5)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.humidity_5)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.pressure_5)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) view.findViewById(R.id.image_line_5_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) view.findViewById(R.id.image_line_5_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) view.findViewById(R.id.image_line_5_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                } else {
                    ((TextView) MainForecaster.this.findViewById(R.id.temperature)).setText(cursor.getString(0));
                    ((TextView) MainForecaster.this.findViewById(R.id.cloudcover)).setText(cursor.getString(1));
                    ((TextView) MainForecaster.this.findViewById(R.id.humidity)).setText(cursor.getString(2));
                    ((TextView) MainForecaster.this.findViewById(R.id.pressure)).setText(cursor.getString(3));
                    ((ImageView) MainForecaster.this.findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(cursor.getString(4))));

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "3", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) MainForecaster.this.findViewById(R.id.header1)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_1_1)).setText(temps[0]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_1_2)).setText(temps[1]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_1_3)).setText(temps[2]);
                        ((TextView) MainForecaster.this.findViewById(R.id.cloudcover_1)).setText(cursor.getString(1));
                        ((TextView) MainForecaster.this.findViewById(R.id.humidity_1)).setText(cursor.getString(2));
                        ((TextView) MainForecaster.this.findViewById(R.id.pressure_1)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_1_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_1_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_1_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "4", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) MainForecaster.this.findViewById(R.id.header2)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_2_1)).setText(temps[0]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_2_2)).setText(temps[1]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_2_3)).setText(temps[2]);
                        ((TextView) MainForecaster.this.findViewById(R.id.cloudcover_2)).setText(cursor.getString(1));
                        ((TextView) MainForecaster.this.findViewById(R.id.humidity_2)).setText(cursor.getString(2));
                        ((TextView) MainForecaster.this.findViewById(R.id.pressure_2)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_2_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_2_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_2_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "5", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) MainForecaster.this.findViewById(R.id.header3)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_3_1)).setText(temps[0]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_3_2)).setText(temps[1]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_3_3)).setText(temps[2]);
                        ((TextView) MainForecaster.this.findViewById(R.id.cloudcover_3)).setText(cursor.getString(1));
                        ((TextView) MainForecaster.this.findViewById(R.id.humidity_3)).setText(cursor.getString(2));
                        ((TextView) MainForecaster.this.findViewById(R.id.pressure_3)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_3_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_3_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_3_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "6", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) MainForecaster.this.findViewById(R.id.header4)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_4_1)).setText(temps[0]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_4_2)).setText(temps[1]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_4_3)).setText(temps[2]);
                        ((TextView) MainForecaster.this.findViewById(R.id.cloudcover_4)).setText(cursor.getString(1));
                        ((TextView) MainForecaster.this.findViewById(R.id.humidity_4)).setText(cursor.getString(2));
                        ((TextView) MainForecaster.this.findViewById(R.id.pressure_4)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_4_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_4_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_4_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }

                    cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[] {
                                    MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS, MyTable.COLUMN_PIC_ID, MyTable.COLUMN_DATE},
                            MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[] {
                                    "7", city}, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ((TextView) MainForecaster.this.findViewById(R.id.header5)).setText(cursor.getString(5));
                        String[] temps = cursor.getString(0).split("&");
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_5_1)).setText(temps[0]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_5_2)).setText(temps[1]);
                        ((TextView) MainForecaster.this.findViewById(R.id.temp_5_3)).setText(temps[2]);
                        ((TextView) MainForecaster.this.findViewById(R.id.cloudcover_5)).setText(cursor.getString(1));
                        ((TextView) MainForecaster.this.findViewById(R.id.humidity_5)).setText(cursor.getString(2));
                        ((TextView) MainForecaster.this.findViewById(R.id.pressure_5)).setText(cursor.getString(3));
                        String[] ids = cursor.getString(4).split("&");
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_5_1)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[0])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_5_2)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[1])));
                        ((ImageView) MainForecaster.this.findViewById(R.id.image_line_5_3)).setImageBitmap(BitmapFactory.decodeResource(getResources(), Integer.valueOf(ids[2])));
                    }
                    
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            cursorLoader.reset();
        }

    }

}
