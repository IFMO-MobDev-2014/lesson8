package ru.ifmo.md.lesson8.service;

import android.os.CountDownTimer;

/**
 * Created by flyingleafe on 11.12.14.
 */
public class Throttler {
    private CountDownTimer mTimer;

    public interface Callback {
        void call();
    }

    public void throttle(final Callback foo, long timeout) {
        if(mTimer != null) {
            cancel();
        }
        mTimer = new CountDownTimer(timeout, timeout) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                foo.call();
            }
        }.start();
    }

    public void cancel() {
        mTimer.cancel();
        mTimer = null;
    }
}
