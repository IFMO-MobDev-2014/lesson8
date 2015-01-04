package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ItemListActivity extends ActionBarActivity
        implements ItemListFragment.Callbacks {

    private boolean mTwoPane;
    private DrawerLayout myDrawerLayout;
    private ActionBarDrawerToggle myDrawerToggle;

    // navigation drawer title
    private CharSequence myDrawerTitle;
    // used to store app title
    private CharSequence myTitle;

    private String[] viewsNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        ((ItemListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_list))
                .setActivateOnItemClick(true);

//        if (findViewById(R.id.drawer_layout) == null)
//            mTwoPane = true;

        myTitle = getTitle();
        myDrawerTitle = getResources().getString(R.string.menu);
        viewsNames = getResources().getStringArray(R.array.views_array);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (myDrawerLayout != null) {
            // enabling action bar app icon and behaving it as toggle button
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
                    R.drawable.ic_drawer, //nav menu toggle icon
                    R.string.toggle_open
            ) {
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(myTitle);
                    // calling onPrepareOptionsMenu() to show action bar icons
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(myDrawerTitle);
                    // calling onPrepareOptionsMenu() to hide action bar icons
                    invalidateOptionsMenu();
                }
            };
            myDrawerLayout.setDrawerListener(myDrawerToggle);
        }


        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

/*            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if navigation drawer is opened, hide the action items
//        boolean drawerOpen = myDrawerLayout.isDrawerOpen(myDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle = title;
        getActionBar().setTitle(myTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        myDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        myDrawerToggle.onConfigurationChanged(newConfig);
    }

}
