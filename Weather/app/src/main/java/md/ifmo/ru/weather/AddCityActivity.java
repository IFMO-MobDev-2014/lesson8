package md.ifmo.ru.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URLEncoder;


public class AddCityActivity extends Activity {

    private Context context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_city);
        context = this;
        Intent intent = getIntent();
        String localCity = intent.getStringExtra(MainActivity.CODE);
        if ((localCity!=null) && (localCity!=" ")) {
            TextView textView = (TextView) findViewById(R.id.add_download);
            textView.setText("Загружаем...");
            ListView lvAdd = (ListView) findViewById(R.id.lvAdd);
            ArrayAdapter<String> adapterC = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[0]);
            lvAdd.setAdapter(adapterC);
            new SearchTask().execute(localCity);
        }
    }

    public void addCity(View v) {
        EditText editText = (EditText) findViewById(R.id.add_name);
        String city;
        city = editText.getText().toString();
        TextView textView = (TextView) findViewById(R.id.add_download);
        textView.setText("Загружаем...");
        ListView lvAdd = (ListView) findViewById(R.id.lvAdd);
        ArrayAdapter<String> adapterC = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[0]);
        lvAdd.setAdapter(adapterC);
        new SearchTask().execute(city);
    }

    private static final String API_KEY = "9r8bamvx7utesvcyxb993ggm";
    private static final String API_URL = "http://api.worldweatheronline.com/free/v1/search.ashx?format=json&num_of_results=42&key=" + API_KEY;
    private static final String BAD_LUCK = "Не удалось выполнить операцию. Проверьте подключение к Интернету и попробуйте ещё раз";
    private static final String BAD_SEARCH = "К сожалению, поиск не дал результатов";

    private class SearchTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String cityN = params[0];
                {
                    String T_API_KEY = "trnsl.1.1.20131001T130428Z.18896fd9b4b712d0.b8984cdd58a32edec6bbbd7e752ad2ad3b262b5f";
                    String T_API_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?lang=ru-en&key=" + T_API_KEY + "&text=";
                    HttpResponse httpResponseT = new DefaultHttpClient().execute(new HttpGet(T_API_URL + URLEncoder.encode(cityN)));
                    HttpEntity httpEntityT = httpResponseT.getEntity();
                    String jsonT = EntityUtils.toString(httpEntityT, "UTF-8");
                    JSONObject objectT = (JSONObject) new JSONTokener(jsonT).nextValue();
                    String resultT = objectT.getString("text");
                    cityN = resultT.substring(2, resultT.length() - 2);
                    cityN = cityN.replace('-', ' ');
                }
                String url = API_URL + "&q=" + URLEncoder.encode(cityN);
                HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(url));
                HttpEntity httpEntity = httpResponse.getEntity();
                String json = EntityUtils.toString(httpEntity, "UTF-8");

                String result = "";
                JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                if (object.has("search_api")) {
                    json = object.getString("search_api");
                    object = (JSONObject) new JSONTokener(json).nextValue();
                    JSONArray array = new JSONArray(object.getString("result"));
                    for (int id = 0; id < array.length(); id++) {
                        object = array.getJSONObject(id);
                        if (id > 0) {
                            result += "|";
                        }
                        String city = new JSONArray(object.getString("areaName")).getJSONObject(0).getString("value");
                        String region = object.has("region") ? (", " + new JSONArray(object.getString("region")).getJSONObject(0).getString("value")) : "";
                        String country = object.has("country") ? (", " + new JSONArray(object.getString("country")).getJSONObject(0).getString("value")) : "";
                        result += city + region + country;
                    }
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        String[] cities;

        @Override
        protected void onPostExecute(String result) {
            TextView download = (TextView) findViewById(R.id.add_download);
            if (result == null) {
                download.setText(BAD_LUCK);
            } else
            if ("".equals(result)) {
                download.setText(BAD_SEARCH);
            } else {
                download.setText("");
                cities = result.split("\\|");
                ListView lvAdd = (ListView) findViewById(R.id.lvAdd);
                ArrayAdapter<String> adapterC = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, cities);
                lvAdd.setAdapter(adapterC);
                lvAdd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        push(cities[position]);
                    }
                });
            }
        }
    }

    public void push(String city) {
        DBAdapter myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        myDBAdapter.addCity(city);
        myDBAdapter.close();
        this.finish();
    }
}
