package ru.ifmo.md.extratask1;

import android.graphics.Bitmap;

public class MyImage {
    public Bitmap picture;
    public String username;
    public String pictureName;

    public MyImage(Bitmap picture, String username, String pictureName) {
        this.picture = picture;
        this.username = username;
        this.pictureName = pictureName;
    }
}