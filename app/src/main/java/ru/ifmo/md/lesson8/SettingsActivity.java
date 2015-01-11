package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by sergey on 10.01.15.
 */
public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
