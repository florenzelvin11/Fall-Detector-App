package org.elvincode.fall_detector_v2.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.elvincode.fall_detector_v2.models.HistoryLogDisplay;
import org.elvincode.fall_detector_v2.models.HistoryLogModel;
import org.elvincode.fall_detector_v2.repositories.HistoryLogRepositories;
import org.elvincode.fall_detector_v2.viewmodels.HistoryLogActivityViewModel;
import org.elvincode.fall_detector_v2.viewmodels.MainActivityViewModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BluetoothService extends Service {

    // Debug Var
    private static final String TAG = "BluetoothService";

    // Firebase Firestore inits
    private CollectionReference collectionReferenceHistory;
    private DocumentReference documentReferenceDateTime, userRef;

    // Database Vars
    private String userID;                                          /* User Id */
    private final String KEY_USER = "users";
    private final String USER_HISTORY_LOGS = "history_logs";

    // GPS Location Vars
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String mDateTimeKey;
    private String mFallState;
    private String mLocation;

    // Models
    private HistoryLogModel mHistoryLogModel;

    // Bluetooth Vars
    private String mDeviceName;
    private String mDeviceAddress;
    public static Handler mHandler;
    public static BluetoothSocket mSocket;

    // Activity Vars
    public static Boolean connectingUpdate, btnState, fall_state_trigger;
    private static String fall_state;

    // Service Vars
    private final IBinder mBinder = new MyBinder();
    private ServiceCallbacks serviceCallbacks;
    private Activity boundedActivity;

    // Handler Vars
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    // Connectivity Threads
    private static ConnectedThread connectedThread;
    private static CreateConnectThread createConnectThread;

    // Repositories
    private HistoryLogRepositories mRepo;

    // Contacts Vars
    private String phoneNo;
    private String emergencyMsg;

    /* ============================ Getters and Setters =================================== */

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }

    public CreateConnectThread getCreateConnectThread(){
        return createConnectThread;
    }

    public Boolean getConnectingUpdate() { return connectingUpdate; }

    public Boolean getBtnState() { return btnState; }

    public Boolean getFall_state_trigger() { return fall_state_trigger; }

    public void setFallState(String state) { fall_state = state; }

    public String getFall_state() { return fall_state; }

    public void setServiceCallbacks(ServiceCallbacks callbacks){
        serviceCallbacks = callbacks;
    }

    public void setBoundedActivity(Activity activity) { boundedActivity = activity; }
    /* ============================ onBind =================================== */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        public BluetoothService getBluetoothService(){
            return BluetoothService.this;
        }
    }

    /* ============================ On Create Service =================================== */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Initialises FusedLocationProvideClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Bluetooth Connecting State
        connectingUpdate = false;
        btnState = false;               // Button State - whether the button is enabled or disabled
        fall_state_trigger = false;     // States when a msg from arduino was received
        fall_state = "N/A";             // Initialises the text from being sent to N/A

        // Firebase Firestore Date base Inits
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (userID != null) {
            userRef = FirebaseFirestore.getInstance().collection(KEY_USER).document(userID);
            collectionReferenceHistory = FirebaseFirestore.getInstance().collection(KEY_USER).document(userID).collection(USER_HISTORY_LOGS);
        }

        if(serviceCallbacks != null){
            if(serviceCallbacks.getDeviceName() != null){
                mDeviceName = serviceCallbacks.getDeviceName();
            }
        }

        if (mDeviceName != null) {
            // Get the device address to make the Bluetooth Connection
            mDeviceAddress = serviceCallbacks.getDeviceAddress();
            // Show progress and the connection status
            Log.d(TAG, "onStartCommand: attempting to connect " + mDeviceName);
            connectingUpdate = true;

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, mDeviceAddress);
            createConnectThread.start();    // Starts the thread
        }

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                // Connected State
                                Log.d(TAG, "handleMessage: Device Connected");
                                Toast.makeText(BluetoothService.this, "Connected to " + mDeviceName, Toast.LENGTH_LONG).show();
                                btnState = true;
                                connectingUpdate = false;
                                break;
                            case -1:
                                // Failed to Connect State
                                Log.d(TAG, "handleMessage: Device Disconnected");
                                Toast.makeText(BluetoothService.this, "Device failed to connect", Toast.LENGTH_LONG).show();
                                btnState = false;
                                connectingUpdate = false;
                                break;
                        }
                        break;
                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        Log.e(TAG, "handleMessage: " + arduinoMsg.toLowerCase());
                        validateState(arduinoMsg.toLowerCase());
                        break;
                }
            }
        };

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    /* ============================ Database Actions =================================== */
    private void validateState(String msg){
        Log.e(TAG, "validateState: Activated");
        if(msg.equals("fatal fall")){
            Log.e(TAG, "Configured Message: Fatal Fall Activated");
            // Do something
            fall_state_trigger = true;      // A msg was read
            fall_state = "Hard-Fall";

            // Changes the current user health_state
            setUserFallState();

            // Gets the Date and time - then gets the location - then stores the value into the data base
            getHistoryLogs();

            // Send SMS
            validateSMS();

            // Emergency Call
            makePhoneCall();

            // Emergency Vibrate
            makePhoneVibrate();

        }else if(msg.equals("non-fatal fall")){
            // Do Something
            Log.e(TAG, "Configured Message: Non-Fatal Fall Activated");
            fall_state_trigger = true;      // A msg was read
            fall_state = "Medium-Fall";

            // Changes the current user health_state
            setUserFallState();

            // Gets the Date and time - then gets the location - then stores the value into the data base
            getHistoryLogs();

            // Send SMS
            validateSMS();

            // Emergency Vibrate
            makePhoneVibrate();

        }else if(msg.equals("normal")){
            // Do something
            Log.e(TAG, "Configured Message: Normal Activated");
            fall_state_trigger = true;      // A msg was read
            fall_state = "Normal";

            // Changes the current user health_state
            setUserFallState();

            // Stop EmergencyVibrate
            stopPhoneVibrate();

        }else{
            Log.e(TAG, "Configured Message: Nothing Activated");
            fall_state_trigger = false; // Nothing is being read
        }
    }

    private void setUserFallState(){
        if(userRef != null){
            Map<String, Object> tmpFallState = new HashMap<>();
            tmpFallState.put("health_state", fall_state);
            userRef.update(tmpFallState);
        }
    }
    private void getHistoryLogs(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User has enabled GPS on the app
            getLocation();
            getTimeDate();
            initDataBase();
        } else {
            // User has not enabled GPS for the app, ask the user to enable GPS on app
            ActivityCompat.requestPermissions(boundedActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Location> task) {
                // Initialises location
                Location location = task.getResult();
                if (location != null) {
                    try {
                        // Initialise geoCoder
                        Geocoder geocoder = new Geocoder(
                                BluetoothService.this, Locale.getDefault());

                        // Initialise address list
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        mLocation = addresses.get(0).getAddressLine(0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getTimeDate(){
        // Gets current date and time the trigger was activated
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
        String dateTime = simpleDateFormat.format(calendar.getTime());
        mDateTimeKey = dateTime;
    }

    private void initDataBase(){
        // Gets all the variables and stores it into the database
        HistoryLogDisplay historyLog = new HistoryLogDisplay(mDateTimeKey, fall_state, "UNSW"); // mLocation - Make this change after demo
        documentReferenceDateTime = collectionReferenceHistory.document(mDateTimeKey);

        documentReferenceDateTime.set(historyLog).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: history log created");
            }
        });
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public class CreateConnectThread extends Thread{

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address){
            // Use a temporary object that is later assigned to mmSocket because mmSocket is final
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try{
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

            }catch (IOException e){
                Log.e(TAG, "Socket's create() method failed",e);
                stopSelf();
            }
            mSocket = tmp;
        }

        public void run(){
            // Cancel Discovery because it otherwise slows down the connection
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try{
                mSocket.connect();
                Log.e("Status", "Device connected");
                mHandler.obtainMessage(CONNECTING_STATUS, 1,-1).sendToTarget();
            }catch (IOException connectException){
                try{
                    mSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    // Handler function
                    mHandler.obtainMessage(CONNECTING_STATUS, -1,-1).sendToTarget();
                }catch (IOException closeException){
                    Log.e(TAG, "Could not close the client socket", closeException);
                    stopSelf();
                }
                stopSelf();
                return;
            }

            // The connection attempt succeeded. Perform work associated with the connection
            // in a separate thread
            connectedThread = new ConnectedThread(mSocket);
            connectedThread.run();
        }

        // Close the client socket and causes the thread to finish
        public void cancel(){
            try{
                mSocket.close();
            }catch (IOException e){
                Log.e(TAG, "Could not close the client socket", e);
                stopSelf();
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output stream, using temp objects
            // because member streams are final
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e) { stopSelf(); }

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024]; // Buffer stores for the stream
            int bytes = 0;  // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while(true){
                try{
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInputStream.read();
                    String readMessage;
                    if(buffer[bytes] == '\n'){
                        readMessage = new String(buffer, 0,  bytes);
                        Log.e("Arduino Message", readMessage);
                        // Handler Function
                        mHandler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    }else{
                        bytes++;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    stopSelf();
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(String input){
            byte[] bytes = input.getBytes();    // Converts entered String into bytes
            try{
                mmOutputStream.write(bytes);
            }catch (IOException e){
                Log.e("Send Error", "Unable to send message", e);
                stopSelf();
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e) { stopSelf(); }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if(createConnectThread != null){
            createConnectThread.cancel();
        }

        if(connectedThread != null){
            connectedThread.cancel();
        }
        stopSelf();
    }

    private void makePhoneCall(){
        userRef.addSnapshotListener(boundedActivity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if(value != null){
                    String tmpNum = value.getString("emergencyPhoneNum");
                    if(tmpNum != null || tmpNum.equals("")){
                        phoneNo = tmpNum;
                    }
                }
            }
        });

        if(phoneNo == null) { return; }

        if(phoneNo.trim().length() > 0){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(boundedActivity, new String[]{Manifest.permission.CALL_PHONE}, 1);
            }else{
                String dial  = "tel:" + phoneNo;
                this.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }else{
            Toast.makeText(boundedActivity, "No Phone Number", Toast.LENGTH_SHORT).show();
        }
    }


    private void validateSMS(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                sendSMS();
            }else{
                ActivityCompat.requestPermissions(boundedActivity, new String[]{Manifest.permission.SEND_SMS},1);
            }
        }
    }

    private void sendSMS(){

        userRef.addSnapshotListener(boundedActivity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if(value != null){
                    String tmpNum = value.getString("emergencyPhoneNum");
                    if(tmpNum != null || tmpNum.equals("")){
                        phoneNo = tmpNum;
                    }
                }
            }
        });

        emergencyMsg = "In Need of Assistance!\n" + fall_state + "!\n At UNSW"; // use "mLocation" for final product
                                                                                        // Use of UNSW is for presentation purposes

        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, emergencyMsg, null, null);
            Toast.makeText(boundedActivity, "MessageSent", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(boundedActivity, "Failed to send msg", Toast.LENGTH_SHORT).show();
        }

    }

    private void makePhoneVibrate(){
        // Get instance of Vibrator from current Context
        Vibrator v = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 5000, 1000};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        v.vibrate(pattern, 0);
    }

    private void stopPhoneVibrate(){
        // Get instance of Vibrator from current Context
        Vibrator v = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        v.cancel();
    }
}
