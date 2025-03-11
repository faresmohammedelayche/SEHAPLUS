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

    // حقول إدخال كود التحقق (6 خانات)
    private EditText codeInput1, codeInput2, codeInput3, codeInput4, codeInput5, codeInput6;

    // معرف التحقق المرسل من Firebase
    private String verificationId;

    // متغير المصادقة عبر Firebase
    private FirebaseAuth mAuth;

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

        // تهيئة المصادقة عبر Firebase
        mAuth = FirebaseAuth.getInstance();

        // الحصول على معرف التحقق المرسل من الشاشة السابقة
        verificationId = getIntent().getStringExtra("verificationId");

        // إعداد انتقال الإدخال بين الحقول
        setupOTPInputs();

        // عند الضغط على زر "إعادة إرسال الرمز"
        resendCode.setOnClickListener(v -> resendOTP());
    }

    /**
     * إعداد انتقال الإدخال بين حقول الـ OTP
     * يتم التبديل التلقائي إلى الحقل التالي بعد إدخال رقم واحد
     * وعند الحذف يتم الرجوع إلى الحقل السابق
     */
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
                            otpFields[index + 1].requestFocus(); // الانتقال إلى الحقل التالي تلقائيًا
                        } else {
                            verifyOTP(); // التحقق عند إدخال آخر رقم
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // عند الضغط على زر الحذف (Backspace) يتم الرجوع للخلف
            otpFields[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (index > 0 && otpFields[index].getText().toString().isEmpty()) {
                        otpFields[index - 1].requestFocus(); // الرجوع للحقل السابق عند الحذف
                    }
                }
                return false;
            });
        }
    }

    /**
     * التحقق من كود OTP المدخل من قبل المستخدم
     */
    private void verifyOTP() {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // تجميع الأرقام المدخلة في الحقول لإنشاء كود OTP كامل
        String code = codeInput1.getText().toString().trim() +
                codeInput2.getText().toString().trim() +
                codeInput3.getText().toString().trim() +
                codeInput4.getText().toString().trim() +
                codeInput5.getText().toString().trim() +
                codeInput6.getText().toString().trim();

        // التأكد من أن الكود مكون من 6 أرقام
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            return;
        }

        // إنشاء بيانات الاعتماد باستخدام الكود المدخل ومعرف التحقق
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // تسجيل الدخول باستخدام Firebase Authentication
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نجاح التحقق، الانتقال إلى الشاشة التالية
                        Toast.makeText(VerifierOTP.this, "Verified successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerifierOTP.this, UserInfo.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // فشل التحقق، عرض رسالة خطأ
                        Toast.makeText(VerifierOTP.this, "Error, Try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * تنفيذ إعادة إرسال كود التحقق OTP
     */
    private void resendOTP() {
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        // هنا يجب تنفيذ إرسال الرمز من جديد باستخدام Firebase Authentication
    }
}
