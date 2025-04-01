package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class storeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);
        findViewById(R.id.button_back).setOnClickListener(v -> goToMain());

        Button btn =findViewById(R.id.button);
        Button btn2 =findViewById(R.id.button2);
        Button btn3 =findViewById(R.id.button3);
        Button btn4 =findViewById(R.id.button4);
        Button btn5 =findViewById(R.id.button5);
        Button btn6 =findViewById(R.id.button6);
        Button btn7 =findViewById(R.id.button7);
        Button btn8 =findViewById(R.id.button8);
        Button btn9 =findViewById(R.id.button9);
        Button btn10 =findViewById(R.id.button10);
        Button btn11 =findViewById(R.id.button11);
        Button btn12 =findViewById(R.id.button12);

        btn.setOnClickListener(view -> openProduitListActivity("PETCARE"));
        btn2.setOnClickListener(view -> openProduitListActivity("VITAMINS"));
        btn3.setOnClickListener(view -> openProduitListActivity("ORTHOPAEDICS"));
        btn4.setOnClickListener(view -> openProduitListActivity("Measurements"));
        btn5.setOnClickListener(view -> openProduitListActivity("DILUTIONS"));
        btn6.setOnClickListener(view -> openProduitListActivity("CHILDCARE"));
        btn7.setOnClickListener(view -> openProduitListActivity("FacewashCleansers"));
        btn8.setOnClickListener(view -> openProduitListActivity("DRESSING"));
        btn9.setOnClickListener(view -> openProduitListActivity("BLOODGLUCOSE"));
        btn10.setOnClickListener(view -> openProduitListActivity("AYURVEDIC"));
        btn11.setOnClickListener(view -> openProduitListActivity("PAINRELIEFAPPLICATION"));
        btn12.setOnClickListener(view -> openProduitListActivity("FEMININEHYGIENE"));
    }

    private void openProduitListActivity(String category) {
        Intent intent = new Intent(storeActivity.this,ProduitListActivity.class);
        intent.putExtra("Category",category);
        startActivity(intent);
    }

    private void goToMain() {
        Intent intent = new Intent(storeActivity.this, MainActivity.class);
        startActivity(intent);
    }
}