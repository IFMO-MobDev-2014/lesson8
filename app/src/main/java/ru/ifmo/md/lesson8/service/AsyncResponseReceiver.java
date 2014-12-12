package ru.ifmo.md.lesson8.service;

/**
 * Created by flyingleafe on 09.12.14.
 */
public interface AsyncResponseReceiver<T> {
    void processFinish(T result);
}
