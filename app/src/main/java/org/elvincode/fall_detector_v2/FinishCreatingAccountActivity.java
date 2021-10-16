package org.elvincode.fall_detector_v2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.Resources;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.elvincode.fall_detector_v2.databinding.ActivityFinishCreatingAccountBinding;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class FinishCreatingAccountActivity extends AppCompatActivity {
    private static final String TAG = "FinishAccountActivity";

    private ActivityFinishCreatingAccountBinding binding;
    private String userId, gender, condition, stringAge;
    private Integer mdate, mmonth, myear;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private DocumentReference userReference;
    private final String KEY_USER = "users";
    private StorageReference storageReference;
    private Uri imgURI;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_creating_account);

        // Sets the Binding activity
        binding = ActivityFinishCreatingAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialises the Firebase Components
        userId = getIntent().getStringExtra("user_id");                                       // Gets Newly added userID from Sign-Up activity
        storageReference = FirebaseStorage.getInstance().getReference();                            // Firebase Storage Component
        userReference = FirebaseFirestore.getInstance().collection(KEY_USER).document(userId);      // Firebase Firestore Database access into the user collection

        // Starts Image profile select
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            imgURI = data.getData();
                            if(imgURI != null){
                                uploadImgToFirebase(imgURI);
                                binding.imgProfileDp.setImageURI(imgURI);
                                binding.btnSetProfilePic.setText("Change Profile Pic");
                            }
                        }
                    }
                });

        // Gets the data from the given Text View
        getDatePicker();
    }

    public void btnSetBirthday(View v){
        // Event Button for setting the Date of Birth
        Calendar cal = Calendar.getInstance();
        int cDate = cal.get(Calendar.DAY_OF_MONTH);
        int cMonth = cal.get(Calendar.MONTH);
        int cYear = cal.get(Calendar.YEAR);

        DatePickerDialog dialog = new DatePickerDialog(
                FinishCreatingAccountActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                cYear, cMonth, cDate);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    // Event Button for setting and the image profile
    public void btnSetProfileDp(View v){
        // Open Gallery
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(openGallery);
    }

    // Finishes the Activity and completes what the user has filled in
    public void  btnFinish(View v){

        if(validateInput()){
            stringAge = getAgeString();
            gender = binding.edtTxtGender.getText().toString();
            condition = binding.edtTxtHealthCondition.getText().toString();
            Map<String, Object> userTmp = new HashMap<>();
            userTmp.put("date", mdate);
            userTmp.put("month", mmonth);
            userTmp.put("year", myear);
            userTmp.put("stringAge", stringAge);
            userTmp.put("gender", gender);
            userTmp.put("condition", condition);
            userReference.update(userTmp).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(FinishCreatingAccountActivity.this, "Account Completed", Toast.LENGTH_SHORT).show();
                }
            });

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private boolean validateInput(){
        if(mdate == null && mmonth == null && myear == null){
            binding.inputBirthday.setError("Enter Birthday");
            binding.inputBirthday.setErrorEnabled(true);
            return false;
        }
        binding.inputBirthday.setErrorEnabled(false);

        if (binding.edtTxtGender.getText().toString().equals("")){
            binding.inputGender.setError("State your Gender");
            binding.inputGender.setErrorEnabled(true);
            return false;
        }
        binding.inputGender.setErrorEnabled(false);

        if(binding.edtTxtHealthCondition.getText().toString().equals("")){
            binding.inputHealthCondition.setError("Enter Health Condition");
            binding.inputHealthCondition.setErrorEnabled(true);
            return false;
        }
        binding.inputHealthCondition.setErrorEnabled(false);

        if(imgURI == null){
            Toast.makeText(this, "Select a Profile Photo", Toast.LENGTH_SHORT).show();
            return false;
        }

        binding.btnRegister.setText("Done");

        return true;
    }

    // Uploads the selected user profile image
    private void uploadImgToFirebase(Uri imgUri){
        StorageReference fileRef = storageReference.child("users/"+ userId +"/profile.jpg");
        fileRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(FinishCreatingAccountActivity.this, "Profile Photo Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Access the Date of Birth by the user input
    private void getDatePicker(){
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d(TAG, "onDateSet: " + dayOfMonth + "/" + month + "/" + year);
                mdate = dayOfMonth;
                mmonth = month + 1;
                myear = year;
                String birthDate = dayOfMonth + " / " + mmonth + " / " + year;
                binding.edtTxtBirthday.setText(birthDate);
            }
        };
    }

    // Calculates the Current Age of the user
    private String getAgeString(){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(myear, mmonth, mdate);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return new Integer(age).toString();
    }
}