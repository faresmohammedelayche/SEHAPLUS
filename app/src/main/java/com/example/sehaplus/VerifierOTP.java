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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifierOTP extends AppCompatActivity {

    private EditText codeInput1, codeInput2, codeInput3, codeInput4, codeInput5, codeInput6;
    private String verificationId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifier_otp);

        // ربط الحقول في XML
        codeInput1 = findViewById(R.id.code_input1);
        codeInput2 = findViewById(R.id.code_input2);
        codeInput3 = findViewById(R.id.code_input3);
        codeInput4 = findViewById(R.id.code_input4);
        codeInput5 = findViewById(R.id.code_input5);
        codeInput6 = findViewById(R.id.code_input6);
        TextView resendCode = findViewById(R.id.button_resend); // تأكد أن الـ ID موجود في XML

        mAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");

        // تفعيل الانتقال التلقائي بين الحقول
        setupOTPInputs();

        // عند الضغط على زر "إعادة الإرسال"
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
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus(); // الانتقال إلى التالي تلقائيًا
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpFields[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (index > 0 && otpFields[index].getText().toString().isEmpty()) {
                        otpFields[index - 1].requestFocus(); // الرجوع للخلف عند الحذف
                    }
                }
                return false;
            });
        }
        verifyOTP();
    }

    private void verifyOTP() {
        // تجميع الكود من جميع الحقول
        String code = codeInput1.getText().toString().trim() +
                codeInput2.getText().toString().trim() +
                codeInput3.getText().toString().trim() +
                codeInput4.getText().toString().trim() +
                codeInput5.getText().toString().trim() +
                codeInput6.getText().toString().trim();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            Toast.makeText(this, "Please enter full OTP code", Toast.LENGTH_SHORT).show();
            return;
        }

        // التحقق من الكود في Firebase
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifierOTP.this, "Verified successfully!", Toast.LENGTH_SHORT).show();
                        startActivity (new Intent(VerifierOTP.this, UserInfo.class));
                        finish();
                    } else {
                        Toast.makeText(VerifierOTP.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOTP() {
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        // تنفيذ منطق إعادة إرسال OTP عبر Firebase هنا
    }
}
