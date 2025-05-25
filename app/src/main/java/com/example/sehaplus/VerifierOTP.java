package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerifierOTP extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText[] codeInputs;
    private String verificationId, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier_otp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        codeInputs = new EditText[]{
                findViewById(R.id.code_input1),
                findViewById(R.id.code_input2),
                findViewById(R.id.code_input3),
                findViewById(R.id.code_input4),
                findViewById(R.id.code_input5),
                findViewById(R.id.code_input6)
        };

        TextView resendCode = findViewById(R.id.button_resend);
        TextView phoneNumberText = findViewById(R.id.textView3);

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        if (phoneNumber != null) {
            phoneNumberText.setText(phoneNumber);
        }

        setupOTPInputs();

        resendCode.setOnClickListener(v -> resendOTP());
    }

    private void setupOTPInputs() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;

            codeInputs[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        if (index < codeInputs.length - 1) {
                            codeInputs[index + 1].requestFocus();
                        } else {
                            verifyOTP();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            codeInputs[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (index > 0 && codeInputs[index].getText().toString().isEmpty()) {
                        codeInputs[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    private void verifyOTP() {
        if (verificationId == null) {
            Toast.makeText(this, "Enter Code", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder codeBuilder = new StringBuilder();
        for (EditText input : codeInputs) {
            codeBuilder.append(input.getText().toString().trim());
        }

        String code = codeBuilder.toString();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            Toast.makeText(this, "Complete Code", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            savePhoneNumber(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Error,Try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void savePhoneNumber(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("phone", phoneNumber);

        db.collection("Users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Phone Number Saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, UserInfo.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error in Saving", Toast.LENGTH_SHORT).show()
                );
    }

    private void resendOTP() {
        Toast.makeText(this, "Resending verification code...", Toast.LENGTH_SHORT).show();

    }
}
