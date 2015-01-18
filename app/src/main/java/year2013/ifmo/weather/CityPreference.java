package year2013.ifmo.weather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Юлия on 17.01.2015.
 */
public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    String getCity(){
        Log.d("CityPreference", "I'm in getCity method! ");
        return prefs.getString("city", "Moscow");
    }

    void setCity(String city){
        Log.d("CityPreference", "I'm in setCity method! ");
        prefs.edit().putString("city", city).commit();
    }

}
