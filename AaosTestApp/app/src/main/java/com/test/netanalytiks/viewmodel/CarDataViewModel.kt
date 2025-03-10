package com.test.netanalytiks.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import com.test.netanalytiks.CarDataCallbackInterface
import com.test.netanalytiks.CarDataState
import com.test.netanalytiks.CarPropertyService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CarDataViewModel : ViewModel() {
    private val _CarDataState = MutableStateFlow(CarDataState())
    val carDataState : StateFlow<CarDataState> = _CarDataState.asStateFlow()

    /**
     * Interface to listen to car speed change notified from CarPropertyService. This interface object
     * is set to service using binder.
     */
    private var carDataListener = object : CarDataCallbackInterface {
        override fun onMaxSpeedSet(maxSpeed: Int) {
            _CarDataState.update { mState ->
                mState.copy(carMaxSpeed = maxSpeed)
            }
        }

        override fun onSpeedChanged(speed: Float) {
            _CarDataState.update { mState ->
                mState.copy(carCurrentSpeed = speed)
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService().  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            var binder = service as CarPropertyService.CarPropertyServiceBinder
            binder.addCarDataCallback(carDataListener)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

    /**
     * Connect to Service which will fetch car property values.
     */
    fun connectToService(context: Context) {
        Intent(context, CarPropertyService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

}