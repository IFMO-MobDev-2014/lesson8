package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by 107476 on 10.01.2015.
 */
public class AppResultReceiver extends ResultReceiver {
    public static final int OK = 0;
    public static final int ERROR = 1;
    public static final int UPDATE = 2;
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle data);
    }

    private Receiver mReceiver;

    public AppResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
