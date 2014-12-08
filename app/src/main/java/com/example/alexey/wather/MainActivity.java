package com.example.alexey.wather;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;



public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public static CharSequence mTitle;
    public static Cursor gcursor=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ContentValues cv = new ContentValues();
        if(!provider.isTableExists(provider.db,"main"))
        {
            provider.add_table("main");
            cv.put(provider.DATE,"Nevinnomussk, Rassia");
            cv.put(provider.DAY, "http://api.wunderground.com/api/0c4d0979336b962f/forecast10day/q/zmw:00000.1.37036.json");
            cv.put("mqin", "main");
            getContentResolver().insert(provider.CONTENT_URI, cv);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {

        ListView lv=(ListView) findViewById(R.id.listView);
        Cursor c = (Cursor) mNavigationDrawerFragment.mDrawerListView.getAdapter().getItem(number - 1);

        //Cursor cursor=getContentResolver().query(provider.CONTENT_URI, null, provider.DATE + " = " + "'"++"'", null, "main");
        //cursor.moveToFirst();
        //String str;
        //str=cursor.getString(cursor.getColumnIndex(provider.DATE));
        mTitle=c.getString(c.getColumnIndex(provider.DATE));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_example) {
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements AppReceiver.Receiver {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        // String mTitle=null;
        int g=0;
        String prevmTitle=null;
         ProgressBar mProgress=null;
        ListView listView;
        static AppReceiver mReceiver;
        public static int number=0;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            number=sectionNumber;
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        int iy=4;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)  {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);


    //                Cursor cursor=getActivity().getContentResolver().query(provider.CONTENT_URI, null, provider._ID + " = " + Integer.toString(number), null, "main");
  //                    cursor.moveToFirst();
//            mTitle=cursor.getString(cursor.getColumnIndex(provider.DATE));
         //   mTitle=getActivity().getTitle();



            if (prevmTitle==null||!prevmTitle.equals(mTitle.toString())) {
                TabHost tabs = (TabHost) rootView.findViewById(R.id.tabHost);
                tabs.setup();
                TabHost.TabSpec spec = tabs.newTabSpec("tag1");

                spec.setContent(R.id.tab1);
                spec.setIndicator("Today");
                tabs.addTab(spec);

                spec = tabs.newTabSpec("tag2");
                spec.setContent(R.id.tab2);
                spec.setIndicator("Forecast");
                tabs.addTab(spec);
                tabs.setCurrentTab(0);
                if (!provider.isTableExists(provider.db, mTitle.toString())) {
                    provider.add_table(mTitle.toString());
                    String s[]={mTitle.toString()};
                    getActivity().getContentResolver().delete(provider.CONTENT_URI, null, s);
                    final Intent intent = new Intent("SOME_COMMAND_ACTION", null, getActivity(), IServise.class);
                    mReceiver = new AppReceiver(new Handler());
                    mReceiver.setReceiver(this);
                    Cursor cursor1=getActivity().getContentResolver().query(provider.CONTENT_URI,null,provider.DATE + " = " + "'" + mTitle.toString() + "'",null,"main");
                    intent.putExtra("1", mReceiver);
                    intent.putExtra("task", mTitle.toString());
                    cursor1.moveToFirst();
                    String link=cursor1.getString(cursor1.getColumnIndex(provider.DAY));
                    intent.putExtra("link",link);
                    getActivity().startService(intent);
               } else {
                    mReceiver = new AppReceiver(new Handler());
                    mReceiver.setReceiver(this);
                    final Intent intent = new Intent("SOME_COMMAND_ACTION", null, getActivity(), IServise.class);
                    intent.putExtra("1", mReceiver).putExtra("task", "dont_w");
                    getActivity().startService(intent);

                    mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar2);
                }
            }
            prevmTitle = mTitle.toString();
            return rootView;
        }



        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle data) {
            Log.i("Result", "got");
            mProgress=(ProgressBar) getActivity().findViewById(R.id.progressBar2);
            switch (resultCode) {
                case 2:
                    mProgress.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    mProgress.setVisibility(View.INVISIBLE);

                   showData();

                    break;
            }
        }
        
        void showData(){

            Cursor cursor_2 = getActivity().getContentResolver().query(provider.CONTENT_URI, null, null, null, mTitle.toString());
            cursor_2.moveToFirst();
            Activity x=getActivity();
            ImageView im = (ImageView) getActivity().findViewById(R.id.imageView);
            im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.FIVE_PATH))));
            im = (ImageView) getActivity().findViewById(R.id.imageView2);
            im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.SIX_PATH))));
            TextView tv = (TextView) getActivity().findViewById(R.id.textView3);
            tv.setText("Today is " + cursor_2.getString(cursor_2.getColumnIndex(provider.DATE)));
            tv = (TextView) getActivity().findViewById(R.id.description);
            tv.setText("Day forecast");
            tv = (TextView) getActivity().findViewById(R.id.textView);
            tv.setText(cursor_2.getString(cursor_2.getColumnIndex(provider.DAY)));
            tv = (TextView) getActivity().findViewById(R.id.textView1);
            tv.setText("Nigth forecast");
            tv = (TextView) getActivity().findViewById(R.id.textView2);
            tv.setText(cursor_2.getString(cursor_2.getColumnIndex(provider.NIGHT)));

            ListView lv = (ListView) getActivity().findViewById(R.id.listView);
            Cursor cursor_1 = getActivity().getContentResolver().query(provider.CONTENT_URI, null, null, null, mTitle.toString());
            String[] from=new String[]{provider.DATE, provider.TEMPERATURE, provider.DAY};
            int[] to=new int[]{R.id.textView6, R.id.textView7, R.id.textView8};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.prog_bar, cursor_1,from,to);
            lv.setAdapter(adapter);

            mProgress = (ProgressBar) getActivity().findViewById(R.id.progressBar2);
            mProgress.setVisibility(View.INVISIBLE);

        }
    }
}
