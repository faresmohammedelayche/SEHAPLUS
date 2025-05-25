package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class NurseProfileActivity extends AppCompatActivity {

    private TextView nameTextView, specialtyTextView, priceTextView;
    private TextView aboutEditText;
    private ImageView profileImage;
    private FirebaseFirestore db;
    private String nurseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_profile);

        nameTextView = findViewById(R.id.textView16);
        specialtyTextView = findViewById(R.id.textView17);
        priceTextView = findViewById(R.id.textView18);
        aboutEditText = findViewById(R.id.editTextTextMultiLine);
        profileImage = findViewById(R.id.imageView8);
        Button consultButton = findViewById(R.id.button14);
        ImageView backButton = findViewById(R.id.imageView9);
        ImageView moreButton = findViewById(R.id.more); // Three dots button

        db = FirebaseFirestore.getInstance();
        nurseId = getIntent().getStringExtra("nurseId");

        if (nurseId != null) {
            loadNurseData(nurseId);
        } else {
            Toast.makeText(this, "No nurse ID provided", Toast.LENGTH_SHORT).show();
        }

        backButton.setOnClickListener(v -> finish());

        consultButton.setOnClickListener(view -> GotoConsult());

        moreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(NurseProfileActivity.this, moreButton);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_favorite) {
                    Toast.makeText(NurseProfileActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_share) {
                    Toast.makeText(NurseProfileActivity.this, "Shared successfully", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_report) {
                    Toast.makeText(NurseProfileActivity.this, "Reported", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void GotoConsult() {
        Intent intent = new Intent(NurseProfileActivity.this,LocationSelectionActivity.class);
        startActivity(intent);
    }

    private void loadNurseData(String nurseId) {
        db.collection("nurses")
                .document(nurseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                        String specialty = documentSnapshot.getString("speciality");
                        Long price = documentSnapshot.getLong("price");
                        String about = documentSnapshot.getString("about");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        nameTextView.setText(name);
                        specialtyTextView.setText(specialty);
                        priceTextView.setText(price + " DA");
                        aboutEditText.setText(about);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.user); // Default image
                        }
                    } else {
                        Toast.makeText(this, "Nurse not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading nurse data", Toast.LENGTH_SHORT).show()
                );
    }
}
