package ru.ifmo.md.weather.db.model;

/**
 * Created by Kirill on 01.12.2014.
 */
public class Weather {

    private String receivingTime;
    private double temp;
    private double humidity;
    private double tempMin;
    private double tempMax;
    private double pressure;
    private double windSpeed;
    private String description;
    private String iconName;
    private String cityName;
    private int cityId;

    public Weather() {
        cityId = 0;
    }

    public Weather(String receivingTime, double temp, double humidity, double tempMin,
                   double tempMax, double pressure, double windSpeed, String cityName,
                   String description, String iconName, int cityId) {
        this.receivingTime = receivingTime;
        this.temp = temp;
        this.humidity = humidity;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.cityName = cityName;
        this.description = description;
        this.iconName = iconName;
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getReceivingTime() {
        return receivingTime;
    }

    public void setReceivingTime(String receivingTime) {
        this.receivingTime = receivingTime;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "receivingTime='" + receivingTime + '\'' +
                ", temp=" + temp +
                ", humidity=" + humidity +
                ", tempMin=" + tempMin +
                ", tempMax=" + tempMax +
                ", pressure=" + pressure +
                ", windSpeed=" + windSpeed +
                ", description='" + description + '\'' +
                ", iconName='" + iconName + '\'' +
                ", cityName='" + cityName + '\'' +
                ", cityId=" + cityId +
                '}';
    }
}
