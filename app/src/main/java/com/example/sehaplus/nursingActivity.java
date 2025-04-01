package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class nursingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nursing);
        findViewById(R.id.button_back).setOnClickListener(v -> goToMain());

        //ربط العناصر كود xml بكود java
        ImageView btnNutritionist=findViewById(R.id.Nutritionist);
        ImageView btnPhysiotherapy=findViewById(R.id.physiotherapy);
        ImageView btnPsychiatry=findViewById(R.id.psychiatry);
        ImageView btnElderlyCare=findViewById(R.id.ElderlyCare);
        ImageView btnNursing=findViewById(R.id.Nursing);
        ImageView btnPsychology=findViewById(R.id.psychology);
        ImageView btnPeadiatrician=findViewById(R.id.paediatrician);
        ImageView btnHomeopathy=findViewById(R.id.homeopathy);
        ImageView btnMidwife=findViewById(R.id.Midwife);



        btnPhysiotherapy.setOnClickListener(view -> openNurseListActivity("Physiotherapy"));

        btnPsychiatry.setOnClickListener(view -> openNurseListActivity("Psychiatry"));

        btnNutritionist.setOnClickListener(view -> openNurseListActivity("Nutritionist"));

        btnElderlyCare.setOnClickListener(view -> openNurseListActivity("ElderlyCare"));

        btnNursing.setOnClickListener(view -> openNurseListActivity("Nursing"));

        btnPsychology.setOnClickListener(view -> openNurseListActivity("Psychology"));

        btnPeadiatrician.setOnClickListener(view -> openNurseListActivity("Peadiatrician"));

        btnHomeopathy.setOnClickListener(view -> openNurseListActivity("Homeopathy"));

        btnMidwife.setOnClickListener(view -> openNurseListActivity("Midwife"));

    }
    private void goToMain() {
        Intent intent = new Intent(nursingActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //code to search


    private void openNurseListActivity(String selectedSpeciality){
        // عند النقر على التخصص
        Intent intent = new Intent(nursingActivity.this, NurseListActivity.class);
        intent.putExtra("speciality", selectedSpeciality);  // selectedSpeciality هو التخصص الذي تختاره
        startActivity(intent);

    }

}