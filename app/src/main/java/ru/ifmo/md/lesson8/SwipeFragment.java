package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class SwipeFragment extends Fragment
        implements android.support.v4.app.LoaderManager.LoaderCallbacks<Object> {

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

    private final int AUTOCOMPLETE_LOADER = 1;

    public DrawerLayout swipeLayout;
    private ListView swipeListView;
    private View mFragmentContainerView;

    public int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public CitiesAdapter adapter;

    private ArrayList<String> suggestions = new ArrayList<>();
    AutoCompleteTextView input;
    AlertDialog chooseDialog;

    ArrayList<Pair<String, String>> cities;
    String zmw = null;

    private MainActivity parent;

    public SwipeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        swipeListView = (ListView) inflater.inflate(
                R.layout.fragment_swipe, container, false);
        adapter = new CitiesAdapter(getActionBar().getThemedContext(), this);
        swipeListView.setAdapter(adapter);

        return swipeListView;
    }

    public boolean isDrawerOpen() {
        return swipeLayout != null && swipeLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        swipeLayout = drawerLayout;

        swipeLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                swipeLayout,
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

                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            swipeLayout.openDrawer(mFragmentContainerView);
        }

        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        swipeLayout.setDrawerListener(mDrawerToggle);
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (swipeListView != null) {
            swipeListView.setItemChecked(position, true);
        }
        if (swipeLayout != null) {
            swipeLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (NavigationDrawerCallbacks) activity;
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
        if (swipeLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void displayFragment(int position) {
        mCurrentSelectedPosition = position;
        mCallbacks.onNavigationDrawerItemSelected(position);
    }

    void addCity() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(getString(R.string.add_dialog));

        suggestions.clear();
        zmw = null;
        input = new AutoCompleteTextView(getActivity());
        input.setThreshold(1);
        input.setDropDownHeight(300);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                chooseDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                seekForCity(s.toString());
            }
        });

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                adapter.addCity(input.getText().toString(), zmw);
                adapter.notifyDataSetChanged();
                suggestions.clear();
                mCurrentSelectedPosition = adapter.getCount() - 1;
                mCallbacks.onNavigationDrawerItemSelected(adapter.getCount() - 1);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        chooseDialog = alert.create();
        chooseDialog.show();
        chooseDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_add:
                addCity();
                return true;
            case R.id.action_refresh:
                if (mCurrentSelectedPosition < adapter.getCount()) {
                    Intent loadForecast = new Intent(getActivity(), WeatherIntentService.class);
                    loadForecast.putExtra(MyDatabase.NAME, adapter.citiesName.get(mCurrentSelectedPosition));
                    loadForecast.putExtra(MyDatabase.CODE, adapter.citiesCode.get(mCurrentSelectedPosition));
                    loadForecast.putExtra("force", true);
                    getActivity().startService(loadForecast);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    private boolean isSuggested(String s) {
        for (Pair<String, String> city: cities) {
            if (s.equals(city.first)) {
                zmw = city.second;
                chooseDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                return true;
            }
        }
        return false;
    }

    private void seekForCity(String query) {
        if ((cities != null && isSuggested(query)) || query.length() == 0) {
            return;
        }
        Bundle b = new Bundle();
        b.putString("query", query);
        getLoaderManager().destroyLoader(AUTOCOMPLETE_LOADER);
        getLoaderManager().initLoader(AUTOCOMPLETE_LOADER, b, this);
    }

    @Override
    public android.support.v4.content.Loader<Object> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.Loader<Object> loader = null;
        switch (id) {
            case AUTOCOMPLETE_LOADER:
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add("Loading city list...");
                input.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line,tmp));
                input.showDropDown();
                loader = new AutoСompleteLoader(getActivity(), args.getString("query"));
        }
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Object> loader, Object data) {
        if (data == null || ((ArrayList<Pair<String, String>>) data).isEmpty()) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add("No cities");
            input.setAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, tmp));
            input.showDropDown();
            return;
        }

        ArrayList<String> tmp = new ArrayList<>();
        for (Pair<String, String> city: (ArrayList<Pair<String, String>>) data)
            tmp.add(city.first);
        input.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, tmp));
        input.showDropDown();

        cities = (ArrayList<Pair<String, String>>) data;
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Object> loader) {
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
