package com.example.projekt;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrenghtAnalysis extends AppCompatActivity {

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

    TextView spinner_text, body_part, add_workout;
    ListView workouts;
    LineChart graph;
    Button graph_btn;
    EditText type_graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strenght_analysis);

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

        Spinner spinner = findViewById(R.id.spinner);
        spinner_text = findViewById(R.id.spinner_item);
        body_part = findViewById(R.id.body_part);
        workouts = findViewById(R.id.strenght_workouts);
        add_workout = findViewById(R.id.add_weight);
        graph = findViewById(R.id.strenght_graph);
        graph_btn = findViewById(R.id.graph_btn);
        type_graph = findViewById(R.id.type_graph);

        graph.setVisibility(View.GONE);

        String[] items = {"arms", "back", "core", "legs"};

        // Create an ArrayAdapter using the items array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        List<String> strenght_workouts = new ArrayList<>();

        StrenghtItemAdapter workout_adapter = new StrenghtItemAdapter(this, strenght_workouts);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinner_text.setText(items[i].toString());
                body_part.setText(items[i].toString().toUpperCase());
                graph.setVisibility(View.GONE);
                type_graph.setText("");

                loadWorkouts(items[i].toString(), workout_adapter, strenght_workouts);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        graph_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(graph.getVisibility() == View.GONE){
                    if(type_graph.getText().toString().isEmpty()){
                        Toast.makeText(StrenghtAnalysis.this, "Specify which workout you want to show", Toast.LENGTH_SHORT);
                    } else {
                        graph.setVisibility(View.VISIBLE);
                        loadGraph(graph, spinner.getSelectedItem().toString(), type_graph.getText().toString());
                    }
                } else if(graph.getVisibility() == View.VISIBLE){
                    graph.setVisibility(View.GONE);
                }
            }
        });

        add_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addWorkout(Arrays.asList(items));
            }
        });
    }

    private void loadGraph(LineChart graph, String body_part, String workout_type) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Workouts");
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.child("UserID").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        if(dataSnapshot.child("Group").exists()){
                            if(dataSnapshot.child("Group").getValue(String.class).equals("strenght")){
                                if(dataSnapshot.child("Type").getValue(String.class).equals(workout_type)) {
                                    xValues.add(xValues.size() + 1);
                                    yValues.add(Integer.parseInt(dataSnapshot.child("Weight").getValue(String.class)));
                                }
                            }
                        }
                    }
                }

                // Create an array to hold data points (entries)
                ArrayList<Entry> entries = new ArrayList<>();

                for(int i = 0; i < xValues.size(); i++){
                    entries.add(new Entry(xValues.get(i), yValues.get(i)));
                }

                // Create a LineDataSet from the entries
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setColor(Color.BLUE);
                dataSet.setCircleColor(Color.RED);
                dataSet.setCircleRadius(5f);

                // Create a LineData object and add the dataSet to it
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);
                LineData lineData = new LineData(dataSets);

                // Set up the X-axis (assuming x[] represents dates)
                XAxis xAxis = graph.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                // Set the data to the chart
                graph.setData(lineData);

                // Refresh the chart to display the data
                graph.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("GRAPH_load", error.getMessage());
            }
        };

        reference.addValueEventListener(listener);
    }

    private void loadWorkouts(String body_part, StrenghtItemAdapter adapter, List<String> workouts_list){
        List<String> list = new ArrayList<>();
        Log.d("Workouts", body_part);

        DatabaseReference workoutRef = FirebaseDatabase.getInstance().getReference().child("Workouts");
        workoutRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workouts_list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.child("UserID").getValue(String.class).equals(FirebaseAuth.getInstance().getUid())){
                        if(dataSnapshot.child("Group").exists()){
                            if(dataSnapshot.child("Bodypart").getValue(String.class).equals(body_part)){
                                StringBuilder workout = new StringBuilder();
                                workout.append(dataSnapshot.child("Type").getValue(String.class)).append("-");
                                workout.append(dataSnapshot.child("Weight").getValue(String.class)).append("-");
                                workout.append(dataSnapshot.child("Series").getValue(String.class)).append("-");
                                workout.append(dataSnapshot.child("Reps").getValue(String.class)).append(";");

                                workouts_list.add(workout.toString());
                            }
                        }
                    }
                }

                workouts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Workouts", error.getMessage());
            }
        });
    }

    private void addWorkout(List<String> body_parts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(80, 80, 80, 80);
        final EditText weight = new EditText(this);
        weight.setHint("Lifted weight inkg");
        weight.setWidth(2000);
        final EditText type = new EditText(this);
        type.setHint("Type of workout");
        type.setWidth(2000);
        final EditText series = new EditText(this);
        series.setHint("Number of series");
        series.setWidth(2000);
        final EditText reps = new EditText(this);
        reps.setHint("Number of reps per series");
        reps.setWidth(2000);
        String body_part = new String();
        final EditText add_body_part = new EditText(this);
        add_body_part.setHint("Bodypart");
        add_body_part.setWidth(2000);

        linearLayout.addView(type);
        linearLayout.addView(weight);
        linearLayout.addView(add_body_part);
        linearLayout.addView(series);
        linearLayout.addView(reps);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        builder.setView(linearLayout);

        builder.setTitle("Add a new cycling session")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button clicked
                    // Positive button clicked
                    Date today = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String workout_date = formatter.format(today);

                    DatabaseReference workoutRef = FirebaseDatabase.getInstance().getReference().child("Workouts");
                    DatabaseReference newWorkoutRef = workoutRef.push();

                    Map<String, Object> workoutData = new HashMap<>();
                    workoutData.put("Type", type.getText().toString());
                    workoutData.put("Weight", weight.getText().toString());
                    workoutData.put("Bodypart", add_body_part.getText().toString().toLowerCase());
                    workoutData.put("Series", series.getText().toString());
                    workoutData.put("Reps", reps.getText().toString());
                    workoutData.put("Date", workout_date);
                    workoutData.put("UserID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    workoutData.put("Group", "strenght");

                    newWorkoutRef.setValue(workoutData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Data successfully pushed to the database
                            } else {
                                // Failed to push data to the database
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}