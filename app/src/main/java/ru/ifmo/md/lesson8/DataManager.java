package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import ru.ifmo.md.lesson8.provider.WeatherDatabaseHelper;
import ru.ifmo.md.lesson8.provider.WeatherProvider;

/**
 * Created by pva701 on 07.12.14.
 */
public class DataManager implements LoaderManager.LoaderCallbacks<ArrayList<String> > {
    private static DataManager instance;
    public static DataManager get(Context context) {
        if (instance == null)
            instance = new DataManager(context);
        return instance;
    }

    public static DataManager get(Activity context) {
        if (instance == null)
            instance = new DataManager(context);
        return instance;
    }

    private HashSet<String> se = new HashSet<String>();
    private Context con;
    private ArrayList <String> allCities;
    @Override
    public Loader<ArrayList<String>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<ArrayList<String>>(con) {
            @Override
            public ArrayList<String> loadInBackground() {
                //read list here
                Log.i("DataManager", "loadInBackground");
                long l = System.currentTimeMillis();
                ArrayList <String> ret = new ArrayList<>();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(con.getAssets().open("city_list.txt")));
                    while (true) {
                        String str = bufferedReader.readLine();
                        if (str == null)
                            break;
                        ret.add(str);
                    }
                    //Log.i("DataManager", "time load = " + (System.currentTimeMillis() - l) / 1000.0);
                    return ret;
                } catch (IOException e) {
                    Log.i("DataManager", "IOExc");
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> arrayListLoader, ArrayList<String> strings) {
        allCities = strings;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> arrayListLoader) {
    }

    private DataManager(Context c) {
        con = c.getApplicationContext();
        dump();
    }

    private DataManager(Activity c) {
        con = c.getApplicationContext();
        c.getLoaderManager().initLoader(45, null, this).forceLoad();
        dump();
    }

    private void dump() {
        Cursor cur = con.getContentResolver().query(WeatherProvider.CITY_CONTENT_URI, null, null, null, null);
        while (cur.moveToNext())
            se.add(WeatherDatabaseHelper.CityCursor.getCity(cur).getName());
    }

    public boolean contains(String name) {
        return se.contains(name);
    }

    public void insertCity(City c) {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabaseHelper.CITY_IS_SELECTED, 0);
        cv.put(WeatherDatabaseHelper.CITY_NAME, c.getName());
        cv.put(WeatherDatabaseHelper.CITY_LAST_UPDATE, 0);
        con.getContentResolver().insert(WeatherProvider.CITY_CONTENT_URI, cv);
        se.add(c.getName());
    }

    public boolean deleteCity(City c) {
        Cursor cur = con.getContentResolver().query(WeatherProvider.CITY_CONTENT_URI, null, null, null, null);
        int count = 0;
        while (cur.moveToNext() && ++count < 2);
        if (count == 1)
            return false;
        con.getContentResolver().delete(WeatherProvider.CITY_CONTENT_URI,
                WeatherDatabaseHelper.CITY_ID + " = " + c.getId(), null);
        se.remove(c.getName());
        return true;
    }

    public void setSelect(City c) {
        ContentValues cv2 = new ContentValues();
        cv2.put(WeatherDatabaseHelper.CITY_IS_SELECTED, 0);
        con.getContentResolver().update(WeatherProvider.CITY_CONTENT_URI, cv2,
                WeatherDatabaseHelper.CITY_IS_SELECTED + " = " + 1, null);

        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabaseHelper.CITY_IS_SELECTED, 1);
        con.getContentResolver().update(WeatherProvider.CITY_CONTENT_URI, cv,
                WeatherDatabaseHelper.CITY_ID + " = " + c.getId(), null);
    }

    public String getCity(int i) {
        return allCities.get(i);
    }

    public int countCities() {
        return allCities.size();
    }
}
