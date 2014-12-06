package ru.ifmo.md.lesson8;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class CitiesActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_cities);
        if (f == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CitiesFragment())
                            .commit();
        }
    }
}
