package com.test.netanalytiks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CarPropertyService extends Service {
    public CarPropertyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Initialise CarPropertyHelper class which will do all the necessary work to fetch speed and update database.
        CarPropertyHelper.getInstance().init(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //To Unregister car instance initialized when init method called.
        CarPropertyHelper.getInstance().unRegisterCar();
    }
}