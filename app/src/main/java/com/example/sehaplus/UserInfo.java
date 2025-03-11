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

    // تعريف حقول إدخال الاسم الأول واسم العائلة
    private EditText firstName, lastName;

    // كائن المصادقة عبر Firebase
    private FirebaseAuth mAuth;

    // مرجع قاعدة البيانات في Firebase
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // ربط الحقول بعناصر واجهة المستخدم في ملف XML
        firstName = findViewById(R.id.first_name_input);
        lastName = findViewById(R.id.last_name_input);
        Button saveInfoBtn = findViewById(R.id.next_btn);

        // تهيئة Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // تحديد مرجع قاعدة البيانات "Users" في Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // تعيين حدث عند الضغط على زر الحفظ
        saveInfoBtn.setOnClickListener(v -> saveUserInfo());
    }

    /**
     * حفظ معلومات المستخدم في قاعدة بيانات Firebase
     */
    private void saveUserInfo() {
        String firstname = firstName.getText().toString().trim();
        String lastname = lastName.getText().toString().trim();

        // الحصول على معرف المستخدم الحالي من Firebase Auth
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // التحقق من أن حقل الاسم الأول غير فارغ
        if (TextUtils.isEmpty(firstname)) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        }

        // إنشاء كائن المستخدم لتخزين البيانات
        User user = new User(firstname, lastname);

        // حفظ بيانات المستخدم في قاعدة البيانات تحت معرف المستخدم
        databaseRef.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نجاح العملية، عرض رسالة وإعادة التوجيه إلى الصفحة الرئيسية
                        Toast.makeText(UserInfo.this, "Information saved successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserInfo.this, MainActivity.class));
                        finish();
                    } else {
                        // فشل العملية، عرض رسالة خطأ
                        Toast.makeText(UserInfo.this, "Failed to save information", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * كلاس يمثل كائن المستخدم لتخزين بياناته في قاعدة البيانات
     */
    static class User {
        public String firstname, lastname;

        // **تصحيح خطأ في ترتيب المعاملات في المُنشئ**
        public User(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }
    }
}
