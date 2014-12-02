package lesson8.android.weather

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import android.app.Activity
import android.content.{DialogInterface, Context}
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.view._
import android.widget.FrameLayout.LayoutParams
import android.widget._
import com.achep.header2actionbar.HeaderFragment
import com.achep.header2actionbar.HeaderFragment.OnHeaderScrollChangedListener
import lesson8.android.weather.weather._


class WeatherDetailFragment extends HeaderFragment {
  //  private var mWeatherToday: Weather = null
  private var mForecast: Array[(Weather, Boolean)] = List(
    (new Weather("Saint-Petersburg", "Ruske", "+20", "celsium", "clear", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis())), true),
    (new Weather("Saint-Petersburg", "Ruske", "+22", "celsium", "clear", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+23", "celsium", "rainy", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 2 * 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+18", "celsium", "clear", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 3 * 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+16", "celsium", "clear", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 4 * 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+15", "celsium", "rainy", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 5 * 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+13", "celsium", "clear", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 6 * 86400000)), false),
    (new Weather("Saint-Petersburg", "Ruske", "+11", "celsium", "ranie", "humid", "756 mm/hg", "2 m/s SW",
      new Date(System.currentTimeMillis() + 7 * 86400000)), false)).toArray
  private var mListView: ListView = null
  private var mLoaded: Boolean = false
  private var mInflater: LayoutInflater = null
  private var mContentOverlay: FrameLayout = null

  override def onAttach(activity: Activity): Unit = {
    super.onAttach(activity)
    mInflater = cast(getActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))

    setHeaderBackgroundScrollMode(HeaderFragment.HEADER_BACKGROUND_SCROLL_PARALLAX)
    setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener {
      override def onHeaderScrollChanged(progress: Float, height: Int, scroll: Int): Unit = {
        var progress2: Float = scroll.toFloat / (height - getActivity.getActionBar.getHeight).toFloat
        if (progress2 > 1f) progress2 = 1f
        progress2 = (1f - Math.cos(progress2 * Math.PI).toFloat) * 0.5f
        cast[Activity, WeatherActivity](getActivity).getFadingActionBarHelper()
          .setActionBarAlpha((255 * progress2).toInt)
      }
    })
    mLoaded = true
  }


  override def onDetach(): Unit = super.onDetach()

  override def onCreateHeaderView(inflater: LayoutInflater, container: ViewGroup): View =
    inflater.inflate(R.layout.fragment_header, container, false)

  override def onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View = {
    mListView = cast[View, ListView](inflater.inflate(R.layout.fragment_listview, container, false))
    //    mListView = cast[View, ListView](view.findViewById(R.id.forecast_list))
    mListView
  }


  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = setForecast()

  def setForecast() = {
    cast[View, TextView](getActivity.findViewById(R.id.title)).setText(mForecast(0)._1.city)
    cast[View, TextView](getActivity.findViewById(R.id.subtitle)).setText(mForecast(0)._1.cloudness)
    setListViewAdapter(mListView, new BaseAdapter {
      override def getItemId(id: Int): Long = if (id > mForecast.length) -1 else id
      override def getCount: Int = mForecast.length
      override def isEnabled(id: Int): Boolean = !mForecast(id)._2
      override def getView(id: Int, p2: View, p3: ViewGroup): View = {
        var forecastView: View = null
        val forecast = mForecast(id)._1
        if (mForecast(id)._2) {
          forecastView = mInflater.inflate(R.layout.fragment_weather_main, p3, false)
          cast[View, TextView](forecastView.findViewById(R.id.humidity)).setText("Humidity: " + forecast.humidity)
          cast[View, TextView](forecastView.findViewById(R.id.pressure)).setText("Pressure: " + forecast.pressure)
          cast[View, TextView](forecastView.findViewById(R.id.wind)).setText("Wind: " + forecast.wind)
        } else {
          forecastView = mInflater.inflate(R.layout.forecast_item, p3, false)
          cast[View, ImageView](forecastView.findViewById(R.id.image_status)).setImageResource(forecast.cloudness match {
            case "clear" => R.drawable.sunny
            case _ => R.drawable.sunny_night
          })
          cast[View, TextView](forecastView.findViewById(R.id.date)).setText(new SimpleDateFormat("d").format(forecast.date))
        }
        cast[View, TextView](forecastView.findViewById(R.id.lower_temp)).setText(forecast.temp + " °C")
        // TODO: trash, lots of
        cast[View, TextView](forecastView.findViewById(R.id.upper_temp)).setText(forecast.temp + " °C")
        forecastView.setOnClickListener(new View.OnClickListener {
          override def onClick(p1: View): Unit = {
            if (id != 0) {
              mForecast.update(id, (mForecast(id)._1, !mForecast(id)._2))
              notifyDataSetChanged()
            }
          }
        })
        forecastView
      }
      override def getItem(p1: Int): AnyRef = mForecast(p1)._1
    })
  }

  override def onCreateContentOverlayView(inflater: LayoutInflater, p2: ViewGroup): View = {
    val progressBar: ProgressBar = new ProgressBar(getActivity)
    mContentOverlay = new FrameLayout(getActivity)
    mContentOverlay.addView(progressBar, new LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT,
      Gravity.CENTER))
    if (mLoaded) mContentOverlay.setVisibility(View.GONE)
    mContentOverlay
  }
}