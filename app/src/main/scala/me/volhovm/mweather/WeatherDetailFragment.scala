package me.volhovm.mweather

import java.text.SimpleDateFormat

import android.app.Activity
import android.app.LoaderManager.LoaderCallbacks
import android.content.{AsyncTaskLoader, Context, Intent, Loader}
import android.os.{Bundle, Handler}
import android.util.Log
import android.view._
import android.view.animation.Animation.AnimationListener
import android.view.animation._
import android.widget.FrameLayout.LayoutParams
import android.widget._
import com.achep.header2actionbar.HeaderFragment
import com.achep.header2actionbar.HeaderFragment.OnHeaderScrollChangedListener

//
//trait NameProvider {
//    def getElem: String
//    def setElem(a: String): Unit
//  }

//trait FragmentViewProvider {
//  def getFragmentView(id: Int): View
//}

object WeatherDetailFragment {
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

class WeatherDetailFragment extends HeaderFragment with LoaderCallbacks[List[Weather]] {
  private var mId: Int = -1
  private var mDatabaseHelper: DatabaseHelper = null
  private var mForecast: List[Weather] = null
  private var mForecastUsed: Array[Boolean] = null
  private var mListView: ListView = null
  private var mLoaded: Boolean = false
  private var mReciever: WeatherLoadReceiver = null
  private var mInflater: LayoutInflater = null
  private var mContentOverlay: FrameLayout = null

  //FIXME: Initialize that
  var cityName: String = null
  def setCity(str: String) = cityName = str

  override def onAttach(activity: Activity): Unit = {
    super.onAttach(activity)
    mId = getArguments.getInt(WeatherDetailFragment.FRAGMENT_ID)
    setCity(getArguments.getString(WeatherDetailFragment.CITY_NAME))
    mInflater = cast(activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
    mDatabaseHelper = new DatabaseHelper(getActivity)
    mReciever = new WeatherLoadReceiver(new Handler())
    mReciever.setReceiver(cast[Activity, Receiver](activity))
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
    getLoaderManager.initLoader(0, null, this).forceLoad()
  }


  override def onDetach(): Unit = super.onDetach()

  override def onCreateHeaderView(inflater: LayoutInflater, container: ViewGroup): View =
    inflater.inflate(R.layout.fragment_header, container, false)

  override def onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View = {
    mListView = cast[View, ListView](inflater.inflate(R.layout.fragment_listview, container, false))
    //    mListView = cast[View, ListView](view.findViewById(R.id.forecast_list))
    if (mLoaded) setForecast(mForecast)
    else mListView.setVisibility(View.INVISIBLE)
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

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)
    Log.d(this.toString, "View created")
    cast[View, TextView](getView.findViewById(R.id.title)).setText(cityName)
  }

  def setForecast(forecast: List[Weather]) = {
    if (forecast.length > 0) {
      Log.d(this.toString, "Setting forecast")
      while (mContentOverlay == null | mListView == null) {
        Log.d(this.toString, "Wating until view created")
      }
      mForecast = if (forecast.head.date.after(forecast.last.date)) forecast.reverse else forecast
      if (mForecastUsed == null || mForecastUsed.length != forecast.length) {
        mForecastUsed = new Array(mForecast.length)
        mForecastUsed.update(0, true)
      }
      mContentOverlay.setVisibility(View.GONE)
      mListView.setVisibility(View.VISIBLE)
      mLoaded = true
      animate(
        cast[View, ImageView](getView.findViewById(android.R.id.background)),
        cast[View, ImageView](getView.findViewById(R.id.background_overlay)),
        mForecast(0).weatherState.getBackground)
      //    cast[View, ImageView](getActivity.findViewById(android.R.id.background)).setImageResource(mForecast(0).weatherState.getBackground))
      cast[View, TextView](getView.findViewById(R.id.title)).setText(cityName)
      cast[View, TextView](getView.findViewById(R.id.subtitle)).setText(mForecast(0).weatherState.getDesc.capitalize)
      if (mListView.getAdapter != null)
        cast[ListAdapter, BaseAdapter](cast[Adapter, HeaderViewListAdapter](mListView.getAdapter).getWrappedAdapter).notifyDataSetChanged()
      else setListViewAdapter(mListView, new BaseAdapter {
        override def getItemId(id: Int): Long = if (id >= mForecast.length) -1 else id
        override def isEnabled(position: Int): Boolean = position != 0
        override def getCount: Int = mForecast.length
        override def getView(id: Int, p2: View, p3: ViewGroup): View = {
          var forecastView: View = null
          val forecast = mForecast(id)
          if (id == 0) {
            forecastView = mInflater.inflate(R.layout.main_weather_data, p3, false)
            cast[View, TextView](forecastView.findViewById(R.id.humidity)).setText(getResources.getString(R.string.humidity) + ": " + forecast.humidity + "%")
            cast[View, TextView](forecastView.findViewById(R.id.pressure)).setText(getResources.getString(R.string.pressure) + ": " + forecast.pressure + " mmhg")
            cast[View, TextView](forecastView.findViewById(R.id.wind)).setText(getResources.getString(R.string.wind) + ": " + forecast.wind)
          } else {
            forecastView = mInflater.inflate(R.layout.forecast_item, p3, false)
            if (mForecastUsed(id)) {
              forecastView.findViewById(R.id.additional_table).setVisibility(View.VISIBLE)
              cast[View, TextView](forecastView.findViewById(R.id.humidity)).setText(getResources.getString(R.string.humidity) + ": " + forecast.humidity + "%")
              cast[View, TextView](forecastView.findViewById(R.id.pressure)).setText(getResources.getString(R.string.pressure) + ": " + forecast.pressure + " mmhg")
              cast[View, TextView](forecastView.findViewById(R.id.wind)).setText(getResources.getString(R.string.wind) + ": " + forecast.wind)
              cast[View, TextView](forecastView.findViewById(R.id.weatherState)).setText(
                //                getResources.getString(R.string.state) + ": "+
                forecast.weatherState.getDesc)
            } else {
              forecastView.findViewById(R.id.additional_table).setVisibility(View.GONE)
            }
            cast[View, ImageView](forecastView.findViewById(R.id.image_status)).setImageResource(forecast.weatherState.getIcon)
            cast[View, TextView](forecastView.findViewById(R.id.date)).setText(new SimpleDateFormat("dd MMM").format(forecast.date))
          }
          cast[View, TextView](forecastView.findViewById(R.id.lower_temp)).setText(forecast.lowTemp + " °C")
          // TODO: trash, lots of
          cast[View, TextView](forecastView.findViewById(R.id.upper_temp)).setText(forecast.highTemp + " °C")
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
    } else throw new IllegalArgumentException("Tried to set empty forecast")
  }

  private def animate(imageView: ImageView, overlay: ImageView, srcRecID: Int): Unit = {
    val fadeInDuration: Int = 400
    val fadeOut: Animation = new AlphaAnimation(10, 0.1f)
    val fadeIn: Animation = new AlphaAnimation(0.1f, 10)
    overlay.setImageResource(srcRecID)
    fadeIn.setInterpolator(new AccelerateInterpolator())
    fadeIn.setDuration(fadeInDuration)
    fadeIn.setAnimationListener(new AnimationListener {
      override def onAnimationEnd(p1: Animation): Unit = {
        imageView.setImageResource(srcRecID)
        overlay.setVisibility(View.INVISIBLE)
      }
      override def onAnimationStart(p1: Animation): Unit = overlay.setVisibility(View.VISIBLE)
      override def onAnimationRepeat(p1: Animation): Unit = {}
    })

    val animation: AnimationSet = new AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.addAnimation(fadeOut)
    animation.setRepeatCount(1)
    overlay.setVisibility(View.VISIBLE)
    overlay.setAnimation(animation)
    animation.start()
  }

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

  override def toString: String = "WeatherDetailFragment #" + mId + " named " + cityName

  def onCreateLoader(id: Int, bundle: Bundle): Loader[List[Weather]] = new AsyncTaskLoader[List[Weather]](getActivity) {
    override def loadInBackground(): List[Weather] = {
      mDatabaseHelper.getWeatherByCity(cityName)
    }
  }

  def onLoadFinished(loader: Loader[List[Weather]], forecast: List[Weather]): Unit =
    if (forecast.length > 0) setForecast(forecast) else reloadContents()

  // TODO: Implement onLoaderReset
  def onLoaderReset(loader: Loader[List[Weather]]): Unit = {}
}