package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class UserInfo extends AppCompatActivity {

    private EditText firstName,lastName;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        firstName = findViewById(R.id.first_name_input);
        lastName = findViewById(R.id.last_name_input);
        Button saveInfoBtn = findViewById(R.id.next_btn);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        saveInfoBtn.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        String firstname = firstName.getText().toString().trim();
        String lastname = lastName.getText().toString().trim();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (TextUtils.isEmpty(firstname)) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(firstname, lastname);
        databaseRef.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserInfo.this, "Information saved successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserInfo.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(UserInfo.this, "Failed to save information", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class User {
        public String firstname, lastname;
        public User(String lastname, String firstname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }
    }
}
