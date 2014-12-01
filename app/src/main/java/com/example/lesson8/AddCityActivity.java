package com.example.lesson8;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AddCityActivity extends Activity {
    public static final String REQUEST_LINK = "http://xml.weather.co.ua/1.2/city/";

    private CityAdapter adapter = null;
    private ArrayList<City> cities = null;
    private CityExecutor cityExecutor = null;
    private WeatherDB weatherDB = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_city);

        adapter = new CityAdapter();
        ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);

        weatherDB = new WeatherDB(this);
        weatherDB.open();

        EditText editText = (EditText) findViewById(R.id.Edit);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (cityExecutor != null) {
                    cityExecutor.cancel(true);
                }
                cityExecutor = null;
                cities = null;
                adapter.notifyDataSetChanged();

                String city = charSequence.toString();
                city = city.replaceAll(" ", "%20");
                new CityExecutor().execute(city);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                weatherDB.insertData(new Forecast(cities.get(i).getCity(), "", ""));
                Intent intent = new Intent(AddCityActivity.this, MyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public class CityExecutor extends AsyncTask<String, Void, ArrayList<City>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<City> doInBackground(String... strings) {
            String link = REQUEST_LINK + "?search=" + strings[0] + "&lang=en";
            ArrayList<City> resultList = null;
            try {
                URL url = new URL(link);

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                XMLReader xmlReader = parser.getXMLReader();
                CityParserHandler handler = new CityParserHandler();
                xmlReader.setContentHandler(handler);
                InputSource is = new InputSource(url.openStream());
                xmlReader.parse(is);
                resultList = handler.getResult();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<City> result) {
            super.onPostExecute(result);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
            try {
                cities = result;
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CityAdapter extends ArrayAdapter<City> {

        CityAdapter() {
            super(AddCityActivity.this, R.layout.city_adapter);
        }

        @Override
        public int getCount() {
            if (cities != null) {
                return cities.size();
            } else {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View current = null;
            LayoutInflater inflater = getLayoutInflater();
            current = inflater.inflate(R.layout.city_adapter, parent, false);
            TextView cityName = (TextView) current.findViewById(R.id.cityName);
            TextView countryName = (TextView) current.findViewById(R.id.countryName);
            City curCity = cities.get(position);
            if (curCity != null) {
                if (curCity.getCity() != null) {
                    cityName.setText(cities.get(position).getCity());
                }
                if (curCity.getCountry() != null) {
                    countryName.setText(curCity.getCountry());
                }
            }
            return current;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weatherDB != null)
            weatherDB.close();
    }

}
