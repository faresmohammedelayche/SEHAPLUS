package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class UserInfo extends AppCompatActivity {

    private EditText firstName, lastName;
    private CheckBox agreeCheckBox;
    private Button saveInfoBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // ربط الحقول بعناصر الواجهة
        firstName = findViewById(R.id.first_name_input);
        lastName = findViewById(R.id.last_name_input);
        agreeCheckBox = findViewById(R.id.myCheckBox);
        saveInfoBtn = findViewById(R.id.next_btn);

        // تهيئة Firebase Auth و Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // تعطيل زر "التالي" في البداية
        saveInfoBtn.setEnabled(false);

        // التحقق من إدخال المعلومات والموافقة على الشروط
        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> validateInput());

        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        lastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // تعيين حدث عند الضغط على زر "التالي"
        saveInfoBtn.setOnClickListener(v -> saveUserInfo());
    }

    /**
     * التحقق من صحة الإدخال وتفعيل زر "التالي" إذا كانت جميع الشروط مستوفاة
     */
    private void validateInput() {
        String firstname = firstName.getText().toString().trim();
        String lastname = lastName.getText().toString().trim();
        boolean isChecked = agreeCheckBox.isChecked();

        boolean isValid = !TextUtils.isEmpty(firstname) && !TextUtils.isEmpty(lastname) && isChecked;

        saveInfoBtn.setEnabled(isValid);
    }

    /**
     * حفظ معلومات المستخدم في Firestore بنفس الوثيقة (document) التي تحتوي على رقم الهاتف
     */
    private void saveUserInfo() {
        String firstname = firstName.getText().toString().trim();
        String lastname = lastName.getText().toString().trim();

        if (TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // الحصول على معرف المستخدم الحالي
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // إنشاء مرجع إلى مستند المستخدم في Firestore
        DocumentReference userRef = firestore.collection("Users").document(userId);

        // إنشاء بيانات جديدة لتحديث الوثيقة
        Map<String, Object> userData = new HashMap<>();
        userData.put("first_name", firstname);
        userData.put("last_name", lastname);

        // تحديث البيانات في نفس الوثيقة
        userRef.update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserInfo.this, "Information saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserInfo.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserInfo", "Failed to save information", e);
                    Toast.makeText(UserInfo.this, "Failed to save information", Toast.LENGTH_SHORT).show();
                });
    }
}
