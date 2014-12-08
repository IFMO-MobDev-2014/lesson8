package ru.ifmo.md.lesson8;

/**
 * Created by pva701 on 24.11.14.
 */
public class City {
    private int id;
    private String name;
    private boolean isSelected;
    private int lastUpdate;
    public City(int id, String name, int isSelected, int lastUpdate) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected == 1;
        this.lastUpdate = lastUpdate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public int getLastUpdate() {
        return lastUpdate;
    }
}
