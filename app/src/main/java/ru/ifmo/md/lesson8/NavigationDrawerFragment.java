package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POS = "selected_navigation_drawer_position";
    private static final String USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks navigationDrawerCallbacks;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView listView;
    private View fragmentContainerView;
    private boolean selectedDefault = false;
    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, new MyLoaderManager());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userLearnedDrawer = sharedPreferences.getBoolean(USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POS);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                cursor.moveToFirst();
                cursor.move(position);
                selectItem(cursor.getInt(1), position, cursor.getString(0));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int index, long l) {
                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.delete, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Cursor cursor = (Cursor) adapterView.getAdapter().getItem(index);
                        cursor.moveToFirst();
                        cursor.move(index);
                        int cityId = cursor.getInt(1);
                        int id = item.getItemId();
                        if (id == R.id.action_delete) {
                            getActivity().getContentResolver().delete(DatabaseContentProvider.URI_CITY_DIR, CitiesTable.URL + " = " + cityId, null);
                            getActivity().getContentResolver().notifyChange(DatabaseContentProvider.URI_CITY_DIR, null);
                            return true;
                        } else if (id == R.id.action_set_default) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                            editor.putInt(MainActivity.CITY_DEFAULT, cityId);
                            if (cityId != 0) {
                                editor.putBoolean(MainActivity.CITY_DEFAULT_SELECTED, true);
                            } else {
                                editor.putBoolean(MainActivity.CITY_DEFAULT_SELECTED, false);
                            }
                            editor.apply();
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });
        listView.setAdapter(new CityListCursorAdapter(getActivity(), null, false));
        listView.setItemChecked(currentSelectedPosition, true);
        return listView;
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        ActionBar actionBar = getActivity().getActionBar();
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
                if (!userLearnedDrawer) {
                    userLearnedDrawer = true;
                    SharedPreferences sharedPreference = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sharedPreference.edit().putBoolean(USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        if (!userLearnedDrawer && !fromSavedInstanceState) {
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
        currentSelectedPosition = position;
        if (listView != null) {
            listView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (navigationDrawerCallbacks != null) {
            navigationDrawerCallbacks.onNavigationDrawerItemSelected(name, id);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            navigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
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
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationDrawerCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POS, currentSelectedPosition);
    }


    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
        getActivity().openOptionsMenu();
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String name, int id);
    }

    private class MyLoaderManager implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getActivity().getApplicationContext(),
                    DatabaseContentProvider.URI_CITY_DIR,
                    new String[]{CitiesTable.NAME, CitiesTable.URL, CitiesTable.ID},
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
            ((CursorAdapter) listView.getAdapter()).swapCursor(data);
            if (!selectedDefault) {
                data.moveToFirst();
                int id = data.getInt(1);
                int defaultId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(MainActivity.CITY_DEFAULT, -1);
                String name = data.getString(0);
                while (id != defaultId && defaultId != -1 && !data.isAfterLast()) {
                    data.moveToNext();
                    id = data.getInt(1);
                    name = data.getString(0);
                }
                final String finalName = name;
                final int finalId = id;
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            selectItem(finalId, 0, finalName);
                        } catch (CursorIndexOutOfBoundsException e) {
                        }
                    }
                });
                selectedDefault = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    class CityListCursorAdapter extends CursorAdapter {
        public CityListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            TextView textView = new TextView(context);
            textView.setTextSize(25.0f);
            textView.setText(cursor.getString(0));
            return textView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(0));
        }
    }
}
