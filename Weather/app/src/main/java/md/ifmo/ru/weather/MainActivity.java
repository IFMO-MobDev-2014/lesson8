package md.ifmo.ru.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends Activity {
    //public static final String[] CITIES = {"Москва", "Санкт-Петербург", "Оттава"};
    public static final String CODE = "city";
    private DBAdapter myDBAdapter;
    private Cursor cursor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        cursor = myDBAdapter.fetchCities();
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        registerForContextMenu(lvChooser);
        lvChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                int cityID = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID));
                myDBAdapter.setLast(cityID);
                String city = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_CITY));
                Intent intent = new Intent(view.getContext(), WeatherActivity.class);
                intent.putExtra(CODE, city);
                startActivity(intent);
            }
        });
        showCities();
        Cursor cursorLast = myDBAdapter.getLast();
        cursorLast.moveToFirst();
        int last = cursorLast.getInt(0);
        if (last != -1) {
            cursorLast = myDBAdapter.fetchCity(last);
            if (cursorLast.moveToFirst()) {
                String city = cursorLast.getString(cursorLast.getColumnIndexOrThrow(DBAdapter.KEY_CITY));
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.putExtra(CODE, city);
                startActivity(intent);
            }
        }
    }

    private void showCities() {
        cursor = myDBAdapter.fetchCities();
        String[] from = new String[]{DBAdapter.KEY_CITY};
        int[] to = new int[]{R.id.city_row_name};
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        SimpleCursorAdapter cities = new SimpleCursorAdapter(this, R.layout.city_row, cursor, from, to);
        lvChooser.setAdapter(cities);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCities();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.lvChooser) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            MenuInflater inflater = getMenuInflater();
            cursor.moveToPosition(info.position);
            menu.setHeaderTitle(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_CITY)));
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        cursor.moveToPosition(info.position);
        int cityID = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID));
        switch (item.getItemId()) {
            case R.id.remove:
                myDBAdapter.deleteCity(cityID);
                showCities();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void clickPlus(View v) {
        Intent intent = new Intent(this, AddCityActivity.class);
        startActivity(intent);
    }
}
