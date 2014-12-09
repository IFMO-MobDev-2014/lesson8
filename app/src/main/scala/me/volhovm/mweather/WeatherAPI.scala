package me.volhovm.mweather

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.util.Date

import android.util.Log
import org.json.JSONObject

sealed trait WeatherApi {
  def getForecastForCity(cityname: String, locale: String, timeout: Int): List[Weather] = ???
  def getForecastForCoordinates(lat: Double, long: Double, locale: String, timeout: Int): List[Weather] = ???
}

object OpenWeatherMapApi extends WeatherApi {
  override def getForecastForCity(cityname: String, locale: String, timeout: Int): List[Weather] =
    loadWeather(new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" +
      cityname.replaceAll(" ", "-") + "&mode=json&units=metric&cnt=15&lang=" + locale), timeout)

  override def getForecastForCoordinates(lat: Double, lon: Double, locale: String, timeout: Int): List[Weather] =
    loadWeather(new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat=" +
      lat + "&lon=" + lon + "&mode=json&units=metric&cnt=15&lang=" + locale), timeout)

  private def loadWeather(url: URL, timeout: Int): List[Weather] = {
    Log.d("WeatherLoadService", "Trying to load weather from api")
    Log.d("WeatherLoadService", "sending request: " + url.toString)
    val connection: HttpURLConnection = cast(url.openConnection())
    connection.setRequestMethod("GET")
    connection.setConnectTimeout(timeout)
    connection.setReadTimeout(timeout)
    connection.connect()
    Log.d("WeatherLoadService", "Connection passed or timeout")
    val reader: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))
    var jsonString = ""
    val str = scala.collection.immutable.Stream.continually(reader.readLine()).takeWhile(_ != null).mkString(" ")
//    Log.d("WeatherLoaderService", "got string from url, parsing: " + str)
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
        curr.optInt("humidity", 0).toDouble / 100,
        (curr.optDouble("pressure", 0) / 1.3332239).toInt,
        curr.optDouble("speed", 0).toInt.toString + "m/s " + degToDir(curr.optDouble("deg", 0)),
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
