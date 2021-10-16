package org.elvincode.fall_detector_v2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.elvincode.fall_detector_v2.databinding.ActivityProfilePageBinding;

public class ProfilePageActivity extends AppCompatActivity {

    private ActivityProfilePageBinding  binding;
    private MaterialToolbar topBarHome;
    private BottomNavigationView bottomNav;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ---------------------- Init Views ------------------ //
        topBarHome = findViewById(R.id.appBar_home);
        bottomNav = findViewById(R.id.bottom_navigation);
        storageReference = FirebaseStorage.getInstance().getReference();

        setProfileDetails();

        // ----------------------Top Bar Functions --------------//
        topBarHome.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_settings:
                        Toast.makeText(getApplicationContext(), "Settings Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_bluetooth:
                        Toast.makeText(getApplicationContext(), "Bluetooth Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_about:
                        Toast.makeText(getApplicationContext(), "About Us Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.subMenu_logOut:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(), "Logging Out...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        // ----------------------Bottom NavBar Functions --------------//
        bottomNav.setOnItemSelectedListener(item -> {
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
    }

    private void setProfileDetails(){
        DocumentReference dr = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dr.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String name = value.getString("first_name")  + " " + value.getString("last_name");
                binding.txtName.setText(name);
                binding.txtEmail.setText(value.getString("email"));
                binding.txtContact.setText(value.getString("contact"));
                if(value.get("date") != null && value.get("month") != null && value.get("year") != null){
                    String birthday = value.get("date").toString() + "/" + value.get("month").toString() +"/" + value.get("year").toString();
                    binding.txtBirthday.setText(birthday);
                }
                binding.txtGender.setText(value.getString("gender"));
                binding.txtCondition.setText(value.getString("condition"));

                StorageReference fileRef = storageReference.child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/profile.jpg");
                if(fileRef != null){
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(getApplicationContext()).load(uri).into(binding.imgProfilePic);
                        }
                    });
                }
            }
        });
    }
}