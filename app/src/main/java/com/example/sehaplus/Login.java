package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    // متغير لحقل إدخال رقم الهاتف
    private EditText phoneNumber;

    // متغير لمصادقة Firebase
    private FirebaseAuth mAuth;

    // متغير لتخزين معرف التحقق من الهاتف
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ربط حقل إدخال رقم الهاتف مع الـ XML
        phoneNumber = findViewById(R.id.phoneNumber);

        // تهيئة FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // عند الضغط على زر الإدخال من لوحة المفاتيح (Enter أو Done) يتم إرسال OTP
        phoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendOTP(); // استدعاء دالة إرسال OTP
                return true;
            }
            return false;
        });
    }

    // دالة لإرسال رمز التحقق OTP إلى رقم الهاتف المدخل
    private void sendOTP() {
        // الحصول على الرقم المدخل بعد إزالة الفراغات
        String number = phoneNumber.getText().toString().trim();

        // التحقق من صحة الرقم
        if (TextUtils.isEmpty(number) || number.length() < 10) {
            Toast.makeText(this, "Enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // إعداد خيارات المصادقة عبر الهاتف باستخدام Firebase
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number) // تعيين رقم الهاتف
                .setTimeout(60L, TimeUnit.SECONDS) // تحديد مهلة انتهاء صلاحية الرمز
                .setActivity(this) // تحديد النشاط الحالي
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // يتم استدعاء هذه الدالة عند التحقق التلقائي من رقم الهاتف بدون إدخال المستخدم للرمز
                        Toast.makeText(Login.this, "Automatically verified", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // يتم استدعاء هذه الدالة عند حدوث خطأ في التحقق
                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // يتم استدعاء هذه الدالة عند إرسال رمز التحقق بنجاح
                        verificationId = id;

                        // الانتقال إلى شاشة إدخال رمز التحقق OTP
                        Intent intent = new Intent(Login.this, VerifierOTP.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("PHONE_NUMBER", number); // تعديل هنا
                        startActivity(intent);
                    }
                })
                .build();

        // بدء عملية التحقق من رقم الهاتف عبر Firebase
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}


