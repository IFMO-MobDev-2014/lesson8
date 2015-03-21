package ru.ifmo.md.lesson8;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * @author Aydar Gizatullin a.k.a. lightning95
 */

public class MyContentObserver extends ContentObserver {
    Handler mHandler;
    Callbacks mCallback = null;

    public interface Callbacks {
        public void onObserverFired();
    }

    public MyContentObserver(Callbacks callback) {
        super(null);
        mHandler = new Handler();
        mCallback = callback;
    }

    @Override
    public void onChange(boolean selfChange) {
        mCallback.onObserverFired();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange);
    }
}
