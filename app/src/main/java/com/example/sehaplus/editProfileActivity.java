package com.example.sehaplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import okhttp3.*;

public class editProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "6ae56faaa68b4fbc1c460819a90bf4f6";

    private EditText editFirstName, editLastName, editPhoneNumber, editemail, editAdditionalPhone, editMedicalCondition;
    private ImageView profilePic, btnChangePic;
    private Spinner bloodType, gender;
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
        editemail = findViewById(R.id.editEmail);
        editAdditionalPhone = findViewById(R.id.editAdditionalPhoneNumber);
        profilePic = findViewById(R.id.profileImage);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnSave = findViewById(R.id.btnSave);
        bloodType = findViewById(R.id.spinnerBloodType);
        gender = findViewById(R.id.spinnerGender);
        btnSelectBirthDate = findViewById(R.id.editbirthday);
        editMedicalCondition = findViewById(R.id.editMedicalConditions);

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
        mapView = findViewById(R.id.mapView);
        etCoordinates = findViewById(R.id.et_coordinates);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(15.0);
        mapView.setOnTouchListener(null);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        userMarker = new Marker(mapView);
        userMarker.setDraggable(true);
        mapView.getOverlays().add(userMarker);

        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint myLocation = locationOverlay.getMyLocation();
            if (myLocation != null) {
                updateLocation(myLocation);
            }
        }));

        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(new GeoPoint(36.7528, 3.0422));
        userMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            public void onMarkerDragStart(Marker marker) {}
            public void onMarkerDrag(Marker marker) {}
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newPosition = marker.getPosition();
                etCoordinates.setText(newPosition.getLatitude() + ", " + newPosition.getLongitude());
            }
        });

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
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

    private void updateLocation(GeoPoint newPoint) {
        userMarker.setPosition(newPoint);
        mapView.getController().animateTo(newPoint);
        etCoordinates.setText(newPoint.getLatitude() + ", " + newPoint.getLongitude());
        saveCoordinatesToFirestore(newPoint.getLatitude(), newPoint.getLongitude());
    }

    private void saveCoordinatesToFirestore(double latitude, double longitude) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.update("latitude", latitude, "longitude", longitude);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    btnSelectBirthDate.setText(selectedBirthDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
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
                if (selectedBirthDate != null) btnSelectBirthDate.setText(selectedBirthDate);
                setSpinnerSelection(bloodType, documentSnapshot.getString("blood_type"));
                setSpinnerSelection(gender, documentSnapshot.getString("gender"));
                String imageUrl = documentSnapshot.getString("profile_image");
                if (!TextUtils.isEmpty(imageUrl)) Picasso.get().load(imageUrl).into(profilePic);
                Double lat = documentSnapshot.getDouble("latitude");
                Double lon = documentSnapshot.getDouble("longitude");
                if (lat != null && lon != null) updateLocation(new GeoPoint(lat, lon));
            }
        });
    }

    private void saveUserData() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("first_name", editFirstName.getText().toString());
        userData.put("last_name", editLastName.getText().toString());
        userData.put("phone", editPhoneNumber.getText().toString());
        userData.put("Email", editemail.getText().toString());
        userData.put("additional_phone", editAdditionalPhone.getText().toString());
        userData.put("Medical_Condition", editMedicalCondition.getText().toString());
        userData.put("birthday", selectedBirthDate);
        userData.put("blood_type", bloodType.getSelectedItem().toString());
        userData.put("gender", gender.getSelectedItem().toString());

        if (imageUri != null) {
            uploadImageToImgbb(imageUri, url -> {
                userData.put("profile_image", url);
                userRef.update(userData).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            userRef.update(userData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void uploadImageToImgbb(Uri imageUri, OnImageUploadListener listener) {
        String filePath = getPathFromUri(imageUri);
        if (filePath == null) return;
        File file = new File(filePath);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("key", IMGBB_API_KEY)
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        Request request = new Request.Builder().url("https://api.imgbb.com/1/upload").post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseData);
                    String imageUrl = json.getJSONObject("data").getString("url");
                    runOnUiThread(() -> listener.onImageUploaded(imageUrl));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        cursor.close();
        return path;
    }

    interface OnImageUploadListener {
        void onImageUploaded(String url);
    }
}
