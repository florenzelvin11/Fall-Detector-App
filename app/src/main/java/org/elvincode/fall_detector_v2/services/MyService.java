package org.elvincode.fall_detector_v2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Random;

public class MyService extends Service {

    private static final String TAG = "MyService";

    private Random random = new Random();

    private IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        MyService getService(){
            return MyService.this;
        }
    }

    public int getRandom(){
        return random.nextInt();
    }
}
