package ru.ifmo.md.lesson8.places;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Place {
    private final String name;
    private final String country;
    private long woeid;

    private Place(String country, String name, long woeid) {
        this.country = country;
        this.name = name;
        this.woeid = woeid;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public long getWoeid() {
        return woeid;
    }

    public String formattedName() {
        return name + ", " + country;
    }


    public static class Builder {
        private String country;
        private String name;
        private long woeid;

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setWoeid(long woeid) {
            this.woeid = woeid;
            return this;
        }

        public Builder clean() {
            this.name = null;
            this.country = null;
            return this;
        }

        public Place createPlace() {
            return new Place(country, name, woeid);
        }
    }
}
