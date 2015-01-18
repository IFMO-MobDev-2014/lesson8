package year2013.ifmo.weather;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MenuActivity extends ActionBarActivity implements ForecastDialogFragment.NoticeDialogListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter adapter;
    private ListView listView;
    Cursor oc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        String from[] = {Forecast.CITY_NAME};
        int to[] = { android.R.id.text1};
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, from, to);

        listView = (ListView) findViewById(R.id.city_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listener);
        listView.setOnItemLongClickListener(longListener);

        getLoaderManager().initLoader(0, null, this);

    }

    private AdapterView.OnItemLongClickListener longListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            oc = ((Cursor) adapter.getItem(position));
            //CITY = o.getString(Forecast.CITY_COLUMN);
            DialogFragment dialogFragment = new ForecastDialogFragment();
            dialogFragment.show(getFragmentManager(), "city");
            return true;
        }
    };



    @Override
    public void onDialogPositiveClick() {
        Uri uri = ContentUris.withAppendedId(Forecast.CONTENT_URI,
                oc.getLong(Forecast.ID_COLUMN));
        getContentResolver().delete(uri, null, null);
        adapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor o = ((Cursor) adapter.getItem(position));

            String city = o.getString(Forecast.CITY_COLUMN);
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_CITY_NAME, city);
            startActivity(intent);
        }
    };

    public void enterCity (View view) {
        EditText cityName = (EditText) findViewById(R.id.enter_city);
        String city = cityName.getText().toString();
        if (city.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Enter City Name",
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            ContentValues cv = new ContentValues();
            cv.put(Forecast.CITY_NAME, city);
            cv.put(Forecast.CURRENT_FORECAST, "");
            cv.put(Forecast.DAYS_FORECAST, "");
            cityName.setText("");
            getContentResolver().insert(Forecast.CONTENT_URI, cv);
        }
    }

    static final String[] SUMMARY_PROJECTION = new String[] {
            Forecast._ID,
            Forecast.CITY_NAME,
            Forecast.CURRENT_FORECAST,
            Forecast.DAYS_FORECAST
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Forecast.CONTENT_URI;

        return new CursorLoader(getBaseContext(), baseUri,
                SUMMARY_PROJECTION, null, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
