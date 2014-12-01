package odeen.weatherpredictor;

/**
 * Created by Женя on 25.11.2014.
 */
public class Location {
    private int mId;
    private String mCity;
    private int mColor;

    public Location(int id, String city, int color) {
        this.mId = id;
        this.mCity = city;
        mColor = color;
    }

    public Location() {

    }
    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }
}
