package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataManager implements LoaderManager.LoaderCallbacks<List<String>> {
    private Context context;
    private Loader<List<String>> loader;
    private List<String> cities;

    public DataManager(Activity c) {
        context = c.getApplicationContext();
        loader = new AsyncTaskLoader<List<String>>(context) {
            @Override
            public List<String> loadInBackground() {
                ArrayList <String> ret = new ArrayList<>();
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(context.getAssets().open("city_list.txt")));
                    while (true) {
                        String str = bufferedReader.readLine();
                        if (str == null)
                            break;
                        ret.add(str);
                    }
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        c.getLoaderManager().initLoader(45, null, this).forceLoad();
    }

    @Override
    public Loader<List<String>> onCreateLoader(int i, Bundle bundle) {
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<String>> arrayListLoader, List<String> strings) {
        cities = strings;
    }

    @Override
    public void onLoaderReset(Loader<List<String>> arrayListLoader) {

    }

    public String getCityName(int i) {
        return cities.get(i);
    }

    public int prefixLeftBound(String prefix) {
        int l = 0;
        int r = cities.size();
        prefix = prefix.toLowerCase();
        while (r - l > 1) {
            int m = (l + r) / 2;
            if (prefix.compareTo(cities.get(m).toLowerCase()) > 0) {
                l = m;
            } else {
                r = m;
            }
        }
        return r;
    }

    public int prefixRightBound(String prefix) {
        int l = 0;
        int r = cities.size();
        prefix = prefix.toLowerCase();
        while (r - l > 1) {
            int m = (l + r) / 2;
            String cur = cities.get(m).toLowerCase();
            cur = cur.substring(0, Math.min(cur.length(), prefix.length()));
            if (prefix.compareTo(cur) >= 0) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }
}
