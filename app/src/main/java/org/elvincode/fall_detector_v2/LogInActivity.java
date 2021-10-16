package org.elvincode.fall_detector_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.elvincode.fall_detector_v2.databinding.ActivityLogInBinding;
import org.elvincode.fall_detector_v2.databinding.ActivityMainBinding;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth fireAuth;
    private AlertDialog.Builder passwordResetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        binding = ActivityLogInBinding.inflate(getLayoutInflater()); // Its like the findViewById() function but makes it easier for us
        setContentView(binding.getRoot());

        fireAuth = FirebaseAuth.getInstance();
        passwordResetDialog = new AlertDialog.Builder(this);

        binding.btnSignUpHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                finish();
            }
        });

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.edtTxtEmail.getText().toString();
                String password = binding.edtTxtPassword.getText().toString();

                if(binding.edtTxtEmail.getText().toString().equals("")){
                    binding.inputEmail.setError("Enter Email");
                    binding.inputEmail.setErrorEnabled(true);
                    return;
                }
                binding.inputEmail.setErrorEnabled(false);

                if(binding.edtTxtPassword.getText().toString().equals("")){
                    binding.inputPassword.setError("Enter Password");
                    binding.inputPassword.setErrorEnabled(true);

                    return;
                }
                binding.inputPassword.setErrorEnabled(false);

                // Authenticate the User
                fireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LogInActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(LogInActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        binding.btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetEmail = new EditText(v.getContext());

                passwordResetDialog.setTitle("Reset Password?")
                        .setMessage("Enter Your Email To Receive Link")
                        .setView(resetEmail)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Extract the email and sent reset link
                        String mail = resetEmail.getText().toString();
                        fireAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LogInActivity.this, "Reset Link Sent To Your Address", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LogInActivity.this, "Error! Reset Link Is Not Sent " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).setNegativeButton("No", null)
                .create().show();

            }
        });
    }
}