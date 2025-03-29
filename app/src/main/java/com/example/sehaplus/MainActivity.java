package com.example.sehaplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView welcomeText;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        welcomeText = findViewById(R.id.welcome_text);

        findViewById(R.id.menu_button).setOnClickListener(v -> showSideSheetDialog());
        findViewById(R.id.nursing_button).setOnClickListener(v -> goToNurse());
        findViewById(R.id.store_button).setOnClickListener(v -> goToStore());

        fetchUserName();
    }

    private void fetchUserName() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "فشل في جلب البيانات", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("first_name");
                String lastName = documentSnapshot.getString("last_name");

                if (firstName != null && lastName != null) {
                    String fullName = firstName + " " + lastName;
                    welcomeText.setText(fullName);
                    welcomeText.setTextColor(Color.parseColor("#50D7C3"));
                }
            }
        });
    }

    private void goToNurse() {
        Intent intent = new Intent(MainActivity.this, nursingActivity.class);
        startActivity(intent);
    }

    private void goToStore() {
        Intent intent = new Intent(MainActivity.this, storeActivity.class);
        startActivity(intent);
    }

    private void showSideSheetDialog() {
        SideSheetDialog sideSheetDialog = new SideSheetDialog(this);
        sideSheetDialog.setContentView(R.layout.side_sheet_layout);
        sideSheetDialog.setCanceledOnTouchOutside(true);
        sideSheetDialog.setSheetEdge(Gravity.START);

        // جلب مراجع العناصر داخل SideSheet
        TextView userNameTextView = sideSheetDialog.findViewById(R.id.userName);
        TextView userPhoneTextView = sideSheetDialog.findViewById(R.id.phone);
        ImageView editIcon = sideSheetDialog.findViewById(R.id.editIcon);
        ImageView profileImageView = sideSheetDialog.findViewById(R.id.profileImage);

        // التأكد من أن العناصر غير فارغة قبل تحديثها
        if (userNameTextView != null && userPhoneTextView != null && profileImageView != null) {
            fetchUserData(userNameTextView, userPhoneTextView, profileImageView);
        }
        if (editIcon != null) {
            editIcon.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, editProfileActivity.class);
                startActivity(intent);
            });
        }
        sideSheetDialog.show();
    }

    private void fetchUserData(TextView userNameTextView, TextView userPhoneTextView, ImageView profileImageView) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "فشل في جلب البيانات", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("first_name");
                String lastName = documentSnapshot.getString("last_name");
                String phoneNumber = documentSnapshot.getString("phone");
                String profileImageUrl = documentSnapshot.getString("profile_image");

                if (firstName != null && lastName != null) {
                    userNameTextView.setText(firstName + " " + lastName);
                }
                if (phoneNumber != null) {
                    userPhoneTextView.setText(phoneNumber);
                }
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.user).into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.user);
                }
            }
        });
    }
}
