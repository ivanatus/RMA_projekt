package com.example.projekt;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class TrainingAnalysis extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Button cardio_btn, strenght_btn, food_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_analysis);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // on navigation bar click
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                switch (item.getItemId()) {
                    case R.id.home:
                        break;
                    case R.id.profile:
                        // Handle Dashboard item click
                        Intent intentProfile = new Intent(getApplicationContext(), UserProfile.class);
                        startActivity(intentProfile);
                        finish();
                        break;
                    case R.id.search:
                        // Handle Search item click
                        Intent intentUsers = new Intent(getApplicationContext(), UserList.class);
                        startActivity(intentUsers);
                        finish();
                        break;
                    case R.id.analysis:
                        // Handle Search item click
                        Intent intentAnalysis = new Intent(getApplicationContext(), TrainingAnalysis.class);
                        startActivity(intentAnalysis);
                        finish();
                        break;
                    case R.id.messaging:
                        Intent intent = new Intent(getApplicationContext(), Messaging.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return false;
            }
        });

        cardio_btn = findViewById(R.id.cardio_btn);
        strenght_btn = findViewById(R.id.strenght_btn);
        food_btn = findViewById(R.id.food_btn);

        cardio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardio_intent = new Intent(getApplicationContext(), CardioAnalysis.class);
                startActivity(cardio_intent);
                finish();
            }
        });

        strenght_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent strenght_intent = new Intent(getApplicationContext(), StrenghtAnalysis.class);
                startActivity(strenght_intent);
                finish();
            }
        });

        food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent food_intent = new Intent(getApplicationContext(), NutritionAnalysis.class);
                startActivity(food_intent);
                finish();
            }
        });
    }
}