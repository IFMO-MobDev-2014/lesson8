package ru.ifmo.md.weather;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import ru.ifmo.md.weather.db.WeatherContentProvider;

/**
 * Created by Kirill on 15.12.2014.
 */
public class SettingsActivity extends Activity {

    Cursor settings = null;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.settings);

        settings = getContentResolver()
                .query(Uri.withAppendedPath(WeatherContentProvider.CONTENT_URI_SETTINGS, "0"),
                        null, null, null, null);
        CheckBox checkBox = (CheckBox)findViewById(R.id.auto_update_checkbox);
        
        EditText interval = (EditText)findViewById(R.id.update_interval);
        TextView lastUpdate = (TextView)findViewById(R.id.last_update);

    }
}
