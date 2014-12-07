package ru.ifmo.md.lesson8;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class AddCityActivity extends FragmentActivity {
    public static String ADDED_NAME = "added_name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcities);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
        if (f == null) {
            Fragment fragment = new AddCityFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }
}
