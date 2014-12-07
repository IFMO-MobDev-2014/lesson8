package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;


public class CitiesActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_cities);
        if (f == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CitiesFragment()).commit();
        }
    }
}
