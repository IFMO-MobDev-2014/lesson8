package ru.ifmo.md.lesson8;

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
import android.util.Log;
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

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private boolean selectedDefault = false;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

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
                        WeatherContentProvider.URI_CITY_DIR,
                        new String[]{WeatherDatabase.Structure.COLUMN_NAME, WeatherDatabase.Structure.COLUMN_URL, WeatherDatabase.Structure.COLUMN_ID},
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
                ((CursorAdapter) mDrawerListView.getAdapter()).swapCursor(data);
                if(!selectedDefault) {
                    data.moveToFirst();
                    if(!data.isAfterLast())
                        mDrawerListView.post(new Runnable() {
                            @Override
                            public void run() {
                                selectItem(data.getInt(1), 0, data.getString(0));
                            }
                        });

                    selectedDefault = true;
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
        selectedDefault = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cr = (Cursor) parent.getAdapter().getItem(position);
                cr.moveToFirst();
                cr.move(position);
                selectItem(cr.getInt(1), position, cr.getString(0));
            }
        });

        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.delete, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Cursor cr = (Cursor) adapterView.getAdapter().getItem(i);
                        cr.moveToFirst();
                        cr.move(i);
                        int cityId = cr.getInt(1);

                        int id = item.getItemId();
                        if(id == R.id.action_delete) {
                            getActivity().getContentResolver().delete(WeatherContentProvider.URI_CITY_DIR, WeatherDatabase.Structure.COLUMN_URL + " = " + cityId, null);
                            getActivity().getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIR, null);
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });
        mDrawerListView.setAdapter(new CityListCursorAdapter(getActivity(), null, false));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        if(drawerLayout != null) {

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

            mDrawerToggle = new ActionBarDrawerToggle(
                    getActivity(),                    /* host Activity */
                    mDrawerLayout,                    /* DrawerLayout object */
                    R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                    R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                    R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
            ) {
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
                        // The user manually opened the drawer; store this flag to prevent auto-showing
                        // the navigation drawer automatically in the future.
                        mUserLearnedDrawer = true;
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(getActivity());
                        sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    }

                    getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                }
            };

            // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
            // per the navigation drawer design guidelines.
            if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
                mDrawerLayout.openDrawer(mFragmentContainerView);
            }

            // Defer code dependent on restoration of previous instance state.
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });

            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }



        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    private void selectItem(int id, int position, String name) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(name, id);
        }
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
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout == null || isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle == null || mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(String name, int id);
    }

    private class CityListCursorAdapter extends CursorAdapter {


        public CityListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
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
}
