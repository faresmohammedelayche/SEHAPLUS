package com.example.sehaplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
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
        db.collection("Users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");

                        if (firstName != null && lastName != null) {
                            welcomeText.setText(firstName + " " + lastName);
                            welcomeText.setTextColor(Color.parseColor("#50D7C3"));
                        }
                    }
                });
    }

    private void goToNurse() {
        startActivity(new Intent(this, nursingActivity.class));
    }

    private void goToStore() {
        startActivity(new Intent(this, storeActivity.class));
    }

    private void showSideSheetDialog() {
        SideSheetDialog sideSheetDialog = new SideSheetDialog(this);
        sideSheetDialog.setContentView(R.layout.side_sheet_layout);
        sideSheetDialog.setSheetEdge(Gravity.START);

        TextView userNameTextView = sideSheetDialog.findViewById(R.id.userName);
        TextView userPhoneTextView = sideSheetDialog.findViewById(R.id.phone);
        ImageView editIcon = sideSheetDialog.findViewById(R.id.editIcon);
        ImageView profileImage = sideSheetDialog.findViewById(R.id.profileImage);
        Button switchMode = sideSheetDialog.findViewById(R.id.btnSwitchMode);

        if (userNameTextView != null && userPhoneTextView != null && profileImage != null) {
            fetchUserData(userNameTextView, userPhoneTextView, profileImage);
        }

        if (editIcon != null) {
            editIcon.setOnClickListener(v -> startActivity(new Intent(this, editProfileActivity.class)));
        }
        sideSheetDialog.show();
    }

    private void fetchUserData(TextView userName, TextView phone, ImageView image) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String firstName = snapshot.getString("first_name");
                        String lastName = snapshot.getString("last_name");
                        String phoneNumber = snapshot.getString("phone");
                        String imageUrl = snapshot.getString("profile_image");

                        if (firstName != null && lastName != null)
                            userName.setText(firstName + " " + lastName);
                        if (phoneNumber != null)
                            phone.setText(phoneNumber);
                        if (imageUrl != null && !imageUrl.isEmpty())
                            Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(image);
                        else
                            image.setImageResource(R.drawable.user);
                    }
                });
    }
}
