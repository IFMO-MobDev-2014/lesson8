package md.ifmo.ru.weather;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends Activity {
    public static final String[] CITIES = {"Москва", "Санкт-Петербург", "Оттава"};
    public static final String[] CITIES_ENG = {"Moscow", "Saint+Petersburg", "Ottawa"};
    public static final String CODE = "city";
    public static final String CODE_ENG = "city_eng";
    private DBAdapter myDBAdapter;
    private Cursor cursor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        ArrayAdapter<String> adapterC = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, CITIES);
        lvChooser.setAdapter(adapterC);
        lvChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myDBAdapter.setLast(position);
                Intent intent = new Intent(view.getContext(), WeatherActivity.class);
                intent.putExtra(CODE, CITIES[position]);
                intent.putExtra(CODE_ENG, CITIES_ENG[position]);
                startActivity(intent);
            }
        });
        cursor = myDBAdapter.getLast();
        cursor.moveToFirst();
        int last = cursor.getInt(0);
        if (last != -1) {
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra(CODE, CITIES[last]);
            intent.putExtra(CODE_ENG, CITIES_ENG[last]);
            startActivity(intent);
        }
    }
}
