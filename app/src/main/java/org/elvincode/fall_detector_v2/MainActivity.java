package org.elvincode.fall_detector_v2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.elvincode.fall_detector_v2.adapters.ContactListAdapter;
import org.elvincode.fall_detector_v2.adapters.HomePageRVAdapter;
import org.elvincode.fall_detector_v2.databinding.ActivityMainBinding;
import org.elvincode.fall_detector_v2.models.ContactsModel;
import org.elvincode.fall_detector_v2.models.User;
import org.elvincode.fall_detector_v2.services.BluetoothService;
import org.elvincode.fall_detector_v2.services.ServiceCallbacks;
import org.elvincode.fall_detector_v2.viewmodels.MainActivityViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {

    // Debug Var
    private static final String TAG = "MainActivity";

    // UI Binding Component
    private ActivityMainBinding mainBinding;

    // Recycler Views
    private RecyclerView rv_statusUsers, rv_contacts;
    private HomePageRVAdapter rv_status_adapter;
    private ContactListAdapter rv_contacts_adapter;

    // UI Component
    private MaterialToolbar topBarHome;
    private BottomNavigationView bottomNav;
    private ProgressBar mProgressBar;
    private TextView mTxtFallState;

    // User Details Vars
    private String userId, firstName, lastName, email, contact, age, gender, condition;
    private Uri imgProfileDp;

    // Firebase Database Vars
    private CollectionReference contactColRef;
    private DocumentReference docRef;
    private StorageReference storageReference;

    // Bluetooth Device Name and Address Vars
    private String deviceName = null;
    private String deviceAddress;

    // Other Vars
    private final String KEY_CONTACTS = "contacts";
    private BluetoothService mService;
    private MainActivityViewModel mViewModel;
    private String fall_state = "";

    ArrayList<ContactsModel> contacts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ----------------- UI COMPONENTS INITS --------------- //
        topBarHome = findViewById(R.id.appBar_home);
        bottomNav = findViewById(R.id.bottom_navigation);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        // ----------------- Firebase Database COMPONENTS INITS --------------- //
        storageReference = FirebaseStorage.getInstance().getReference();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else{
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        }
        docRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        contactColRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection(KEY_CONTACTS);

        // ----------------- View Models COMPONENTS INITS --------------- //
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Gets initial variables from the bluetooth Recycler View
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            Toast.makeText(this, "Device: " + deviceName + " connecting", Toast.LENGTH_SHORT).show();
        }

        // Start Service
        startService(); // Starts the background services

        // ----------------- Recycler View COMPONENTS INITS --------------- //
        rv_status_adapter = new HomePageRVAdapter(this);
        rv_statusUsers = findViewById(R.id.rv_homePage);
        rv_contacts_adapter = new ContactListAdapter(this);
        rv_contacts = findViewById(R.id.rv_contactPage);


        // ------------------ View Model Attributes ------------------ //
        mViewModel.getBinder().observe(this, new Observer<BluetoothService.MyBinder>() {
            @Override
            public void onChanged(BluetoothService.MyBinder myBinder) {
                if (myBinder != null) {
                    Log.d(TAG, "onChanged: connected to service");
                    mService = myBinder.getBluetoothService();
                    mService.setBoundedActivity(MainActivity.this);
                    mService.setServiceCallbacks(MainActivity.this);
                    mViewModel.setIsBtnState(mService.getBtnState());
                    mViewModel.setIsUpdating(mService.getConnectingUpdate());
                    mViewModel.setFallStateTrigger(mService.getFall_state_trigger());
                    mViewModel.setFallState(mService.getFall_state());

                } else {
                    Log.d(TAG, "onChanged: unbound from services");
                    mService = null;
                }
            }
        });

        mViewModel.getIsUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setIsUpdating(mService.getConnectingUpdate());
                        if (aBoolean) {
                            if (mViewModel.getBinder().getValue() != null) {
                                mProgressBar.setVisibility(View.VISIBLE);
                            }
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        mViewModel.getIsBtnState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setIsBtnState(mService.getBtnState());
                        if (aBoolean) {
                            if (mViewModel.getBinder().getValue() != null) {
                                //
                            }
                        } else {
                            //
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        mViewModel.getFallStateTrigger().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setFallStateTrigger(mService.getFall_state_trigger());
                        mViewModel.setFallState(mService.getFall_state());

                        if (aBoolean) {
                            // Do Something -- get date, time, location
                            fall_state = mViewModel.getFallState().getValue();
//                            mTxtFallState.setText(fall_state);
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        // ------------------ Button Actions ------------------ //

        // ----------------- Top Bar Transverse ---------------//
        topBarHome.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_add_contact:
                        Toast.makeText(getApplicationContext(), "Add Contact Selected", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), AddContactsActivity.class));
                        return true;
                    case R.id.menu_settings:
                        Toast.makeText(MainActivity.this, "Settings Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_bluetooth:
                        Toast.makeText(MainActivity.this, "Bluetooth Selected", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), BluetoothActivity.class));
                        return true;
                    case R.id.subMenu_about:
                        Toast.makeText(MainActivity.this, "About Us Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_logOut:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, "Logging Out...", Toast.LENGTH_SHORT).show();
                        mService.stopSelf();
                        startActivity(new Intent(MainActivity.this, LogInActivity.class));
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        // ---------------- Bottom Navigation ----------- //
        bottomNav.setOnItemSelectedListener( item -> {
            switch(item.getItemId()){
                case R.id.navHome:
                    Toast.makeText(MainActivity.this, "Home Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.navHistoryLog:
                    Toast.makeText(MainActivity.this, "History Log Selected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HistoryLogActivity.class));
                    break;
                case R.id.navProfile:
                    Toast.makeText(MainActivity.this, "Profile Selected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
                    break;
                default:
                    break;
            }
            return true;
        });

        // ---------------- Home Page Recycler View ---------- //
        rv_statusUsers.setAdapter(rv_status_adapter);
        rv_statusUsers.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        rv_contacts.setAdapter(rv_contacts_adapter);
        rv_contacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_contacts_adapter.setDocRef(docRef);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService();

        initProfileRecyclerView();

        initContactRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mViewModel.getBinder() != null) {
            unbindService(mViewModel.getServiceConnection());
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);

        if(userId != null){
            serviceIntent.putExtra("userCurrentID", userId);
        }

        if (deviceAddress != null && deviceName != null) {
           serviceIntent.putExtra("deviceAddress", deviceAddress.toString());
        }
        startService(serviceIntent);
    }

    private void bindService() {
        Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        bindService(serviceIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topbar_home, menu);
        return true;
    }

    @Override
    public String getDeviceName() {
        if(deviceName != null){
            return deviceName;
        }
        return null;
    }

    @Override
    public String getDeviceAddress() {
        if(deviceAddress != null){
            return deviceAddress;
        }
        return null;
    }

    private void initProfileRecyclerView(){
        ArrayList<User> users = new ArrayList<>();

        // ---------------- Accessing Current Data ------------- //
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                firstName = value.getString("first_name");
                lastName = value.getString("last_name");
                email = value.getString("email");
                contact = value.getString("contact");
                age = value.getString("stringAge");
                gender = value.getString("gender");
                condition = value.getString("condition");

                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String fall_state_temp = value.getString("health_state");
                        if(fall_state_temp != null && fall_state_temp == fall_state){
                            users.clear();
                            try {
                                StorageReference fileRef = storageReference.child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/profile.jpg");
                                if(fileRef != null){
                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imgProfileDp = uri;
                                            users.add(new User(userId, firstName, lastName, age, gender, fall_state_temp, condition, imgProfileDp, false));
                                            rv_status_adapter.setUsers(users);
                                            rv_status_adapter.notifyDataSetChanged();
                                        }
                                    });
                                }else{
                                    users.add(new User(userId, firstName, lastName, age, gender, fall_state_temp, condition, false));
                                    rv_status_adapter.setUsers(users);
                                    rv_status_adapter.notifyDataSetChanged();
                                }
                            }catch (Exception e){
                                Log.d(TAG, "Shit not working man");
                            }
                        }else{
                            if(fall_state_temp != null){
                                users.clear();
                                try {
                                    StorageReference fileRef = storageReference.child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/profile.jpg");
                                    if(fileRef != null){
                                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                imgProfileDp = uri;
                                                users.add(new User(userId, firstName, lastName, age, gender, fall_state_temp, condition, imgProfileDp, false));
                                                rv_status_adapter.setUsers(users);
                                                rv_status_adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }else{
                                        users.add(new User(userId, firstName, lastName, age, gender, fall_state_temp, condition, false));
                                        rv_status_adapter.setUsers(users);
                                        rv_status_adapter.notifyDataSetChanged();
                                    }
                                }catch (Exception e){
                                    Log.d(TAG, "Shit not working man");
                                }
                            }else{
                                users.clear();
                                try {
                                    StorageReference fileRef = storageReference.child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/profile.jpg");
                                    if(fileRef != null){
                                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                imgProfileDp = uri;
                                                users.add(new User(userId, firstName, lastName, age, gender, "N/A", condition, imgProfileDp, false));
                                                rv_status_adapter.setUsers(users);
                                                rv_status_adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }else{
                                        users.add(new User(userId, firstName, lastName, age, gender, "N/A", condition, false));
                                        rv_status_adapter.setUsers(users);
                                        rv_status_adapter.notifyDataSetChanged();
                                    }
                                }catch (Exception e){
                                    Log.d(TAG, "Shit not working man");
                                }
                            }
                        }
                    }
                };
                handler.post(runnable);
            }
        });
    }

    private void initContactRecyclerView(){

        if(contacts.size() > 0) { contacts.clear(); }

        if(contactColRef != null){
            contactColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            contacts.add(new ContactsModel(doc.getString("name"), doc.getString("phoneNumber")));
                        }
                        rv_contacts_adapter.setContacts(contacts);
                    }
                }
            });
        }
    }
}