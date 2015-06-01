package ru.ifmo.ctddev.filippov.weather;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created by Dima_2 on 01.04.2015.
 */
public class CitySearchFragment extends Fragment {
    public interface InteractionListener {
        public void onCityAdded(String name, int cityId);
    }

    private InteractionListener listener;

    public static CitySearchFragment newInstance() {
        return new CitySearchFragment();
    }

    public CitySearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.city_search_fragment, container, false);
        SearchView searchView = (SearchView) view.findViewById(R.id.city_search);
        final ListView listView = (ListView) view.findViewById(R.id.search_list);

        Button refreshButton = (Button) getActivity().findViewById(R.id.refresh_button);
        refreshButton.setVisibility(View.GONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(String... strings) {
                        URL url = null;
                        try {
                            url = new URL("http://api.openweathermap.org/data/2.5/find?q=" + URLEncoder.encode(strings[0], "UTF-8"));
                        } catch (MalformedURLException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if (url == null) {
                            return null;
                        }

                        HttpURLConnection urlConnection = null;
                        try {
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.connect();
                            InputStream inputStream = urlConnection.getInputStream();
                            String resultString = WeatherLoader.streamToString(inputStream);
                            return new JSONObject(resultString);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                        return null;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    protected void onPostExecute(JSONObject json) {
                        try {
                            if (json == null) {
                                return;
                            }
                            int count = json.getInt("count");
                            final String[] cityNames = new String[count];
                            final int[] cityIds = new int[count];

                            for (int i = 0; i < count; i++) {
                                cityNames[i] = json.getJSONArray("list").getJSONObject(i).getString("name") + ", " +
                                        json.getJSONArray("list").getJSONObject(i).getJSONObject("sys").getString("country");
                                cityIds[i] = json.getJSONArray("list").getJSONObject(i).getInt("id");
                            }

                            final BaseAdapter adapter = new BaseAdapter() {
                                @Override
                                public int getCount() {
                                    return cityNames.length;
                                }

                                @Override
                                public Object getItem(int i) {
                                    return new Pair<>(cityNames[i], cityIds[i]);
                                }

                                @Override
                                public long getItemId(int i) {
                                    return i;
                                }

                                @Override
                                public View getView(int i, View view, ViewGroup viewGroup) {
                                    View resultView;
                                    if (view != null) {
                                        resultView = view;
                                    } else {
                                        resultView = getActivity().getLayoutInflater().inflate(R.layout.city_search_entry, viewGroup, false);
                                    }
                                    TextView textView = (TextView) resultView.findViewById(R.id.city_name);
                                    textView.setText(cityNames[i]);
                                    return resultView;
                                }
                            };

                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Pair<String, Integer> np = (Pair<String, Integer>) adapter.getItem(i);
                                    listener.onCityAdded(np.first, np.second);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                task.execute(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (InteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
