package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CitySearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CitySearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CitySearchFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public static CitySearchFragment newInstance() {
        CitySearchFragment fragment = new CitySearchFragment();
        return fragment;
    }

    public CitySearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rv = inflater.inflate(R.layout.fragment_city_search, container, false);

        SearchView sv = (SearchView) rv.findViewById(R.id.citySearch);
        final ListView lv = (ListView) rv.findViewById(R.id.searchList);

        class NanoPair {
            public String name;
            public int id;
            public NanoPair(String name, int id) {
                this.name = name;
                this.id = id;
            }
        };

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

                        try {
                            URLConnection uc = url.openConnection();
                            uc.connect();
                            InputStream is = uc.getInputStream();
                            String s = WeatherLoaderService.streamToString(is);
                            return new JSONObject(s);
                        } catch(IOException | JSONException ioe) {
                            ioe.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(JSONObject res) {
                        try {
                            int count = res.getInt("count");
                            final String[] cityNames = new String[count];
                            final int[] cityIds = new int[count];

                            for(int i = 0; i < count; i++) {
                                cityNames[i] = res.getJSONArray("list").getJSONObject(i).getString("name") + ", " +
                                    res.getJSONArray("list").getJSONObject(i).getJSONObject("sys").getString("country");
                                cityIds[i] = res.getJSONArray("list").getJSONObject(i).getInt("id");
                            }

                            final BaseAdapter adapter = new BaseAdapter() {
                                @Override
                                public int getCount() {
                                    return cityNames.length;
                                }

                                @Override
                                public Object getItem(int i) {
                                    return new NanoPair(cityNames[i], cityIds[i]);
                                }

                                @Override
                                public long getItemId(int i) {
                                    return i;
                                }

                                @Override
                                public View getView(int i, View view, ViewGroup viewGroup) {
                                    View v;
                                    if(view != null) {
                                        v = view;
                                    } else {
                                        v = getActivity().getLayoutInflater().inflate(R.layout.city_search_entry, viewGroup, false);
                                    }

                                    TextView tv = (TextView) v.findViewById(R.id.cityName);

                                    tv.setText(cityNames[i]);
                                    return v;
                                }
                            };

                            lv.setAdapter(adapter);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    NanoPair np = (NanoPair) adapter.getItem(i);
                                    mListener.onCityAdded(np.name, np.id);
                                }
                            });
                        } catch(JSONException jse) {
                            jse.printStackTrace();
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

        return rv;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onCityAdded(String name, int cityId);
    }

}
