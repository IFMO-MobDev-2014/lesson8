package me.volhovm.mweather

import java.io.{InputStreamReader, BufferedReader}
import java.net.{HttpURLConnection, URL}
import java.text.SimpleDateFormat
import java.util.{Locale, Random, Date}

import android.app.IntentService
import android.content.{Context, ContentResolver, Intent}
import android.os.{Handler, Bundle, ResultReceiver}
import android.util.Log
import org.apache.http.HttpConnection
import org.json.JSONObject

object WeatherLoadService {
  val CITY = "city"
  val SERVICE_NAME = "WeatherLoadService"
  val STATUS_RUNNING = 0
  val STATUS_FINISHED = 1
  val STATUS_ERROR = 2
  def fakeInit = {
    Log.d("WeatherLoadService", "Started fake init")
    val rand: Random = new Random()
    var forecast = new Weather("Saint-Petersburg", "Russia", (20, 18), new WeatherState(803, "cloudy"), 0.68, 756, "2 m/s SW", new Date(System.currentTimeMillis())) :: Nil
    for (i <- 0 to 10) forecast = new Weather(
      "Saint-Petersburg",
      "Russia",
      (rand.nextInt(8) + 15, rand.nextInt(8) + 14),
      new WeatherState(rand.nextInt(8) * 100 + rand.nextInt(24), if (rand.nextBoolean()) "clear" else "lol"),
      rand.nextInt(100).toDouble / 100,
      rand.nextInt(20) + 730,
      "SW 5 m/s",
      new Date(System.currentTimeMillis() + i * 86400000)) :: forecast
    Log.d("WeatherLoadService", "Ended fake init")
    forecast
  }
}

class WeatherLoadService extends IntentService("WeatherLoadService") {
  override def onHandleIntent(intent: Intent): Unit = {
    Log.d(WeatherLoadService.SERVICE_NAME, "started service")
    val receiver: ResultReceiver = intent.getParcelableExtra("receiver")
    receiver.send(WeatherLoadService.STATUS_RUNNING, Bundle.EMPTY)
    val city: String = intent.getStringExtra(WeatherLoadService.CITY)
    if (city == null | city.length < 1) throw new IllegalArgumentException("Wrong city name in intent or null")
    try {
      val w = loadWeather(city)
      if (w.length > 0) {
        getContentResolver.delete(WeatherProvider.CONTENT_URI, DatabaseHelper.WEATHER_CITY + "='" + w(0).city + "'", null)
        w.foreach((a: Weather) => getContentResolver.insert(WeatherProvider.CONTENT_URI, a.getValues()))
        val bundle: Bundle = new Bundle()
        bundle.putString(Intent.EXTRA_TEXT, w(0).city)
        receiver.send(WeatherLoadService.STATUS_FINISHED, bundle)
      }
    } catch {
      case a: Throwable =>
        Log.e("WeatherLoadService", "Exception while loading weather: " + a.toString)
        val bundle: Bundle = new Bundle()
        bundle.putString(Intent.EXTRA_TEXT, a.toString)
        receiver.send(WeatherLoadService.STATUS_ERROR, bundle)
    }
  }

  //  private def loadWeather(cityname: String): List[Weather] = WeatherLoadService.fakeInit
  private def loadWeather(cityname: String): List[Weather] = {
    Log.d("WeatherLoadService", "Trying to load weather from api")
    val url: URL = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + cityname.replaceAll(" ", "-") + "&mode=json&units=metric&cnt=15&lang=" + Locale.getDefault.getLanguage)
    Log.d("WeatherLoadService", "sending request: " + url.toString)
    val connection: HttpURLConnection = cast(url.openConnection())
    connection.setRequestMethod("GET")
    connection.setConnectTimeout(5000)
    connection.setReadTimeout(5000)
    connection.connect()
    Log.d("WeatherLoadService", "Connection passed or timeout")
    val reader: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))
    var jsonString = ""
    val str = scala.collection.immutable.Stream.continually(reader.readLine()).takeWhile(_ != null).mkString(" ")
    Log.d("WeatherLoaderService", "got string from url")
    parseWeather(str)
//    WeatherLoadService.fakeInit
  }

  private def parseWeather(jsonString: String): List[Weather] = {
    val jsonObj = new JSONObject(jsonString)
    val city = jsonObj.getJSONObject("city").getString("name")
    val country = jsonObj.getJSONObject("city").getString("country")
    val list = jsonObj.getJSONArray("list")
    var forecast: List[Weather] = Nil
    for (i <- 0 until jsonObj.getInt("cnt")) {
      val curr = list.getJSONObject(i)
      forecast = new Weather(
        city,
        country,
        (curr.getJSONObject("temp").getDouble("day").toInt,
          curr.getJSONObject("temp").getInt("night")),
        new WeatherState(curr.getJSONArray("weather").getJSONObject(0).getInt("id"),
          curr.getJSONArray("weather").getJSONObject(0).getString("description").capitalize),
        curr.getInt("humidity").toDouble / 100,
        (curr.getDouble("pressure") / 1.3332239).toInt,
        curr.getDouble("speed").toInt.toString + "m/s " + degToDir(curr.getDouble("deg")),
        new Date(curr.getInt("dt") * 1000)
      ) :: forecast
    }
    forecast.reverse
  }
  private def degToDir(deg: Double): String = deg match {
    case a if a < 11.25 => "N"
    case a if a < 56.25 => "NE"
    case a if a < 101.25 => "E"
    case a if a < 146.25 => "SE"
    case a if a < 191.25 => "S"
    case a if a < 236.25 => "SW"
    case a if a < 281.25 => "W"
    case a if a < 326.25 => "NW"
    case _ => "N"
  }
}

trait Receiver {
  def onReceiveResult(resCode: Int, resData: Bundle): Unit
}

class WeatherLoadReceiver(handler: Handler) extends ResultReceiver(handler) {
  private var mReceiver: Receiver = null
  def setReceiver(r: Receiver) = mReceiver = r
  override def onReceiveResult(resCode: Int, resData: Bundle) = if (mReceiver != null) mReceiver.onReceiveResult(resCode, resData)
}
