package org.elvincode.fall_detector_v2.viewmodels;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.elvincode.fall_detector_v2.services.BluetoothService;


public class MainActivityViewModel extends ViewModel {

    private static final String TAG = "BluetoothActivityViewMo";

    // Var
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsBtnState = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsFallStateTrigger = new MutableLiveData<>();
    private MutableLiveData<String> mFallState = new MutableLiveData<>();
    private MutableLiveData<BluetoothService.MyBinder> mBinder = new MutableLiveData<>();

    // Service Connection - Connecting the View Model with the service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Trigger when the client is bounded to the service
            Log.d(TAG, "onServiceConnected: connected to service");
            BluetoothService.MyBinder binder = (BluetoothService.MyBinder) service;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Triggers when the component is unbound with the object
            Log.d(TAG, "onServiceDisconnected: disconnected to service");
            mBinder.postValue(null);
        }
    };

    // Var Getters and setters
    public LiveData<Boolean> getIsUpdating() { return mIsUpdating; } // This will call when the bluetooth is connecting

    public LiveData<BluetoothService.MyBinder> getBinder() { return mBinder; }  // returns a bind to the MainActivity

    public ServiceConnection getServiceConnection() { return serviceConnection; }   // returns the service connection

    public void setIsUpdating(Boolean isUpdating) { mIsUpdating.postValue(isUpdating); }    // sets the state of the bluetooth state

    public LiveData<Boolean> getIsBtnState() { return mIsBtnState; }

    public void setIsBtnState(Boolean isBtnState) { mIsBtnState.postValue(isBtnState); }

    public LiveData<Boolean> getFallStateTrigger() { return mIsFallStateTrigger; }

    public void setFallStateTrigger(Boolean isTriggerUpdate) { mIsFallStateTrigger.postValue(isTriggerUpdate); }

    public LiveData<String> getFallState() { return mFallState; }

    public void setFallState(String state) { mFallState.postValue(state); }

}