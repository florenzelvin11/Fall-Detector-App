package org.elvincode.fall_detector_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.elvincode.fall_detector_v2.R;
import org.elvincode.fall_detector_v2.databinding.ActivityAddContactsBinding;
import org.elvincode.fall_detector_v2.models.ContactsModel;

public class AddContactsActivity extends AppCompatActivity {

    private static final String TAG = "AddContactsActivity";

    // Binding Components
    ActivityAddContactsBinding mBinding;

    // UI Components
    private EditText txtName, txtPhoneNumber;
    private Button btnAddContact;
    private TextView btnGoBack;

    // DataBase inits
    private String userId;
    private final String KEY_USER = "users";
    private final String KEY_CONTACTS = "contacts";
    private CollectionReference colRef;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        // Binding Init
        mBinding = ActivityAddContactsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // Database Inits
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userId != null){
            colRef = FirebaseFirestore.getInstance().collection(KEY_USER).document(userId).collection(KEY_CONTACTS);
        }
        // UI Component Inits
        txtName = findViewById(R.id.edtTxtName);
        txtPhoneNumber = findViewById(R.id.edtTxtNumber);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnGoBack = findViewById(R.id.btnGoBack);

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateData()){
                    String mName = txtName.getText().toString();
                    String mNumber = txtPhoneNumber.getText().toString();
                    if(colRef != null){
                        ContactsModel contact = new ContactsModel(mName, mNumber);
                        docRef = colRef.document(mName);
                        docRef.set(contact).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.e(TAG, "onSuccess: Added Contact");
                            }
                        });
                    }

                    // Go Back to HomePage
                    Toast.makeText(getApplicationContext(), "Home Selected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go Back to HomePage
                Toast.makeText(getApplicationContext(), "Home Selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateData(){
        if(txtName.getText().toString().equals("")){
            mBinding.inputName.setError("Enter a Name");
            mBinding.inputName.setErrorEnabled(true);
            return false;
        }
        mBinding.inputName.setErrorEnabled(false);

        if(txtPhoneNumber.getText().toString().equals("")){
            mBinding.inputPhoneNumber.setError("Enter a Phone Number");
            mBinding.inputPhoneNumber.setErrorEnabled(true);
            return false;
        }
        mBinding.inputPhoneNumber.setErrorEnabled(false);

        return true;
    }
}