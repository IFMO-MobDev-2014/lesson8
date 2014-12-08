package com.example.alexey.wather;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements AppReceiver.Receiver {

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
    public ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public static String idl;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //    TextView tv =(TextView)view.findViewById(android.R.id.text1);
              //  idl=tv.getText().toString();
                selectItem(position);
            }
        });
        Cursor cursor=getActivity().getContentResolver().query(provider.CONTENT_URI,null,null,null,"main");
        SimpleCursorAdapter adapter;
        adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                cursor,
                new String[]{provider.DATE},
                new int[]{android.R.id.text1});
        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv=(TextView)view.findViewById(android.R.id.text1);
                String name=tv.getText().toString();
                getActivity().getContentResolver().delete(provider.CONTENT_URI,provider.DATE + " = " + "'"+name+"'",new String[]{"main"});
                getActivity().getContentResolver().delete(provider.CONTENT_URI,null,new String[]{name});
                Boolean stg=provider.isTableExists(provider.db,name);
                return false;
            }
        });
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        SimpleCursorAdapter adapter=(SimpleCursorAdapter)mDrawerListView.getAdapter();
        adapter.notifyDataSetChanged();
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
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

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
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
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    AlertDialog.Builder alert;
    AppReceiver dReceiver;
    ProgressBar dProgress;
    String value;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_settings)
        {
             alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Enter city name!");
            final EditText input = new EditText(getActivity());
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    value = input.getText().toString();
                    value=value.replaceAll("-","_").replaceAll(" ","_");
                    if(value.equals("")) return;
                    Intent intent = new Intent(getActivity(),IServiseAddCity.class);
                    dReceiver=new AppReceiver(new Handler());
                    dReceiver.setReceiver(NavigationDrawerFragment.this);
                    intent.putExtra("name",value).putExtra("1",dReceiver);
                    getActivity().startService(intent);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
            return true;
        }

        if (item.getItemId() == R.id.action_example) {
            String s[] = {MainActivity.mTitle.toString()};
            getActivity().getContentResolver().delete(provider.CONTENT_URI, null, s);
            final Intent intent = new Intent("SOME_COMMAND_ACTION", null, getActivity(), IServise.class);

            intent.putExtra("1", MainActivity.PlaceholderFragment.mReceiver).putExtra("task", MainActivity
                    .mTitle.toString());

            Cursor cursor1=getActivity().getContentResolver().query(provider.CONTENT_URI,null,provider.DATE + " = " + "'" + MainActivity.mTitle.toString() + "'",null,"main");
            cursor1.moveToFirst();
            String link=cursor1.getString(cursor1.getColumnIndex(provider.DAY));
            intent.putExtra("link",link);
            getActivity().startService(intent);

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
        return getActivity().getActionBar();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        ProgressBar pb=(ProgressBar)getActivity().findViewById(R.id.progressBar2);
        switch (resultCode) {
            case 2:
                pb.setVisibility(View.VISIBLE);
                break;
            case 5:
                Bundle bundle=data.getBundle("ans");
                String result=bundle.getString("result");
                String message=null;
                alert=new AlertDialog.Builder(getActivity());
                if(result.equals("error")){
                    message="Wrong name, try again.";
                }
                if(result.equals("one")){
                    ContentValues cv=new ContentValues();
                    cv.put(provider.DATE,value);
                    cv.put(provider.DAY,"http://api.wunderground.com/api/0c4d0979336b962f/forecast10day/q/"+value+".json");
                    cv.put("mqin","main");
                    getActivity().getContentResolver().insert(provider.CONTENT_URI,cv);
                    message="City was added.";
                }
                if (result.equals("many")){
                    ListView lv=new ListView(getActivity());
                    final ArrayList<String> al=bundle.getStringArrayList("list");
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getActivity(),
                            android.R.layout.simple_list_item_1,
                            al);
                    final ArrayList<String> links=bundle.getStringArrayList("links");
                    lv.setAdapter(arrayAdapter);
                    lv.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String str = al.get(i);
                            Cursor cursor=getActivity().getContentResolver().query(provider.CONTENT_URI, null, provider.DATE + " = " + "'" + str + "'",null,"main");
                            cursor.moveToFirst();
                            if(cursor.moveToNext())
                            {return;}
                            else{
                                ContentValues cv=new ContentValues();
                                cv.put(provider.DATE,al.get(i));
                                cv.put(provider.DAY,"http://api.wunderground.com/api/0c4d0979336b962f/forecast10day"+links.get(i)+".json");
                                cv.put("mqin","main");
                            getActivity().getContentResolver().insert(provider.CONTENT_URI,cv);}
                        }
                    });
                    alert.setView(lv);
                    message="Choose your city.";
                }
                alert.setTitle("Done");
                alert.setMessage(message);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                pb.setVisibility(View.INVISIBLE);
                alert.show();
                break;
        }
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
