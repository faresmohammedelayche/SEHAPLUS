package com.example.sehaplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class nursingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nursing);
        findViewById(R.id.button_back).setOnClickListener(v -> goToMain());
    }

    private void goToMain() {
        Intent intent = new Intent(nursingActivity.this, MainActivity.class);
        startActivity(intent);
    }
}