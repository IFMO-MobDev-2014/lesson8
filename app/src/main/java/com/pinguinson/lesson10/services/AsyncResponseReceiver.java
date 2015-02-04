package com.pinguinson.lesson10.services;

/**
 * Created by pinguinson.
 */
public interface AsyncResponseReceiver<T> {
    void processFinish(T result);
}
