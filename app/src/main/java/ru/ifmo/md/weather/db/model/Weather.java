package ru.ifmo.md.weather.db.model;

/**
 * Created by Kirill on 01.12.2014.
 */
public class Weather {

    private String receivingTime;
    private int temp;
    private double humidity;
    private int tempMin;
    private int tempMax;
    private double pressure;
    private double windSpeed;
    private String description;
    private String iconName;
    private String cityName;
    private long cityId;

    public Weather() {
        cityId = 0;
    }

    public Weather(String receivingTime, int temp, double humidity, int tempMin,
                   int tempMax, double pressure, double windSpeed, String cityName,
                   String description, String iconName, long cityId) {
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

    public String getReceivingTime() {
        return receivingTime;
    }

    public void setReceivingTime(String receivingTime) {
        this.receivingTime = receivingTime;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public int getTempMin() {
        return tempMin;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
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
