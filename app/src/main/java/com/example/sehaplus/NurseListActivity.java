package com.example.sehaplus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NurseListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Nurse> nurseArrayList;
    AdabterNurse adabterNurse;
    FirebaseFirestore db;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_list);

        findViewById(R.id.button_back).setOnClickListener(v -> goTonursing());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data ...");
        progressDialog.show();

        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db =FirebaseFirestore.getInstance();
        nurseArrayList = new ArrayList<Nurse>();
        adabterNurse =new AdabterNurse(NurseListActivity.this,nurseArrayList);

        recyclerView.setAdapter(adabterNurse);

        EventChangeListener();

    }

    private void EventChangeListener() {
        db.collection("nurses").orderBy("price", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error !=null){

                            if(progressDialog.isShowing())
                                progressDialog.dismiss();

                            Log.e("Firestore error",error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType() == DocumentChange.Type.ADDED){

                               nurseArrayList.add(dc.getDocument().toObject(Nurse.class));

                            }

                            adabterNurse.notifyDataSetChanged();
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();

                        }

                    }
                });
    }

    private void goTonursing() {
        Intent intent =new Intent(NurseListActivity.this,nursingActivity.class);
        startActivity(intent);
    }
    }