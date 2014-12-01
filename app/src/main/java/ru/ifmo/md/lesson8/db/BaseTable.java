package ru.ifmo.md.lesson8.db;

import android.provider.BaseColumns;

/**
 * Created by flyingleafe on 01.12.14.
 */
abstract public class BaseTable implements BaseColumns {

    protected static final String TEXT_TYPE = " TEXT";
    protected static final String INTEGER_TYPE = " INTEGER";
    protected static final String COMMA_SEP = ",";
}
