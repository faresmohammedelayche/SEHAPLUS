package com.example.sehaplus;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class editProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "6ae56faaa68b4fbc1c460819a90bf4f6"; // ضع مفتاح API هنا

    private EditText editFirstName, editLastName , editPhoneNumber ,editemail ,editAdditionalPhone, editMedicalCondition;

    private ImageView profilePic , btnChangePic;

    private Spinner bloodType , gender;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private Uri imageUri;

    private EditText btnSelectBirthDate;
    private String selectedBirthDate = "";
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker userMarker;
    private EditText etCoordinates;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;




    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        editemail =findViewById(R.id.editEmail);
        editAdditionalPhone =findViewById(R.id.editAdditionalPhoneNumber);
        profilePic = findViewById(R.id.profileImage);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnSave = findViewById(R.id.btnSave);
        bloodType=findViewById(R.id.spinnerBloodType);
        gender=findViewById(R.id.spinnerGender);
        btnSelectBirthDate = findViewById(R.id.editbirthday);
        editMedicalCondition=findViewById(R.id.editMedicalConditions);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");

        loadUserData();

        profilePic.setOnClickListener(v -> openFileChooser());
        btnChangePic.setOnClickListener(v -> openFileChooser());
        btnSave.setOnClickListener(v -> saveUserData());
        btnSelectBirthDate.setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.button_back).setOnClickListener(v -> goToMain());

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));

        // ربط المتغيرات بعناصر XML
        mapView = findViewById(R.id.mapView);
        etCoordinates = findViewById(R.id.et_coordinates);


        // تهيئة الخريطة
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true); // تمكين اللمس المتعدد
        mapView.getController().setZoom(15.0); // تحديد مستوى التكبير الافتراضي// تفعيل اللمس المتعدد
        mapView.setBuiltInZoomControls(true); // إظهار أزرار التكبير والتصغير
        mapView.setScrollableAreaLimitDouble(null); // إلغاء أي قيود على التحريك


// إزالة أي قيود على الحركة
        mapView.getOverlays().remove(locationOverlay); // إزالة الطبقة التي قد تحد من الحركة

// تمكين المستخدم من تحريك الخريطة دون التأثير على الإحداثيات
        mapView.setOnTouchListener(null);

        // إضافة طبقة تحديد الموقع
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // إضافة علامة على الموقع
        userMarker = new Marker(mapView);
        userMarker.setDraggable(true);
        mapView.getOverlays().add(userMarker);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint myLocation = locationOverlay.getMyLocation();
            if (myLocation != null) {
                updateLocation(myLocation);
            }
        }));


        mapView.getController().setZoom(18.0); // تكبير أكثر للحصول على تفاصيل دقيقة
        mapView.getController().setCenter(new GeoPoint(36.7528, 3.0422)); // ضع أي إحداثيات تريد
        userMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // عند بدء السحب
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // أثناء السحب
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // تحديث الإحداثيات بعد انتهاء السحب
                GeoPoint newPosition = marker.getPosition();
                etCoordinates.setText(newPosition.getLatitude() + ", " + newPosition.getLongitude());
            }
        });


        // التأكد من الأذونات
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        // ضبط الموقع الافتراضي عند تشغيل التطبيق
        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint myLocation = locationOverlay.getMyLocation();
            if (myLocation != null) {
                updateLocation(myLocation);
            }
        }));

        // تحديث الإحداثيات عند تحريك العلامة
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                IGeoPoint touchedPoint = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                updateLocation(new GeoPoint(touchedPoint.getLatitude(), touchedPoint.getLongitude()));
            }
            return false;
        });

    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        boolean permissionsNeeded = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded = true;
                break;
            }
        }
        if (permissionsNeeded) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
        }
    }




    private void updateLocation(GeoPoint newPoint) {
        userMarker.setPosition(newPoint); // تحديث موقع العلامة
        mapView.getController().animateTo(newPoint); // تحريك الكاميرا إلى الموقع الجديد
        etCoordinates.setText(newPoint.getLatitude() + ", " + newPoint.getLongitude());

        // حفظ الإحداثيات في Firestore
        saveCoordinatesToFirestore(newPoint.getLatitude(), newPoint.getLongitude());
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
                editPhoneNumber.setText(documentSnapshot.getString("phone"));
                editemail.setText(documentSnapshot.getString("Email"));
                editAdditionalPhone.setText(documentSnapshot.getString("additional_phone"));
                editMedicalCondition.setText(documentSnapshot.getString("Medical_Condition"));
                selectedBirthDate = documentSnapshot.getString("birthday");
                if (selectedBirthDate != null) {
                    btnSelectBirthDate.setText(selectedBirthDate);
                }
                Double latitude = documentSnapshot.getDouble("latitude");
                Double longitude = documentSnapshot.getDouble("longitude");

                if (latitude != null && longitude != null) {
                    GeoPoint userLocation = new GeoPoint(latitude, longitude);
                    updateLocation(userLocation);
                }

                String savedBloodType = documentSnapshot.getString("blood_type");
                String savedGender = documentSnapshot.getString("gender");

                if (savedBloodType != null) {
                    setSpinnerSelection(bloodType, savedBloodType);
                }
                if (savedGender != null) {
                    setSpinnerSelection(gender, savedGender);
                }
                String imageUrl = documentSnapshot.getString("profile_image");
                if (!TextUtils.isEmpty(imageUrl)) {
                    Picasso.get().load(imageUrl).into(profilePic);
                }
            }
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error loading user data", e));
    }

    private void saveCoordinatesToFirestore(double latitude, double longitude) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("latitude", latitude);
        coordinates.put("longitude", longitude);

        userRef.update(coordinates)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "تم حفظ الإحداثيات بنجاح"))
                .addOnFailureListener(e -> Log.e("Firestore", "فشل في الحفظ", e));
    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedBirthDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            btnSelectBirthDate.setText(selectedBirthDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
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
        String phoneNumber= editPhoneNumber.getText().toString().trim();
        String Email = editemail.getText().toString().trim();
        String AdditionalPhone = editAdditionalPhone.getText().toString().trim();
        String selectedBloodType = bloodType.getSelectedItem().toString();
        String selectedGender = gender.getSelectedItem().toString();
        String medicalCondition = editMedicalCondition.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)||TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("first_name", firstName);
        userUpdates.put("last_name", lastName);
        userUpdates.put("phone",phoneNumber);
        userUpdates.put("Email",Email);
        userUpdates.put("additional_phone",AdditionalPhone);
        userUpdates.put("blood_type", selectedBloodType);
        userUpdates.put("gender", selectedGender);
        userUpdates.put("birthday", selectedBirthDate);
        userUpdates.put("Medical_Condition", medicalCondition);

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

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

}