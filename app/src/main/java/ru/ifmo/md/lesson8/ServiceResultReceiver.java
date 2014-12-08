package ru.ifmo.md.lesson8;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

public class ServiceResultReceiver extends ResultReceiver {
    Context context;

    public ServiceResultReceiver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    protected void onReceiveResult(int code, Bundle bundle) {
        String toShow = bundle.getString("error");
        Toast.makeText(context, toShow, Toast.LENGTH_SHORT).show();
    }
}
