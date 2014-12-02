package lesson8.android.weather

import java.util.Date

import android.util.Xml

object Weather {
  def getWeather(xml: Xml): Weather = ???
  def getForecast(xml: Xml): Array[Weather] = ???
}

class Weather(val city: String,
              val country: String,
              val temp: (Int, Int),
              val tempUnit: String,
              val weatherState: WeatherState,
              val humidity: Double,
              val pressure: Int, //hPa
              val wind: String,
              val date: Date) {
  private def wrapTemp(i: Int): String = if (i > 0) "+" + i.toString else i.toString
  def lowTemp() = wrapTemp(temp._1)
  def highTemp() = wrapTemp(temp._2)
}

class WeatherState(status: Int, desc: String) {
  def getBackground(): Int = status match {
    case a if a == 210 | a == 230 | a == 200 => R.drawable.background_thunderstorm_0
    case a if a < 300 && a >= 200 => R.drawable.background_thunderstorm_1
    case a if a == 300 | a == 301 | a == 310 => R.drawable.background_rain_0
    case a if a == 302 | a == 314 | a == 321 => R.drawable.background_rain_2
    case a if a == 500 | a == 501 | a == 511 | a == 520 => R.drawable.background_rain_0
    case a if a == 502 | a == 504 | a == 521 | a == 522 | a == 531 => R.drawable.background_rain_2
    case a if a > 300 && a <= 600 => R.drawable.background_rain_1
    case a if a == 600 | a == 601 | a == 611 | a == 612 | a == 620 => R.drawable.background_snow_0
    case a if a == 615 | a == 616 => R.drawable.background_snow_rain_0
    case a if a > 600 && a < 700 => R.drawable.background_snow_1
    case a if a == 800 | a == 801 => R.drawable.background_clouds_0
    case 802 => R.drawable.background_clouds_1
    case 803 => R.drawable.background_clouds_2
    case 804 => R.drawable.background_clouds_3
    case _ => R.drawable.background_unrecognized
  }
  def getDesc = desc

  //TODO: fill it
  def getIcon: Int = desc match {
    case "clear" => R.drawable.sunny
    case _ => R.drawable.sunny_night
  }
}

