package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ListView;

public class NavigationDrawerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final City EMPTY_CITY = new City(-1, "Empty", 0);
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private final int REQUEST_ADD_CITY = 0;

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private Handler selectHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int position = message.what;
            citiesAdapter.setSelected(position);
            if (mDrawerListView != null) {
                mDrawerListView.setItemChecked(position, true);
            }

            if (mCallbacks != null) {
                mCallbacks.onNavigationDrawerItemSelected(position, citiesAdapter.getItem(position));
            }
            return true;
        }
    });
    private View mFragmentContainerView;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private CitiesAdapter citiesAdapter = new CitiesAdapter();

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        citiesAdapter.add(EMPTY_CITY);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            citiesAdapter.setSelected(savedInstanceState.getInt(STATE_SELECTED_POSITION));
            mFromSavedInstanceState = true;
        }

        getLoaderManager().restartLoader(345738, null, this);
        if (citiesAdapter.getSelected() >= citiesAdapter.getCount()) {
            citiesAdapter.setSelected(0);
        }
        selectHandler.sendEmptyMessage(citiesAdapter.getSelected());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView = (ListView) v.findViewById(R.id.citiesListView);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                citiesAdapter.setSelected(position);
                selectHandler.sendEmptyMessage(position);
            }
        });
        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCallbacks != null) {
                    mCallbacks.onNavigationDrawerItemLongSelected(position, citiesAdapter.getItem(position));
                    getLoaderManager().restartLoader(23984, null, NavigationDrawerFragment.this);
                    return true;
                }
                return false;
            }
        });
        Button addCityButton = (Button) v.findViewById(R.id.addCityButton);
        addCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AddCity.class), REQUEST_ADD_CITY);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_CITY) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(AddCity.CITY_ADDED_NAME);
                getActivity().getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URI, new City(-1, name, 0).getContentValues());
                getLoaderManager().restartLoader(0, null, this);
                citiesAdapter.setSelected(citiesAdapter.getCount());
            }
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, citiesAdapter.getSelected());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), WeatherContentProvider.CITY_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        citiesAdapter.clear();
        while (cursor.moveToNext()) {
            City c = WeatherDatabaseHelper.CityCursor.getCity(cursor);
            citiesAdapter.add(c);
        }
        if (citiesAdapter.getCount() == 0) {
            citiesAdapter.add(EMPTY_CITY);
        }
        mDrawerListView.setAdapter(citiesAdapter);
        mDrawerListView.setItemChecked(citiesAdapter.getSelected(), true);
        if (citiesAdapter.getSelected() >= citiesAdapter.getCount()) {
            citiesAdapter.setSelected(0);
        }
        selectHandler.sendEmptyMessage(citiesAdapter.getSelected());
        citiesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position, City city);
        void onNavigationDrawerItemLongSelected(int position, City city);
    }
}
