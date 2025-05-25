package com.example.sehaplus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NurseListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Nurse> nurseArrayList;
    AdabterNurse adabterNurse;
    FirebaseFirestore db;
    ProgressDialog progressDialog;

    String selectedSpeciality;
    Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_list);

        findViewById(R.id.button_back).setOnClickListener(v -> goTonursing());

        selectedSpeciality = getIntent().getStringExtra("speciality");

        if (selectedSpeciality == null || selectedSpeciality.isEmpty()) {
            Toast.makeText(this, "Specialization not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        nurseArrayList = new ArrayList<>();
        adabterNurse = new AdabterNurse(this, nurseArrayList);
        recyclerView.setAdapter(adabterNurse);

        sortSpinner = findViewById(R.id.sort_spinner);
        // Set listener on spinner to reload data on sort selection change
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Clear previous list and fetch data with new order
                nurseArrayList.clear();
                fetchNursesBySpeciality(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Do nothing or load default order
            }
        });

        // Load data with default spinner selection at start
        progressDialog.show();
        fetchNursesBySpeciality(sortSpinner.getSelectedItemPosition());
    }
    private void fetchNursesBySpeciality(int position) {
        Query query = db.collection("nurses")
                .whereEqualTo("speciality", selectedSpeciality);

        switch (position) {
            case 0: // price ascending
                query = query.orderBy("price", Query.Direction.ASCENDING);
                break;
            case 1: // price descending
                query = query.orderBy("price", Query.Direction.DESCENDING);
                break;
            case 2: // name ascending
                query = query.orderBy("name", Query.Direction.ASCENDING);
                break;
            case 3: // name descending
                query = query.orderBy("name", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("price", Query.Direction.ASCENDING);
                break;
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (error != null) {
                    Log.e("Firestore error", error.getMessage());
                    return;
                }

                nurseArrayList.clear();
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Nurse nurse = dc.getDocument().toObject(Nurse.class);
                        nurse.setId(dc.getDocument().getId()); // 👈 مهم جداً
                        nurseArrayList.add(nurse);
                    }
                }

                adabterNurse.notifyDataSetChanged();
            }
        });
    }


    private void goTonursing() {
        Intent intent = new Intent(NurseListActivity.this, nursingActivity.class);
        startActivity(intent);
    }
}

