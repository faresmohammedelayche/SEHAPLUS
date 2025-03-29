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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VerifierOTP extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText codeInput1, codeInput2, codeInput3, codeInput4, codeInput5, codeInput6;
    private String verificationId, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier_otp);

        // ربط الحقول بملف XML
        codeInput1 = findViewById(R.id.code_input1);
        codeInput2 = findViewById(R.id.code_input2);
        codeInput3 = findViewById(R.id.code_input3);
        codeInput4 = findViewById(R.id.code_input4);
        codeInput5 = findViewById(R.id.code_input5);
        codeInput6 = findViewById(R.id.code_input6);
        TextView resendCode = findViewById(R.id.button_resend);

        // تهيئة Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // الحصول على معرف التحقق ورقم الهاتف
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER"); // استقبال الرقم الصحيح

        TextView phoneNumberText = findViewById(R.id.textView3);
        if (phoneNumber != null) {
            phoneNumberText.setText(phoneNumber);
        }


        // إعداد التنقل بين الحقول
        setupOTPInputs();

        // إعادة إرسال OTP عند الضغط على زر "إعادة الإرسال"
        resendCode.setOnClickListener(v -> resendOTP());
    }

    private void setupOTPInputs() {
        EditText[] otpFields = {codeInput1, codeInput2, codeInput3, codeInput4, codeInput5, codeInput6};

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;

            otpFields[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        if (index < otpFields.length - 1) {
                            otpFields[index + 1].requestFocus();
                        } else {
                            verifyOTP();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (index > 0 && otpFields[index].getText().toString().isEmpty()) {
                        otpFields[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    private void verifyOTP() {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // تجميع الكود المدخل
        String code = codeInput1.getText().toString().trim() +
                codeInput2.getText().toString().trim() +
                codeInput3.getText().toString().trim() +
                codeInput4.getText().toString().trim() +
                codeInput5.getText().toString().trim() +
                codeInput6.getText().toString().trim();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        // إنشاء بيانات الاعتماد
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // تسجيل الدخول باستخدام بيانات الاعتماد
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            savePhoneNumber(user.getUid());
                        }
                    } else {
                        Toast.makeText(VerifierOTP.this, "Verification failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void savePhoneNumber(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("phone", phoneNumber);

        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(VerifierOTP.this, "Phone number saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifierOTP.this, UserInfo.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VerifierOTP.this, "Failed to save phone number", Toast.LENGTH_SHORT).show();
                });
    }

    private void resendOTP() {
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        // تنفيذ إعادة إرسال OTP هنا
    }
}



