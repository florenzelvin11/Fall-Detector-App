package org.elvincode.fall_detector_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.elvincode.fall_detector_v2.adapters.HistoryLogRecyclerView;
import org.elvincode.fall_detector_v2.models.HistoryLogDisplay;
import org.elvincode.fall_detector_v2.services.BluetoothService;
import org.elvincode.fall_detector_v2.viewmodels.HistoryLogActivityViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryLogActivity extends AppCompatActivity {

    private static final String TAG = "HistoryLogActivity";

    // UI Components
    private RecyclerView mRecyclerView;
    private MaterialToolbar topBarHome;
    private BottomNavigationView bottomNav;

    // Adapters
    private HistoryLogRecyclerView mAdapter;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_log);

        // UI Components
        mRecyclerView = findViewById(R.id.rv_history_log);
        mAdapter = new HistoryLogRecyclerView(this);

        // ----------------- UI COMPONENTS INITS --------------- //
        topBarHome = findViewById(R.id.appBar_home);
        bottomNav = findViewById(R.id.bottom_navigation);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ----------------- Top Bar Transverse ---------------//
        topBarHome.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_settings:
                        Toast.makeText(getApplicationContext(), "Settings Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_bluetooth:
                        Toast.makeText(getApplicationContext(), "Bluetooth Selected", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), BluetoothActivity.class));
                        return true;
                    case R.id.subMenu_about:
                        Toast.makeText(getApplicationContext(), "About Us Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_logOut:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(), "Logging Out...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
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
                    Toast.makeText(getApplicationContext(), "Home Selected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    onResume();
                    break;
                case R.id.navHistoryLog:
                    Toast.makeText(getApplicationContext(), "History Log Selected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HistoryLogActivity.class));
                    onResume();
                    break;
                case R.id.navProfile:
                    Toast.makeText(getApplicationContext(), "Profile Selected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
                    onResume();
                    break;
                default:
                    break;
            }
            return true;
        });

        initRecyclerView();
    }

    private void initRecyclerView(){
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<HistoryLogDisplay> dataSet = new ArrayList<>();

        CollectionReference colRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("history_logs");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        Log.d(TAG, "onComplete: "+ doc.getString("dateTime") +" " + doc.getString("fallState") + " " + doc.getString("location"));
                        mAdapter.addHistoryLogs(new HistoryLogDisplay(doc.getString("dateTime"), doc.getString("fallState"), doc.getString("location")));
//                        dataSet.add(new HistoryLogDisplay(doc.getString("dateTime"), doc.getString("fallState"), doc.getString("location")));
                    }
                }
            }
        });
    }
}