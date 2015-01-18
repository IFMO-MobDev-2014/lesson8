package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;


public class SearchActivity extends Activity {

    ListView variants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        final SearchView searchView = (SearchView) findViewById(R.id.citySearch);
        variants = (ListView) findViewById(R.id.searchList);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String s) {
                                                  AsyncTask<String, Void, Pair<String, Integer>[]> Task = new SubmitTask();
                                                  Task.execute(s);
                                                  searchView.clearFocus();
                                                  return true;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String s) {
                                                  return false;
                                              }
                                          }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class SubmitTask extends AsyncTask<String, Void, Pair<String, Integer>[]> {

        @Override
        protected Pair<String, Integer>[] doInBackground(String... strings) {
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/find?q=" + URLEncoder.encode(strings[0], "UTF-8"));
                try {
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();
                    Scanner scanner = new Scanner(urlConnection.getInputStream());
                    scanner.useDelimiter("\\A");
                    JSONObject result = new JSONObject(scanner.next());
                    try {
                        int count = result.getInt("count");
                        Pair<String, Integer>[] cities = new Pair[count];
                        for (int i = 0; i < count; i++) {
                            String name = result.getJSONArray("list").getJSONObject(i).getString("name") + ", " +
                                    result.getJSONArray("list").getJSONObject(i).getJSONObject("sys").getString("country");
                            Integer id = result.getJSONArray("list").getJSONObject(i).getInt("id");
                            cities[i] = new Pair<>(name, id);
                        }
                        return cities;
                    } catch (JSONException jse) {
                        jse.printStackTrace();
                    }
                } catch (IOException | JSONException ioe) {
                    ioe.printStackTrace();
                }
                return null;
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Pair<String, Integer>[] cities) {

            final BaseAdapter adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return cities.length;
                }

                @Override
                public Object getItem(int i) {
                    return new Pair(cities[i].first, cities[i].second);
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    View view1 = getLayoutInflater().inflate(R.layout.city_search_entry, viewGroup, false);
                    TextView textView = (TextView) view1.findViewById(R.id.cityName);
                    textView.setText(cities[i].first);
                    return view1;
                }
            };

            variants.setAdapter(adapter);
            variants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Pair<String, Integer> city = (Pair) adapter.getItem(i);
                    Intent intent = new Intent();
                    intent.putExtra(CitiesTable.NAME, city.first);
                    intent.putExtra(CitiesTable.URL, city.second);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        }
    }

}
