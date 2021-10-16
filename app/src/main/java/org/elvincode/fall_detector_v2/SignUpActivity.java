package org.elvincode.fall_detector_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.elvincode.fall_detector_v2.databinding.ActivitySignUpBinding;
import org.elvincode.fall_detector_v2.models.User;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private ActivitySignUpBinding binding;
    private String firstName, lastName, email, number, password;
    private FirebaseAuth fireAuth;
    private FirebaseFirestore db;
    private String userId;

    private final String KEY_USER = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Sets the Activity Binding for component access
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialises the Authentic instance for the user identification
        fireAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();   // Access to the data base of the users

        // Checks the on create if the user is already signe in from an existing account
        if(fireAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    public void signUpRegister(View v){
        initRegister();
    }

    public void logInRegister(View v){
        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
        finish();
    }

    // Gets the user input from the edit text views and sends it into the Firebase Firestore database
    private void initRegister() {
        Log.d(TAG, "initRegister: Started");
        firstName = binding.edtTxtFirstName.getText().toString();
        lastName = binding.edtTxtLastName.getText().toString();
        email = binding.edtTxtEmail.getText().toString();
        number = binding.edtTxtNumber.getText().toString();
        password = binding.edtTxtPassword.getText().toString();

        if(validateData()){     // Validate User Data

            // Creates a user in the Firebase Authentication
            fireAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Checks if the user data is created in the server
                    if(task.isSuccessful()){
                        Toast.makeText(SignUpActivity.this, "User Created", Toast.LENGTH_SHORT).show();

                        // Takes in the new current users ID
                        userId = fireAuth.getCurrentUser().getUid();

                        // Gets a reference to the new collection USERS and gain access on the document USER_ID
                        DocumentReference docReference = db.collection(KEY_USER).document(userId);

                        // Initialises the data set in the user object
                        User user = new User(userId, firstName, lastName, email, number);
                        docReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: user Profile is created for" +  userId);
                            }
                        });

                        // After initialising all data into the Firestore database we call into finishing the account
                        Intent intent = new Intent(getApplicationContext(), FinishCreatingAccountActivity.class);
                        intent.putExtra("user_id", userId);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(SignUpActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Checks the user input if all edit values are stated
    private boolean validateData() {
        Log.d(TAG, "validateData: Started");
        if(binding.edtTxtFirstName.getText().toString().equals("")){
            binding.inputFirstName.setError("Enter First Name");
            binding.inputFirstName.setErrorEnabled(true);
            return false;
        }
        binding.inputFirstName.setErrorEnabled(false);

        if(binding.edtTxtLastName.getText().toString().equals("")){
            binding.inputLastName.setError("Enter Last Name");
            binding.inputLastName.setErrorEnabled(true);
            return false;
        }
        binding.inputLastName.setErrorEnabled(false);

        if(binding.edtTxtEmail.getText().toString().equals("")){
            binding.inputEmail.setError("Enter Email");
            binding.inputEmail.setErrorEnabled(true);
            return false;
        }
        binding.inputEmail.setErrorEnabled(false);

        if(binding.edtTxtNumber.getText().toString().equals("")){
            binding.inputNumber.setError("Enter Phone Number");
            binding.inputNumber.setErrorEnabled(true);
            return false;
        }
        binding.inputNumber.setErrorEnabled(false);

        if(binding.edtTxtPassword.getText().toString().equals("")){
            binding.inputPassword.setError("Enter Password");
            binding.inputPassword.setErrorEnabled(true);
            return false;
        }
        binding.inputPassword.setErrorEnabled(false);

        if(binding.edtTxtConfirmPassword.getText().toString().equals("")){
            binding.inputConfirmPassword.setError("Enter Password Again");
            binding.inputConfirmPassword.setErrorEnabled(true);
            return false;
        }
        binding.inputConfirmPassword.setErrorEnabled(false);

        if(!binding.edtTxtConfirmPassword.getText().toString().equals(binding.edtTxtPassword.getText().toString())){
            binding.inputConfirmPassword.setError("Password Doesn't Match!");
            binding.inputConfirmPassword.setErrorEnabled(true);
            return false;
        }
        binding.inputConfirmPassword.setErrorEnabled(false);

        return true;
    }
}