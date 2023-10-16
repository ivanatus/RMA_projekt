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

public class CardioAnalysis extends AppCompatActivity {

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

    Button running_btn, walking_btn, cycling_btn, elliptical_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardio_analysis);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // on navigation bar click
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                        startActivity(homeIntent);
                        finish();
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

        running_btn = findViewById(R.id.running_btn);
        walking_btn = findViewById(R.id.walking_btn);
        cycling_btn = findViewById(R.id.cycling_btn);
        elliptical_btn = findViewById(R.id.elliptical_btn);

        // Attach the fragment to the container
        if (savedInstanceState == null) {
            RunningAnalysis fragment = new RunningAnalysis();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }

        running_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RunningAnalysis fragment = new RunningAnalysis();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
        });

        walking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WalkingAnalysis fragment = new WalkingAnalysis();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
        });

        cycling_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CyclingAnalysis fragment = new CyclingAnalysis();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
        });

        elliptical_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EllipticalAnalysis fragment = new EllipticalAnalysis();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }
        });
    }
}