package com.test.netanalytiks;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.car.app.Car;
import androidx.car.app.CarPropertyManager;
import androidx.car.app.CarPropertyValue;
import android.car.CarNotConnectedException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CarPropertyHelper {

    private Context mContext;
    private Car mCar;
    private CarPropertyManager mCarPropertyManager;

    private final String CAR_ROOT = "cars";
    private final String MAX_SPEED = "maxspeed";
    private final String MAX_SPEED_VIO = "speed_violation";
    private String mVinNo = "";

    //Callback to listen for car speed change
    private final CarPropertyManager.PropertyCallback<Float> mSpeedCallback = new CarPropertyManager.PropertyCallback<Float>() {
        @Override
        public void onChangeEvent(CarPropertyValue<Float> value) {
            if (value != null) {
                float speed = value.getValue();
                if (speed > mMaxSpeed) {
                    updateCarOverSpeed(speed);
                    showWarningPopup(speed);
                }
            }
        }

        @Override
        public void onErrorEvent(int propertyId, int errorCode) {
            Log.e("SpeedReader", "Error getting speed: " + errorCode);
            // Handle errors appropriately
        }
    };

    private static CarPropertyHelper carPropertyHelper;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference mDatabaseRef = mDatabase.getReference();

    private float mMaxSpeed = 0;

    private CarPropertyHelper() {

    }

    public static CarPropertyHelper getInstance() {
        if (carPropertyHelper == null) {
            carPropertyHelper = new CarPropertyHelper();
        }
        return carPropertyHelper;
    }

    /*
    This method initialises Car object which will be used to get Car telemetry data
    To get seed data declare the necessary permissions in your AndroidManifest.xml file.
    in this case <uses-permission android:name="android.car.permission.CAR_SPEED" />
     */
    public void init(Context context) {
        mContext = context;
        mCar = new Car(mContext); // 'this' is your Context
        try {
            mCar.connect();
            mCarPropertyManager = mCar.getCarManager(CarPropertyManager.class);
            if (mCarPropertyManager == null) {
                // Handle the case where CarPropertyManager is not available
                Log.e("SpeedReader", "CarPropertyManager is null. Car service might not be available.");
                return;
            }
            try {
                mCarPropertyManager.registerCallback(
                        mSpeedCallback,
                        CarProperty.VEHICLE_SPEED,
                        CarProperty.SENSOR_RATE_NORMAL); // Or other appropriate sensor rate

            } catch (IllegalArgumentException | IllegalStateException | CarNotConnectedException e) {
                Log.e("SpeedReader", "Error registering for speed updates");
            }
        } catch (CarNotConnectedException e) {
            Log.e("SpeedReader", "Car not connected");
        }

        mVinNo = getVinNo();
        mMaxSpeed = getMaxSpeedForCar(mVinNo);
    }

    /*
    It is assumed that each car data is arranged in firebase with VIN NO of each car.
    So by using this method we are getting Cars VIN NO.
     */
    private String getVinNo() {
        CarPropertyValue<String> vinValue = null;
        if (mCarPropertyManager != null) {
            try {
                vinValue = mCarPropertyManager.getProperty(
                        String.class, CarProperty.VEHICLE_IDENTIFICATION_NUMBER, 0); 

                if (vinValue != null) {
                    String vin = vinValue.getValue();
                    Log.d("VIN", "VIN: " + vin);
                    // Use the VIN (e.g., display it in your app)
                } else {
                    Log.e("VIN", "VIN value is null");
                    return null;
                }

            } catch (CarNotConnectedException | IllegalArgumentException | IllegalStateException e) {
                Log.e("VIN", "Error getting VIN");
                // Handle exceptions
            }
        } else {
            Log.e("VIN", "CarPropertyManager is null");
        }
        return vinValue.getValue();
    }

    /*
    Method to get max speed set by Company.
    It is assumed that from another application car rental company sets max speed for each vehicle.
    Vehicle is identified by car rental company using VIN NO.
     */
    private float getMaxSpeedForCar(String vinno) {
        final int[] maxspeed = {0};
        mDatabaseRef.child(CAR_ROOT).child(vinno).child(MAX_SPEED).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    maxspeed[0] = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                }
            }
        });
        return maxspeed[0];
    }

    /*
    Just shows a Toast as warning. We can also implement a popup here.
     */
    private void showWarningPopup(float speed) {
        Toast.makeText(mContext, "Max Speed Limit Exceeded. Max Speed is " + speed , Toast.LENGTH_SHORT).show();
    }

    /*
    When max speed is exceeded, that value is updated in firebase. It is assumed that "speed_violation" node is
    actively monitored by another application to notify violation to Car rental company.
     */
    private void updateCarOverSpeed(float speed) {
        mDatabaseRef.child(CAR_ROOT).child(mVinNo).child(MAX_SPEED_VIO).push().setValue(speed);
    }
    public void unRegisterCar() {
        if (mCarPropertyManager != null) {
            mCarPropertyManager.unregisterCallback(mSpeedCallback);
        }
    }
}
