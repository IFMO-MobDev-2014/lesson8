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

    AlertDialog dialog = null;
    public String city;
    public AlertDialog.Builder alert_2;
    static AppReceiver mReceiver;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    public static CharSequence mTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiver = new AppReceiver(new Handler());
        mReceiver.setReceiver(this);
        setContentView(R.layout.activity_wether);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        Cursor cursor = getContentResolver().query(provider.CONTENT_URI, null, provider.TYPE + " = '1' ", null, null);
        if (cursor.moveToFirst()){
            cursor.move(position);
            String name=cursor.getString(cursor.getColumnIndex(provider.DATE));
            Intent intent=new Intent(this,IService.class);
            intent.putExtra(Consts.RECEIVER,mReceiver)
                    .putExtra("refresh","1")
                    .putExtra("task", name)
                    .putExtra("importance", "1");
            startService(intent);
        }
    }

    void showData(){
        String name =ImageConverter.hash(mTitle.toString());
        Cursor cursor_2 = getContentResolver().query(provider.CONTENT_URI, null, "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )", null, null);
        cursor_2.moveToFirst();
        ImageView im = (ImageView) findViewById(R.id.imageView);
        im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.FIRST_PIC))));
        im = (ImageView) findViewById(R.id.imageView2);
        im.setImageBitmap(ImageConverter.getImage(cursor_2.getBlob(cursor_2.getColumnIndex(provider.SECOND_PIC))));
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
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item, cursor_2,from,to);
        lv.setAdapter(adapter);
        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar2);
        mProgress.setVisibility(View.INVISIBLE);
    }

    public void onSectionAttached(int number) {
        Cursor cursor=getContentResolver().query(provider.CONTENT_URI,null,provider.TYPE + " = '1' ",null,null);
        mTitle="No city";
        NavigationDrawerFragment.pushed=true;
        if (cursor.moveToFirst()) {
            cursor.move(number-1);
            mTitle = cursor.getString(cursor.getColumnIndex(provider.DATE));
            NavigationDrawerFragment.pushed=false;
        }
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
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
                    Intent intent =new Intent(MainActivity.this,IServiceAddCity.class);
                    intent.putExtra("city",city).putExtra(Consts.RECEIVER,mReceiver);
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

    void showCities(Bundle data,String result){
        final AlertDialog .Builder alert = new AlertDialog.Builder(MainActivity.this);
        result=data.getBundle("ans").getString("result");
        if (result.equals("mistake")){
            alert.setTitle("Not found!");
            alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
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
            getContentResolver().insert(provider.CONTENT_URI, cv);
            Intent intent = new Intent(this, IService.class);
            intent.putExtra(Consts.RECEIVER, mReceiver)
                    .putExtra("refresh", "0")
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
                String cityName = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                String link;
                for (int j = 0; j < size; j++)
                    if (cityName.equals(links.get(j))) {
                        link = list.get(j);
                        ContentValues cv = new ContentValues();
                        cv.put(provider.TYPE, "1");
                        cv.put(provider.DATE, cityName);
                        cv.put(provider.HESH, ImageConverter.hash(cityName));
                        cv.put(provider.DAY, "http://api.wunderground.com/api/0c4d0979336b962f/forecast10day" + link + ".json");
                        getContentResolver().insert(provider.CONTENT_URI, cv);
                        Intent intent = new Intent(MainActivity.this, IService.class);
                        intent.putExtra(Consts.RECEIVER, mReceiver)
                                .putExtra("refresh", "0")
                                .putExtra("task", cityName)
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
        int width = display.getWidth();
        int height = display.getHeight();
        dialog.getWindow().setLayout((int)(0.9*width),(int)(height*0.8));
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        if (!data.getBoolean("status")){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Check your network!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        String result = data.getString(Consts.RECEIVER_DATA);
        if (result!=null&&result.equals("cities")) {
            showCities(data,result);
            return;
        }
        if (result != null) {
            if (result.charAt(0) == 'S') {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "success", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "mistake", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
            showData();
            if (data.getString("refresh") != null) NavigationDrawerFragment.pushed = false;
    }

    public static class PlaceholderFragment extends Fragment  {
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(Consts.ARG_SECTION_NUMBER, sectionNumber);
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
                    getArguments().getInt(Consts.ARG_SECTION_NUMBER));
        }
    }
}
