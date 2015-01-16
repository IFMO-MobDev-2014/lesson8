package ru.eugene.weather;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import ru.eugene.weather.ContentProviders.WeatherProvider;
import ru.eugene.weather.database.CityDataSource;
import ru.eugene.weather.database.CityItem;
import ru.eugene.weather.database.WeatherInfoDataSource;


public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    ListView listView;
    Button insert;
    DelayAutoCompleteTextView city;
    SimpleCursorAdapter adapter;
    private Context context;
    private int posOfSelectedEl;
    private int CONTEXT_MENU_DELETE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("LOG", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
        listView = getListView();
        String[] fromColumns = new String[]{CityDataSource.COLUMN_CITY};
        int[] toControlIds = new int[]{android.R.id.text1};
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null, fromColumns, toControlIds, 0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String text = ((TextView) view).getText().toString();
                view.setTag(text);
                String temp[] = text.split(",");
                ((TextView) view).setText(temp[0]);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
        };
        listView.setAdapter(adapter);
        insert = (Button) findViewById(R.id.insert);
        city = (DelayAutoCompleteTextView) findViewById(R.id.city);
        context = this;

        city.setThreshold(1);
        city.setAutoCompleteDelay(100);
        city.setAdapter(new CityAutoCompleteAdapter(context));
        city.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
        city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CityItem cityItem = (CityItem) adapterView.getItemAtPosition(position);
                city.setText(cityItem.getCity());
                city.setTag(cityItem.getLink());
            }
        });

        city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                city.setTag(null);
            }
        });


        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailWeather = new Intent(context, DetailWeather.class);
                Cursor cities = adapter.getCursor();
                if (cities.moveToPosition(position)) {
                    String link = cities.getString(cities.getColumnIndex(CityDataSource.COLUMN_LINK));
                    String city = (String) view.getTag();
                    int idCity = cities.getInt(cities.getColumnIndex(CityDataSource.COLUMN_ID));
                    detailWeather.putExtra(CityDataSource.COLUMN_LINK, link);
                    detailWeather.putExtra(CityDataSource.COLUMN_ID, idCity);
                    detailWeather.putExtra(CityDataSource.COLUMN_CITY, city);
                    startActivity(detailWeather);
                }
            }
        });

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameCity = city.getText().toString();
                String linkCity = (String) city.getTag();
                if (linkCity == null) {
                    Toast.makeText(context, "Please choose city from drop-down list!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues cv = new ContentValues();
                cv.put(CityDataSource.COLUMN_CITY, nameCity);
                cv.put(CityDataSource.COLUMN_LINK, linkCity);
                getContentResolver().insert(WeatherProvider.CONTENT_URI_CITY, cv);

                city.setText("");

                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                registerForContextMenu(listView);
                posOfSelectedEl = position;
                openContextMenu(listView);
                return true;
            }
        });

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_DELETE, 0, "Delete!");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CONTEXT_MENU_DELETE) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete feed")
                    .setMessage("Are you sure you want to delete this city?")
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Cursor cities = adapter.getCursor();
                            int idCity = cities.getInt(cities.getColumnIndex(CityDataSource.COLUMN_ID));
                            Log.i("LOG", "positive button: " + idCity);
                            context.getContentResolver().delete(WeatherProvider.CONTENT_URI_CITY,
                                    CityDataSource.COLUMN_ID + "=?", new String[] {"" + idCity});
                            context.getContentResolver().delete(WeatherProvider.CONTENT_URI_WEATHER_INFO,
                                    WeatherInfoDataSource.COLUMN_ID_CITY + "=?", new String[] {"" + idCity});
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = CityDataSource.getProjection();
        return new CursorLoader(this, WeatherProvider.CONTENT_URI_CITY, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("LOG", "load finish");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
