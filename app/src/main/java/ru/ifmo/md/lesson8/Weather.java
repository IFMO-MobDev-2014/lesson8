package ru.ifmo.md.lesson8;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class Weather {
    private Now now;
    private Day firstDay;
    private Day secondDay;
    private Day thirdDay;
    private Day fourthDay;
    private Day fifthDay;

    Weather(Now now, Day firstDay, Day secondDay, Day thirdDay, Day fourthDay, Day fifthDay) {
        this.now = new Now(now);
        this.firstDay = new Day(firstDay);
        this.secondDay = new Day(secondDay);
        this.thirdDay = new Day(thirdDay);
        this.fourthDay = new Day(fourthDay);
        this.fifthDay = new Day(fifthDay);
    }

    Weather(Weather weather) {
        this.now = new Now(weather.getNow());
        this.firstDay = new Day(weather.getFirstDay());
        this.secondDay = new Day(weather.getSecondDay());
        this.thirdDay = new Day(weather.getThirdDay());
        this.fourthDay = new Day(weather.getFourthDay());
        this.fifthDay = new Day(weather.getFifthDay());
    }

    Weather() {
    }

    public void setNow(Now now) {
        this.now = new Now(now);
    }

    public void setFirstDay(Day firstDay) {
        this.firstDay = new Day(firstDay);
    }

    public void setSecondDay(Day secondDay) {
        this.secondDay = new Day(secondDay);
    }

    public void setThirdDay(Day thirdDay) {
        this.thirdDay = new Day(thirdDay);
    }

    public void setFourthDay(Day fourthDay) {
        this.fourthDay = new Day(fourthDay);
    }

    public void setFifthDay(Day fifthDay) {
        this.fifthDay = new Day(fifthDay);
    }

    public Now getNow() {
        return this.now;
    }

    public Day getFirstDay() {
        return this.firstDay;
    }

    public Day getSecondDay() {
        return this.secondDay;
    }

    public Day getThirdDay() {
        return this.thirdDay;
    }

    public Day getFourthDay() {
        return this.fourthDay;
    }

    public Day getFifthDay() {
        return this.fifthDay;
    }
}
