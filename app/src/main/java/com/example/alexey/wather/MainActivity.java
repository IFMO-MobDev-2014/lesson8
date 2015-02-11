package com.example.alexey.wather;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity
        implements AppReceiver.Receiver,NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public static CharSequence mTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiver = new AppReceiver(new Handler());
        mReceiver.setReceiver(this);
        setContentView(R.layout.activity_wether);
        Cursor cursor = getContentResolver().query(provider.CONTENT_URI, null, provider.HESH + " = '" + ImageConverter.hash("Nevinnomussk") + "' ", null, null);
        /*if (!cursor.moveToFirst()){
            ContentValues cv = new ContentValues();
            cv.put(provider.TYPE, "1");
            cv.put(provider.DATE, "Nevinnomussk");
            cv.put(provider.HESH, ImageConverter.hash("Nevinnomussk"));
            cv.put(provider.DAY, "http://api.wunderground.com/api/0c4d0979336b962f/forecast10day/q/zmw:00000.1.37036.json");
            //mTitle="Nevinnomussk, Rassia";
            getContentResolver().insert(provider.CONTENT_URI, cv);
        }
*/


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    static AppReceiver mReceiver;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        Cursor cursor = getContentResolver().query(provider.CONTENT_URI, null, provider.TYPE + " = '1' ", null, null);
        if (cursor.moveToFirst()){
            cursor.move(position);
            String name=cursor.getString(cursor.getColumnIndex(provider.DATE));
            Intent intent=new Intent(this,IService.class);
            intent.putExtra("1",mReceiver)
                    .putExtra("ref","0")
                    .putExtra("task", name)
                    .putExtra("importance", "1");
            startService(intent);
        }
    }


    void showData(){
        String name =ImageConverter.hash(mTitle.toString());
        //81850019755742275557
        Cursor cursor_2 = getContentResolver().query(provider.CONTENT_URI, null, "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )", null, null);
        cursor_2.moveToFirst();
        ImageView im = (ImageView) findViewById(R.id.imageView);
        im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.FIVE_PATH))));
        im = (ImageView) findViewById(R.id.imageView2);
        im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.SIX_PATH))));
        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setText("Today is " + cursor_2.getString(cursor_2.getColumnIndex(provider.DATE)));
        tv = (TextView) findViewById(R.id.description);
        tv.setText("Day forecast");
        tv = (TextView) findViewById(R.id.textView);
        tv.setText(cursor_2.getString(cursor_2.getColumnIndex(provider.DAY)));
        tv = (TextView) findViewById(R.id.textView1);
        tv.setText("Nigth forecast");
        tv = (TextView) findViewById(R.id.textView2);
        tv.setText(cursor_2.getString(cursor_2.getColumnIndex(provider.NIGHT)));
        ListView lv = (ListView) findViewById(R.id.listView);
        String[] from=new String[]{provider.DATE, provider.TEMPERATURE, provider.DAY};
        int[] to=new int[]{R.id.textView6, R.id.textView7, R.id.textView8};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.prog_bar, cursor_2,from,to);
        lv.setAdapter(adapter);

        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar2);
        mProgress.setVisibility(View.INVISIBLE);
    }

    public void onSectionAttached(int number) {
        Cursor cursor=getContentResolver().query(provider.CONTENT_URI,null,provider.TYPE + " = '1' ",null,null);
        mTitle="No city";
        //try {
        //    alert_3.cancel();
        //}catch (Exception e){
        //    e.printStackTrace();
       //
       //}
        NavigationDrawerFragment.pushed=true;
        if (cursor.moveToFirst()) {
            cursor.move(number-1);
            mTitle = cursor.getString(cursor.getColumnIndex(provider.DATE));
            NavigationDrawerFragment.pushed=false;
        } /*Intent intent=new Intent(this,IService.class);
        intent.putExtra("1",PlaceholderFragment.get())
                .putExtra("task", mTitle.toString())
                .putExtra("importance","1");
        startService(intent);
    */}

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

    public AlertDialog.Builder alert_2;
    public AlertDialog alert_3;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText=new EditText(this);
            alert.setView(editText);
            alert.setTitle("Enter city name!");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    city=editText.getText().toString();
                    alert_2 = new AlertDialog.Builder(MainActivity.this);
                    final ProgressBar pb=new ProgressBar(MainActivity.this);
                    pb.setVisibility(View.VISIBLE);
                    alert_2.setView(pb);
                     //alert_3 = alert_2.create();
                    //alert_2.show();
                    //city="moscow";
                    Intent intent =new Intent(MainActivity.this,IServiceAddCity.class);
                    intent.putExtra("city",city).putExtra("1",mReceiver);
                    startService(intent);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
public String city;
    void showCities(Bundle data,String result){
       // alert_3.cancel();
        final AlertDialog .Builder alert = new AlertDialog.Builder(MainActivity.this);
        //ProgressBar pb = alert_2;
        //pb.setVisibility(View.VISIBLE);0
        //city="moscow";
        result=data.getBundle("ans").getString("result");
        if (result.equals("mistake")){
            alert.setTitle("Not found!");
            alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            //alert_3=alert_2.create();
            alert.show();
            return;
        }
        if (result.equals("one")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Your city was added!", Toast.LENGTH_SHORT);
            toast.show();
            ContentValues cv = new ContentValues();
            cv.put(provider.TYPE, "1");
            cv.put(provider.DATE, city);
            cv.put(provider.HESH, ImageConverter.hash(city));
            String link = data.getBundle("ans").getString("link");
            cv.put(provider.DAY, "http://api.wunderground.com/api/0c4d0979336b962f/forecast10day" + link + ".json");
            //url = new URL("http://api.wunderground.com/api/0c4d0979336b962f/geolookup/q/"+s+".json");;
            getContentResolver().insert(provider.CONTENT_URI, cv);

            Intent intent = new Intent(this, IService.class);
            intent.putExtra("1", mReceiver)
                    .putExtra("ref", "0")
                    .putExtra("task", city)
                    .putExtra("importance", "0");
            startService(intent);
            return;
        }
        alert.setTitle("Choose city!");
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        ListView listView=new ListView(this);
        final ArrayList<String> list=data.getBundle("ans").getStringArrayList("links");
        final ArrayList<String> links=data.getBundle("ans").getStringArrayList("list");
        final int size=data.getBundle("ans").getInt("size");
        ListAdapter adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,links);
        alert.setView(listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String str = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                String link;
                for (int j = 0; j < size; j++)
                    if (str.equals(links.get(j))) {
                        link = list.get(j);
                        ContentValues cv = new ContentValues();
                        cv.put(provider.TYPE, "1");
                        cv.put(provider.DATE, str);
                        cv.put(provider.HESH, ImageConverter.hash(str));
                        cv.put(provider.DAY, "http://api.wunderground.com/api/0c4d0979336b962f/forecast10day" + link + ".json");
                        getContentResolver().insert(provider.CONTENT_URI, cv);
                        Intent intent = new Intent(MainActivity.this, IService.class);
                        intent.putExtra("1", mReceiver)
                                .putExtra("ref", "0")
                                .putExtra("task", str)
                                .putExtra("importance", "0");
                        startService(intent);
                        break;
                    }
                dialog.cancel();
            }
        });
        dialog =alert.create();
        dialog.show();
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();
        dialog.getWindow().setLayout((int)(0.9*width),(int)(height*0.8));
    }
    AlertDialog dialog = null;

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        if (!data.getBoolean("status")){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Check your network!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        String result = data.getString(IService.RECEIVER_DATA);
        if (result!=null&&result.equals("cities")) {
            showCities(data,result);
            return;
        }
        if (result != null) {
            if (result.charAt(0) == 'S') {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "SUCCESS", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "MISTAKE", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
            showData();

            if (data.getString("ref") != null) NavigationDrawerFragment.pushed = false;

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment  {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ProgressBar mProgress = (ProgressBar) rootView.findViewById(R.id.progressBar2);
            mProgress.setVisibility(View.VISIBLE);

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


            return rootView;
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
