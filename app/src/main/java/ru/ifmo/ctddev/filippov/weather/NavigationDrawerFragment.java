package ru.ifmo.ctddev.filippov.weather;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Created by Dima_2 on 01.04.2015.
 */
public class NavigationDrawerFragment extends Fragment {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_DRAWER = "navigation_drawer_learned";
    private NavigationDrawerCallbacks callbacks;
    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;

    private boolean selectedDefault = false;

    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean isDrawerDefined;

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String name, int id);
    }

    private class CityListCursorAdapter extends CursorAdapter {
        public CityListCursorAdapter(Context context, Cursor c, boolean autoUpdate) {
            super(context, c, autoUpdate);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            TextView w = new TextView(context);
            w.setTextSize(30.0f);
            w.setText(cursor.getString(0));
            return w;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(0));
        }
    }

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        getActivity().getApplicationContext(),
                        WeatherContentProvider.URI_CITY_DIRECTORY,
                        new String[]{WeatherDatabase.COLUMN_NAME, WeatherDatabase.COLUMN_URL, WeatherDatabase.COLUMN_ID},
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
                ((CursorAdapter) drawerListView.getAdapter()).swapCursor(cursor);
                if(!selectedDefault) {
                    cursor.moveToFirst();
                    drawerListView.post(new Runnable() {
                        @Override
                        public void run() {
                            selectItem(cursor.getInt(1), 0, cursor.getString(0));
                        }
                    });
                    selectedDefault = true;
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isDrawerDefined = sp.getBoolean(PREF_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }
        selectedDefault = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        drawerListView = (ListView) layoutInflater.inflate(
                R.layout.fragment_navigation_drawer,
                container,
                false
        );

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                cursor.moveToFirst();
                cursor.move(position);
                selectItem(cursor.getInt(1), position, cursor.getString(0));
            }
        });

        drawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.delete, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Cursor cursor = (Cursor) adapterView.getAdapter().getItem(i);
                        cursor.moveToFirst();
                        cursor.move(i);
                        int cityId = cursor.getInt(1);

                        int id = item.getItemId();
                        if (id == R.id.action_delete) {
                            getActivity().getContentResolver().delete(
                                    WeatherContentProvider.URI_CITY_DIRECTORY,
                                    WeatherDatabase.COLUMN_URL + " = " + cityId,
                                    null
                            );
                            getActivity().getContentResolver().notifyChange(
                                    WeatherContentProvider.URI_CITY_DIRECTORY,
                                    null
                            );
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        drawerListView.setAdapter(new CityListCursorAdapter(getActivity(), null, false));
        drawerListView.setItemChecked(currentSelectedPosition, true);
        return drawerListView;
    }

    public boolean isDrawerOpen() {
        return (drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView));
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar == null) {
            throw new AssertionError("An error occurred while creating Action Bar - null was returned");
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                NavigationDrawerFragment.this.drawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!isDrawerDefined) {
                    isDrawerDefined = true;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPreferences.edit().putBoolean(PREF_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        if (!isDrawerDefined && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(fragmentContainerView);
        }
        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    private void selectItem(int id, int position, String name) {
        Button refreshButton = (Button)getActivity().findViewById(R.id.refresh_button);
        refreshButton.setVisibility(View.VISIBLE);

        currentSelectedPosition = position;
        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(name, id);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (NavigationDrawerCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (drawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar == null) {
            throw new AssertionError("An error occurred while creating Action Bar - null was returned");
        }
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }
}
