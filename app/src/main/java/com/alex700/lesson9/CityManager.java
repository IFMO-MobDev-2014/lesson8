package com.alex700.lesson9;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CityManager implements LoaderManager.LoaderCallbacks<List<String>> {
    Loader<List<String>> loader;
    private Context context;
    private List<String> cities;

    public CityManager(Activity activity) {
        this.cities = new ArrayList<>();
        this.context = activity.getApplicationContext();
        loader = new AsyncTaskLoader<List<String>>(context) {
            @Override
            public List<String> loadInBackground() {
                List<String> strings = new ArrayList<>();
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(context.getAssets().open("city_list.txt")));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TRIE", "city_list.txt not found");
                    return null;
                }
                try {
                    Log.d("init", "start");
                    for (String s = br.readLine(); s != null; s = br.readLine()) {
                        strings.add(s);
                    }
                    Log.d("init", "stop");
                    return strings;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }
        };
        activity.getLoaderManager().initLoader(2223, null, this).forceLoad();
    }

    public int getCount() { return cities.size(); }

    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        cities = data;
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }

    String getCityName(int i) {
        return cities.get(i);
    }

    int leftBinarySearch(String s) {
        int l = 0;
        int r = cities.size();
        while (l < r - 1) {
            int m = (l + r) / 2;
            if (cities.get(m).compareToIgnoreCase(s) < 0) {
                l = m;
            } else {
                r = m;
            }
        }
        return r;
    }

    int rightBinarySearch(String s) {
        int l = -1;
        int r = cities.size();
        while (l < r - 1) {
            int m = (l + r) / 2;
            if (cities.get(m).substring(0, Math.min(s.length(), cities.get(m).length())).compareToIgnoreCase(s) <= 0) {
                l = m;
            } else {
                r = m;
            }
        }
        return r;
    }
}