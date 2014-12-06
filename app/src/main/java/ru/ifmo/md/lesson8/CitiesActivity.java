package ru.ifmo.md.lesson8;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add_city) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new AddCityFragment()).commit();
            
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_cities, menu);
        return true;
    }
}
