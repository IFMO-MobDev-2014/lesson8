package me.volhovm.mweather

import android.app.Fragment
import android.app.LoaderManager.LoaderCallbacks
import android.content.{Intent, AsyncTaskLoader, Loader}
import android.os.Bundle
import android.util.Log
import android.widget.{Toast, ListView}


object WeatherFragment {
  val CITY_NAME = "city_name"
  val FRAGMENT_ID = "fragment_id"
  def newInstance(cityname: String, id: Int): WeatherDetailFragment = {
    val bundle: Bundle = new Bundle()
    bundle.putString(CITY_NAME, cityname)
    bundle.putInt(FRAGMENT_ID, id)
    val fragment: WeatherDetailFragment = new WeatherDetailFragment
    fragment.setArguments(bundle)
    fragment
  }
}

trait WeatherFragment extends Fragment  with LoaderCallbacks[List[Weather]] {
  protected var mId: Int = -1
  protected var mDatabaseHelper: DatabaseHelper = null
  protected var mForecast: List[Weather] = null
  protected var mForecastUsed: Array[Boolean] = null
  protected var mListView: ListView = null
  protected var mLoaded: Boolean = false
  protected var mReciever: WeatherLoadReceiver = null

  var cityName: String = null
  def setCity(str: String) = cityName = str


  def setForecast(forecast: List[Weather])

  def reloadContents() = {
   Log.d(this.toString, "Reloading data")
    import me.volhovm.mweather.WeatherLoadService._
    val intent: Intent = new Intent(Intent.ACTION_SYNC, null, getActivity, classOf[WeatherLoadService])
    Toast.makeText(getActivity, "Refreshing", Toast.LENGTH_SHORT).show()
    intent.putExtra(SERVICE_MODE, CITY_MODE)
    intent.putExtra(FRAGMENT_ID, mId)
    intent.putExtra(CITY, cityName)
    intent.putExtra(RECEIVER, mReciever)
    getActivity.startService(intent)
  }


  def onCreateLoader(id: Int, bundle: Bundle): Loader[List[Weather]] = new AsyncTaskLoader[List[Weather]](getActivity) {
    override def loadInBackground(): List[Weather] = {
      Log.d("Loader for " + cityName, "Trying to get data from db")
      synchronized[List[Weather]](mDatabaseHelper.mWrapper.getWeatherByCity(cityName))
    }
  }

  def onLoadFinished(loader: Loader[List[Weather]], forecast: List[Weather]): Unit = {
    Log.d("Loader for " + cityName, "Loader finished, got forecast, length: " + forecast.length)
    if (forecast.length > 0) setForecast(forecast) else reloadContents()
  }

  // TODO: Implement onLoaderReset
  def onLoaderReset(loader: Loader[List[Weather]]): Unit = {}
}