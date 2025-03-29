package com.example.sehaplus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class editProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "6ae56faaa68b4fbc1c460819a90bf4f6"; // ضع مفتاح API هنا

    private EditText editFirstName, editLastName, editLocation;
    private RadioGroup radioGenderGroup;
    private ImageView profilePic;
    private Button btnChangePic, btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        radioGenderGroup = findViewById(R.id.radioGenderGroup);
        editLocation = findViewById(R.id.locationField);
        profilePic = findViewById(R.id.profileImage);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");

        loadUserData();

        profilePic.setOnClickListener(v -> openFileChooser());
        btnChangePic.setOnClickListener(v -> openFileChooser());
        btnSave.setOnClickListener(v -> saveUserData());
        findViewById(R.id.button_back).setOnClickListener(v -> goToMain());
    }

    private void goToMain() {
        Intent intent = new Intent(editProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editFirstName.setText(documentSnapshot.getString("first_name"));
                editLastName.setText(documentSnapshot.getString("last_name"));
                editLocation.setText(documentSnapshot.getString("location"));
                String gender = documentSnapshot.getString("gender");
                if ("Male".equalsIgnoreCase(gender)) {
                    ((RadioButton) findViewById(R.id.radioMale)).setChecked(true);
                } else if ("Female".equalsIgnoreCase(gender)) {
                    ((RadioButton) findViewById(R.id.radioFemale)).setChecked(true);
                }
                String imageUrl = documentSnapshot.getString("profile_image");
                if (!TextUtils.isEmpty(imageUrl)) {
                    Picasso.get().load(imageUrl).into(profilePic);
                }
            }
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error loading user data", e));
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
        }
    }

    private void saveUserData() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        int selectedGenderId = radioGenderGroup.getCheckedRadioButtonId();
        String gender = selectedGenderId == R.id.radioMale ? "Male" : "Female";

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("first_name", firstName);
        userUpdates.put("last_name", lastName);
        userUpdates.put("gender", gender);
        userUpdates.put("location", location);

        if (imageUri != null) {
            uploadImageToImgBB(userRef, userUpdates);
        } else {
            updateFirestore(userRef, userUpdates);
        }
    }

    private void uploadImageToImgBB(DocumentReference userRef, Map<String, Object> userUpdates) {
        progressDialog.setMessage("Uploading image...");
        String imagePath = getRealPathFromURI(imageUri);
        if (imagePath == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(imagePath);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("key", "6ae56faaa68b4fbc1c460819a90bf4f6") // استخدم المفتاح مباشرة
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();

        Request request = new Request.Builder().url("https://api.imgbb.com/1/upload").post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(editProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(editProfileActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                try {
                    // تحليل الاستجابة كـ JSON
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String imageUrl = jsonResponse.getJSONObject("data").getString("url");

                    // تحديث بيانات المستخدم
                    userUpdates.put("profile_image", imageUrl);
                    runOnUiThread(() -> updateFirestore(userRef, userUpdates));

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(editProfileActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateFirestore(DocumentReference userRef, Map<String, Object> userUpdates) {
        userRef.update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(editProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(editProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return uri.getPath();
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String realPath = cursor.getString(idx);
        cursor.close();
        return realPath;
    }
}
