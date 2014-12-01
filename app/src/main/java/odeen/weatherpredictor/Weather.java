package odeen.weatherpredictor;


import java.util.Date;

/**
 * Created by Женя on 23.11.2014.
 */
public class Weather {
    private String mMainName;
    private String mDescription;
    private String mIconId;

    private double mTemperature;
    private double mMinTemperature;
    private double mMaxTemperature;
    private double mPressure;
    private double mHumidity;
    private double mClouds;
    private double mWindSpeed;
    private double mWindDirection;
    private long mTime;


    public Weather(String mainName, String description, String iconId, double temperature,
                   double minTemperature, double maxTemperature, double pressure,
                   double humidity, double clouds, double windSpeed, double windDirection, long time) {
        this.mMainName = mainName;
        this.mDescription = description;
        this.mIconId = iconId;
        this.mTemperature = temperature;
        this.mMinTemperature = minTemperature;
        this.mMaxTemperature = maxTemperature;
        this.mPressure = pressure;
        this.mHumidity = humidity;
        this.mClouds = clouds;
        this.mWindSpeed = windSpeed;
        this.mWindDirection = windDirection;
        this.mTime = time;
    }

    public Weather(){

    }

    public double getWindDirection() {
        return mWindDirection;
    }

    public void setWindDirection(double mWindDirection) {
        this.mWindDirection = mWindDirection;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(double mWindSpeed) {
        this.mWindSpeed = mWindSpeed;
    }

    public double getClouds() {
        return mClouds;
    }

    public void setClouds(double mClouds) {
        this.mClouds = mClouds;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double mHumidity) {
        this.mHumidity = mHumidity;
    }

    public double getPressure() {
        return mPressure;
    }

    public void setPressure(double mPressure) {
        this.mPressure = mPressure;
    }

    public double getMaxTemperature() {
        return mMaxTemperature;
    }

    public void setMaxTemperature(double mMaxTemperature) {
        this.mMaxTemperature = mMaxTemperature;
    }

    public double getMinTemperature() {
        return mMinTemperature;
    }

    public void setMinTemperature(double mMinTemperature) {
        this.mMinTemperature = mMinTemperature;
    }

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double mTemperature) {
        this.mTemperature = mTemperature;
    }

    public String getIconId() {
        return mIconId;
    }

    public void setIconId(String mIconId) {
        this.mIconId = mIconId;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getMainName() {
        return mMainName;
    }

    public void setMainName(String mMainName) {
        this.mMainName = mMainName;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long mUpdatedTime) {
        this.mTime = mUpdatedTime;
    }


}



