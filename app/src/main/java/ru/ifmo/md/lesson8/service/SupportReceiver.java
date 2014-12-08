package ru.ifmo.md.lesson8.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Wrapper class used for passing fragment callbacks to IntentService
 */
public class SupportReceiver extends ResultReceiver {
    Receiver mReceiver;

    public SupportReceiver(Handler handler, Receiver mReceiver) {
        super(handler);
        this.mReceiver = mReceiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
