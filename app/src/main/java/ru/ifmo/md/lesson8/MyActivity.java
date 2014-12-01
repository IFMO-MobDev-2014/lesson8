package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import ru.ifmo.md.lesson8.database.DataProvider;
import ru.ifmo.md.lesson8.database.DatabaseHelper;
import ru.ifmo.md.lesson8.fragments.CityListFragment;
import ru.ifmo.md.lesson8.fragments.WhetherFragment;

public class MyActivity extends Activity
        implements CityListFragment.NavigationDrawerCallbacks {

    private CityListFragment cityListFragment;
    private DataProvider dp;
    private DatabaseHelper db;

    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this, "my.db", null, 1);
        dp = new DataProvider(db);

        setContentView(R.layout.my_activity);

        cityListFragment = (CityListFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        dp.cityListFragment = cityListFragment;
        cityListFragment.dp = dp;
        cityListFragment.addAdapterAndListener();

        mTitle = getTitle();

        // Set up the drawer.
        cityListFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!cityListFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, WhetherFragment.createInstance(this, dp, position))
                .commit();
    }

    public void onSectionAttached(int number) {
        mTitle = dp.getCityInfoByPosition(number + 1).name;
    }
}
