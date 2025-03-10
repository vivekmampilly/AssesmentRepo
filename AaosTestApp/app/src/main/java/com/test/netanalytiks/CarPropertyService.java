package com.test.netanalytiks;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class CarPropertyService extends Service {

    private final IBinder binder = new CarPropertyServiceBinder();
    public CarPropertyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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

    /**
     * Class used for the client Binder.  Because this service always
     * runs in the same process as its clients, AIDL not required.
     */
    public class CarPropertyServiceBinder extends Binder {
        public void addCarDataCallback(CarDataCallbackInterface callbackInterface) {
            CarPropertyHelper.getInstance().setCarDataCallback(callbackInterface);
        }
    }
}