package com.example.lesson8;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getLoaderManager().initLoader(0, null, this);
        Button button = (Button) findViewById(R.id.button);
        ListView listView = (ListView) findViewById(R.id.listView);
        WeatherDB weatherDB = new WeatherDB(this);
        weatherDB.open();
        Cursor cursor = weatherDB.getAllDataCursor();
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(this, R.layout.adapter, null, new String[]{ WeatherDB.COLUMN_CITY }, new int[]{ R.id.city});
        listView.setAdapter(adapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddCityActivity.class);
                startActivity(intent);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentCity = ((TextView) view.findViewById(R.id.city)).getText().toString();
                Intent intent = new Intent(view.getContext(), WeatherActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                getContentResolver().delete(Uri.parse("content://com.example.lesson8.weathers/weather"), "" + id, null);
                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, Uri.parse("content://com.example.lesson8.weathers/weather"), null, null, null, null);
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
