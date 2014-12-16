package me.volhovm.mweather

import java.text.SimpleDateFormat

import android.app.Activity
import android.content.Context
import android.os.{Bundle, Handler}
import android.util.Log
import android.view.animation.Animation.AnimationListener
import android.view.animation.{AccelerateInterpolator, AlphaAnimation, Animation, AnimationSet}
import android.view.{Gravity, LayoutInflater, View, ViewGroup}
import android.widget._

class WeatherTwoPanelFragment extends WeatherFragment {
  var mSpinner: FrameLayout = null
  override def onAttach(activity: Activity): Unit = {
    super.onAttach(activity)
    mId = getArguments.getInt(WeatherDetailFragment.FRAGMENT_ID)
    setCity(getArguments.getString(WeatherDetailFragment.CITY_NAME))
    mInflater = cast(activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
    mDatabaseHelper = new DatabaseHelper(getActivity.getApplicationContext)
    mReciever = new WeatherLoadReceiver(new Handler())
    mReciever.setReceiver(cast[Activity, Receiver](activity))
    Log.d(this.toString, "Initiating loader in onAttach()")
    getLoaderManager.restartLoader(0, null, this).forceLoad()
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View =
    inflater.inflate(R.layout.landscape_fragment_item_detail, container, false)

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)
    Log.d(this.toString, "View created")
    mListView = cast(getView.findViewById(R.id.forecast_list))
    cast[View, TextView](getView.findViewById(R.id.title)).setText(cityName)
    val bar = cast[View, ProgressBar](getView.findViewById(R.id.cover_list_bar))
    new Thread(new Runnable(){
      override def run(): Unit = while (bar.getVisibility != View.GONE) {bar.incrementProgressBy(1); Thread.sleep(100)}
    }).start()
  }

  def setForecast(forecast: List[Weather]) = {
    if (forecast.length > 0) {
      Log.d(this.toString, "Setting forecast")
      cast[View, ProgressBar](getView.findViewById(R.id.cover_list_bar)).setVisibility(View.GONE)
      while (mListView == null) {
        Log.d(this.toString, "Waiting until view created")
      }
      mForecast = if (forecast.head.date.after(forecast.last.date)) forecast.reverse else forecast
      if (mForecastUsed == null || mForecastUsed.length != forecast.length) {
        mForecastUsed = new Array(mForecast.length)
      }
      mListView.setVisibility(View.VISIBLE)
      mLoaded = true
      animate(
        cast[View, ImageView](getView.findViewById(R.id.background)),
        cast[View, ImageView](getView.findViewById(R.id.background_overlay)),
        mForecast(0).weatherState.getBackground)
      //    cast[View, ImageView](getActivity.findViewById(android.R.id.background)).setImageResource(mForecast(0).weatherState.getBackground))
      setText(getView.findViewById(R.id.title), cityName)
      setText(getView.findViewById(R.id.subtitle), mForecast(0).weatherState.getDesc.capitalize)
      setText(getView.findViewById(R.id.upper_temp), mForecast(0).highTemp + " °C")
      setText(getView.findViewById(R.id.wind), mForecast(0).wind)
      setText(getView.findViewById(R.id.clouds), mForecast(0).clouds + "%")
      setText(getView.findViewById(R.id.humidity), mForecast(0).humidity + "%")
      setText(getView.findViewById(R.id.pressure), mForecast(0).pressure + " mmhg")
      if (mListView.getAdapter != null)
        cast[ListAdapter, BaseAdapter](mListView.getAdapter).notifyDataSetChanged()
      else mListView.setAdapter(new BaseAdapter {
        override def getItemId(id: Int): Long = if (id >= mForecast.length) -1 else id
        override def isEnabled(position: Int): Boolean = position != 0
        override def getCount: Int = mForecast.length - 1
        override def getView(id_old: Int, p2: View, p3: ViewGroup): View = {
          val id = id_old + 1
          var forecastView: View = null
          val forecast = mForecast(id)
          forecastView = mInflater.inflate(R.layout.forecast_item, p3, false)
          if (mForecastUsed(id)) {
            forecastView.findViewById(R.id.additional_table).setVisibility(View.VISIBLE)
            setText(forecastView.findViewById(R.id.wind), forecast.wind)
            setText(forecastView.findViewById(R.id.clouds), forecast.clouds + "%")
            setText(forecastView.findViewById(R.id.humidity), forecast.humidity + "%")
            setText(forecastView.findViewById(R.id.pressure), forecast.pressure + " mmhg")
          } else {
            forecastView.findViewById(R.id.additional_table).setVisibility(View.GONE)
          }
          setText(forecastView.findViewById(R.id.weatherState),
            //                getResources.getString(R.string.state) + ": "+
            forecast.weatherState.getDesc)
          cast[View, ImageView](forecastView.findViewById(R.id.image_status)).setImageResource(forecast.weatherState.getIcon)
          setText(forecastView.findViewById(R.id.date), new SimpleDateFormat("dd MMM").format(forecast.date))
          setText(forecastView.findViewById(R.id.lower_temp), forecast.lowTemp + " °C")
          // TODO: trash, lots of
          setText(forecastView.findViewById(R.id.upper_temp), forecast.highTemp + " °C")
          forecastView.setOnClickListener(new View.OnClickListener {
            override def onClick(p1: View): Unit = {
              mForecastUsed.update(id, !mForecastUsed(id))
              notifyDataSetChanged()
            }
          })
          forecastView
        }
        override def getItem(p1: Int): AnyRef = mForecast(p1 + 1)
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

  override def toString: String = "WeatherTwoPanelFragment #" + mId + " named " + cityName
}