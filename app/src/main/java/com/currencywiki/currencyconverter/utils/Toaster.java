package com.currencywiki.currencyconverter.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

enum Toaster {

    INSTANCE;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void showToast(final Context context, final String message, final int length) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, length).show();
                    }
                }
        );
    }

    public static Toaster get() {
        return INSTANCE;
    }
}
