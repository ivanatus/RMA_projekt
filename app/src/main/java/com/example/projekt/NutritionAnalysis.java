package com.example.projekt;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionAnalysis extends AppCompatActivity {

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

    ListView meals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_analysis);

        meals = findViewById(R.id.meals);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // on navigation bar click
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent intentHome = new Intent(getApplicationContext(), Home.class);
                        startActivity(intentHome);
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

        TextView new_meal = findViewById(R.id.add_meal);

        new_meal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addNewMeal();
            }
        });

        List<String> list = new ArrayList<>();
        List<String> id_list = new ArrayList<>();
        MealAdapter adapter = new MealAdapter(getApplicationContext(), list);

        loadMeals(list, adapter, id_list);
    }

    private void addNewMeal(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(80, 80, 80, 80);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontal_layout1 = new LinearLayout(this);
        horizontal_layout1.setOrientation(LinearLayout.HORIZONTAL);

        TextView food = new TextView(this);
        food.setText("Food: ");
        EditText food_name = new EditText(this);
        food_name.setWidth(200);
        TextView amount = new TextView(this);
        amount.setText("Amount: ");
        EditText weight = new EditText(this);
        weight.setWidth(200);
        TextView grams = new TextView(this);
        grams.setText("grams");

        horizontal_layout1.addView(food);
        horizontal_layout1.addView(food_name);
        horizontal_layout1.addView(amount);
        horizontal_layout1.addView(weight);
        horizontal_layout1.addView(grams);

        LinearLayout horizontal_layout2 = new LinearLayout(this);
        horizontal_layout2.setOrientation(LinearLayout.HORIZONTAL);

        TextView food2 = new TextView(this);
        food2.setText("Food: ");
        EditText food_name2 = new EditText(this);
        food_name2.setWidth(200);
        TextView amount2 = new TextView(this);
        amount2.setText("Amount: ");
        EditText weight2 = new EditText(this);
        weight2.setWidth(200);
        TextView grams2 = new TextView(this);
        grams2.setText("grams");

        horizontal_layout2.addView(food2);
        horizontal_layout2.addView(food_name2);
        horizontal_layout2.addView(amount2);
        horizontal_layout2.addView(weight2);
        horizontal_layout2.addView(grams2);

        LinearLayout horizontal_layout3 = new LinearLayout(this);
        horizontal_layout3.setOrientation(LinearLayout.HORIZONTAL);

        TextView food3 = new TextView(this);
        food3.setText("Food: ");
        EditText food_name3 = new EditText(this);
        food_name3.setWidth(200);
        TextView amount3 = new TextView(this);
        amount3.setText("Amount: ");
        EditText weight3 = new EditText(this);
        weight3.setWidth(200);
        TextView grams3 = new TextView(this);
        grams3.setText("grams");

        horizontal_layout3.addView(food3);
        horizontal_layout3.addView(food_name3);
        horizontal_layout3.addView(amount3);
        horizontal_layout3.addView(weight3);
        horizontal_layout3.addView(grams3);

        LinearLayout horizontal_layout4 = new LinearLayout(this);
        horizontal_layout4.setOrientation(LinearLayout.HORIZONTAL);

        TextView food4 = new TextView(this);
        food4.setText("Food: ");
        EditText food_name4 = new EditText(this);
        food_name4.setWidth(200);
        TextView amount4 = new TextView(this);
        amount4.setText("Amount: ");
        EditText weight4 = new EditText(this);
        weight4.setWidth(200);
        TextView grams4 = new TextView(this);
        grams4.setText("grams");

        horizontal_layout4.addView(food4);
        horizontal_layout4.addView(food_name4);
        horizontal_layout4.addView(amount4);
        horizontal_layout4.addView(weight4);
        horizontal_layout4.addView(grams4);

        LinearLayout horizontal_layout5 = new LinearLayout(this);
        horizontal_layout5.setOrientation(LinearLayout.HORIZONTAL);

        TextView food5 = new TextView(this);
        food5.setText("Food: ");
        EditText food_name5 = new EditText(this);
        food_name5.setWidth(200);
        TextView amount5 = new TextView(this);
        amount5.setText("Amount: ");
        EditText weight5 = new EditText(this);
        weight5.setWidth(200);
        TextView grams5 = new TextView(this);
        grams5.setText("grams");

        horizontal_layout5.addView(food5);
        horizontal_layout5.addView(food_name5);
        horizontal_layout5.addView(amount5);
        horizontal_layout5.addView(weight5);
        horizontal_layout5.addView(grams5);


        linearLayout.addView(horizontal_layout1);
        linearLayout.addView(horizontal_layout2);
        linearLayout.addView(horizontal_layout3);
        linearLayout.addView(horizontal_layout4);
        linearLayout.addView(horizontal_layout5);
        builder.setView(linearLayout);

        builder.setTitle("Add a new meal")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button clicked
                    List<String> foods = new ArrayList<>();
                    List<String> weights = new ArrayList<>();

                    if(!food_name.getText().toString().isEmpty()){
                        foods.add(food_name.getText().toString());
                    }
                    if(!food_name2.getText().toString().isEmpty()){
                        foods.add(food_name2.getText().toString());
                    }
                    if(!food_name3.getText().toString().isEmpty()){
                        foods.add(food_name3.getText().toString());
                    }
                    if(!food_name4.getText().toString().isEmpty()){
                        foods.add(food_name4.getText().toString());
                    }
                    if(!food_name5.getText().toString().isEmpty()){
                        foods.add(food_name5.getText().toString());
                    }

                    if(!weight.getText().toString().isEmpty()){
                        weights.add(weight.getText().toString());
                    }
                    if(!weight2.getText().toString().isEmpty()){
                        weights.add(weight.getText().toString());
                    }
                    if(!weight3.getText().toString().isEmpty()){
                        weights.add(weight3.getText().toString());
                    }
                    if(!weight4.getText().toString().isEmpty()){
                        weights.add(weight4.getText().toString());
                    }
                    if(!weight5.getText().toString().isEmpty()){
                        weights.add(weight5.getText().toString());
                    }

                    addMealToDB(foods, weights);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addMealToDB(List<String> foods, List<String> weights){
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Meals");
        DatabaseReference newMealRef = mealRef.push();

        Map<String, String> mealData = new HashMap<>();
        for(int i = 0; i < foods.size(); i++) {
            mealData.put(foods.get(i), weights.get(i));
        }

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        // Create a SimpleDateFormat to format the date as needed
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // Change the format as needed
        // Format the current date as a string
        String currentDate = dateFormat.format(calendar.getTime());
        mealData.put("Date", currentDate.toString());

        // Get the current time
        LocalTime currentTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentTime = LocalTime.now();
        }
        // Create a DateTimeFormatter to format the time as needed
        DateTimeFormatter timeFormatter = null; // Change the format as needed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }
        // Format the current time as a string
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String formattedTime = currentTime.format(timeFormatter);
            mealData.put("Time", formattedTime);
        }

        newMealRef.setValue(mealData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Data successfully pushed to the database
                    Toast.makeText(NutritionAnalysis.this, "Data added stored on database", Toast.LENGTH_SHORT).show();
                } else {
                    // Failed to push data to the database
                    Toast.makeText(NutritionAnalysis.this, "Error storing data to database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMeals(List<String> meal_list, MealAdapter adapter, List<String> meal_id){
            String uid = FirebaseAuth.getInstance().getUid();
            DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Meals");
            mealRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    meal_list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        StringBuilder meal = new StringBuilder();
                        for(DataSnapshot meal_details : dataSnapshot.getChildren()) {
                            if(meal_details.getKey().equals("Date") && meal.toString().isEmpty()){
                                meal.append(meal_details.getValue(String.class)).append(", ");
                            } else if(meal_details.getKey().equals("Time")){
                                meal.append(meal_details.getValue(String.class)).append("-");
                            } else {
                                meal.append(meal_details.getKey()).append("-");
                                meal.append(meal_details.getValue(String.class)).append("-");
                            }
                        }
                        meal.append(";");
                        Log.d("Meals", "Read from db: " + "\n" + meal.toString());
                        meal_list.add(meal.toString());
                        meal_id.add(dataSnapshot.getKey());
                    }
                    Collections.reverse(meal_list);
                    Collections.reverse(meal_id);

                    meals.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Meals", error.getMessage());
                }
            });
        }
}