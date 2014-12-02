package lesson8.android.weather

import java.text.SimpleDateFormat
import java.util
import java.util.{Random, Date}

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
  private val rand: Random = new Random()
  private var mForecast: List[Weather] = new Weather("Saint-Petersburg", "Ruske", (20, 18), "celsium", new WeatherState(803, "cloudy"), 0.68, 756, "2 m/s SW", new Date(System.currentTimeMillis())) :: Nil
  for (i <- 0 to 10) mForecast = new Weather(
    "Saint-Petersburg",
    "Russia",
    (rand.nextInt(8) + 15, rand.nextInt(8) + 14),
    "Celsium",
    new WeatherState(rand.nextInt(8) * 100 + rand.nextInt(24), if (rand.nextBoolean()) "clear" else "lol"),
    rand.nextInt(100).toDouble / 100,
    rand.nextInt(20) + 730,
    "SW 5 m/s",
    new Date(System.currentTimeMillis() + i * 86400000)) :: mForecast
  mForecast =
  mForecast.reverse
  private var mForecastUsed: Array[Boolean] = new Array[Boolean](mForecast.length)
  mForecastUsed.update(0, true)
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
  }


  override def onDetach(): Unit = super.onDetach()

  override def onCreateHeaderView(inflater: LayoutInflater, container: ViewGroup): View =
    inflater.inflate(R.layout.fragment_header, container, false)

  override def onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View = {
    mListView = cast[View, ListView](inflater.inflate(R.layout.fragment_listview, container, false))
    //    mListView = cast[View, ListView](view.findViewById(R.id.forecast_list))
    mListView.setVisibility(View.INVISIBLE)
    if (mLoaded) setForecast(mForecast)
    mListView
  }

  override def onCreateContentOverlayView(inflater: LayoutInflater, p2: ViewGroup): View = {
    val progressBar: ProgressBar = new ProgressBar(getActivity)
    mContentOverlay = new FrameLayout(getActivity)
    mContentOverlay.addView(progressBar, new LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT,
      Gravity.CENTER))
    mContentOverlay.setVisibility(View.VISIBLE)
    if (mLoaded) mContentOverlay.setVisibility(View.GONE)
    mContentOverlay
  }

  override def onResume(): Unit = {
    super.onResume()
    if (!mLoaded) {
      mListView.setVisibility(View.INVISIBLE)
      mContentOverlay.setVisibility(View.VISIBLE)
      new Thread {
        override def run: Unit = getActivity.runOnUiThread(new Thread {
          override def run(): Unit = {
            Thread.sleep(3000)
            setForecast(mForecast)
          }
        })
      }.start()
    }
  }

  def setForecast(forecast: List[Weather]) = {
    mForecast = forecast
    mForecastUsed = new Array(mForecast.length)
    mForecastUsed.update(0, true)
    mContentOverlay.setVisibility(View.GONE)
    mListView.setVisibility(View.VISIBLE)
    mLoaded = true
    //TODO: its not possible now (((99
    cast[View, ImageView](getActivity.findViewById(android.R.id.background)).setImageResource(mForecast(0).weatherState.getBackground)
    cast[View, TextView](getActivity.findViewById(R.id.title)).setText(mForecast(0).city)
    cast[View, TextView](getActivity.findViewById(R.id.subtitle)).setText(mForecast(0).weatherState.getDesc)
    setListViewAdapter(mListView, new BaseAdapter {
      override def getItemId(id: Int): Long = if (id >= mForecast.length) -1 else id
      override def getCount: Int = mForecast.length
      //      override def isEnabled(id: Int): Boolean = !mForecast(id)._2
      override def getView(id: Int, p2: View, p3: ViewGroup): View = {
        var forecastView: View = null
        val forecast = mForecast(id)
        if (id == 0) {
          forecastView = mInflater.inflate(R.layout.fragment_weather_main, p3, false)
          cast[View, TextView](forecastView.findViewById(R.id.humidity)).setText("Humidity: " + forecast.humidity + "%")
          cast[View, TextView](forecastView.findViewById(R.id.pressure)).setText("Pressure: " + forecast.pressure + " hPa")
          cast[View, TextView](forecastView.findViewById(R.id.wind)).setText("Wind: " + forecast.wind)
        } else {
          forecastView = mInflater.inflate(R.layout.forecast_item, p3, false)
          if (mForecastUsed(id)) {
            forecastView.findViewById(R.id.additional_table).setVisibility(View.VISIBLE)
            cast[View, TextView](forecastView.findViewById(R.id.humidity)).setText("Humidity: " + forecast.humidity + "%")
            cast[View, TextView](forecastView.findViewById(R.id.pressure)).setText("Pressure: " + forecast.pressure + " hPa")
            cast[View, TextView](forecastView.findViewById(R.id.wind)).setText("Wind: " + forecast.wind)
            cast[View, TextView](forecastView.findViewById(R.id.weatherState)).setText("State: " + forecast.weatherState.getDesc)
          } else {
            forecastView.findViewById(R.id.additional_table).setVisibility(View.GONE)
          }
          cast[View, ImageView](forecastView.findViewById(R.id.image_status)).setImageResource(forecast.weatherState.getIcon)
          cast[View, TextView](forecastView.findViewById(R.id.date)).setText(new SimpleDateFormat("dd MMM").format(forecast.date))
        }
        cast[View, TextView](forecastView.findViewById(R.id.lower_temp)).setText(forecast.lowTemp() + " °C")
        // TODO: trash, lots of
        cast[View, TextView](forecastView.findViewById(R.id.upper_temp)).setText(forecast.highTemp() + " °C")
        forecastView.setOnClickListener(new View.OnClickListener {
          override def onClick(p1: View): Unit = {
            if (id != 0) {
              mForecastUsed.update(id, !mForecastUsed(id))
              notifyDataSetChanged()
            }
          }
        })
        forecastView
      }
      override def getItem(p1: Int): AnyRef = mForecast(p1)
    })
  }

}