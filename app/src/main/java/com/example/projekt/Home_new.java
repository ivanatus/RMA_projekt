package com.example.projekt;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home_new extends AppCompatActivity {

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

    ListView workouts;
    Button indoor_btn, outdoor_btn;
    TextView add_workout;
    boolean indoor, isAdmin;
    BottomNavigationView bottomNavigationView;

    public void scheduleNotification(Context context, long notificationTimeInMillis, String title, String content) {
        // Create an intent that will be triggered when the notification fires
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.example.projekt.NOTIFICATION_ACTION");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        // Schedule the notification at the specified time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTimeInMillis, pendingIntent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_new);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
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

        indoor = true;

        //  WORKOUTS PER ID
        /*  Date - 00/00/0000
            Time - 00:00
            Location - address
            Indoor/outdoor - true/false
            Type - workout name
            Trainer - coach name
            Spots - number of spots left in that workout
         */

        indoor_btn = findViewById(R.id.indoor);
        outdoor_btn = findViewById(R.id.outdoor);

        workouts = findViewById(R.id.workouts);

        List<String> list = new ArrayList<>();
        List<String> id_list = new ArrayList<>();
        List<String> location_list = new ArrayList<>();

        add_workout = findViewById(R.id.add_workout);
        isAdmin = checkIfUserIsAdmin(FirebaseAuth.getInstance().getUid());

        WorkoutAdapter adapter = new WorkoutAdapter(getApplicationContext(), list, id_list, isAdmin);

        loadPosts(indoor, list, adapter, id_list, location_list);

        workouts.setAdapter(adapter);

        add_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newWorkoutDetails();
            }
        });

        indoor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indoor = true;
                loadPosts(indoor, list, adapter, id_list, location_list);
            }
        });
        outdoor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indoor = false;
                loadPosts(indoor, list, adapter, id_list, location_list);
            }
        });

        workouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String target_location = location_list.get(i);
                Log.d("Maps log", "Long click: " + target_location);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("target_location", target_location);
                startActivity(intent);
                finish();
            }
        });
    }

    private void newWorkoutDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(80, 80, 80, 80);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontal_layout1 = new LinearLayout(this);
        horizontal_layout1.setOrientation(LinearLayout.HORIZONTAL);
        TextView type = new TextView(this);
        type.setText("Type: ");
        EditText type_edit = new EditText(this);
        horizontal_layout1.addView(type);
        horizontal_layout1.addView(type_edit);

        LinearLayout horizontal_layout2 = new LinearLayout(this);
        horizontal_layout2.setOrientation(LinearLayout.HORIZONTAL);
        TextView trainer = new TextView(this);
        trainer.setText("Trainer: ");
        EditText trainer_edit = new EditText(this);
        horizontal_layout2.addView(trainer);
        horizontal_layout2.addView(trainer_edit);

        LinearLayout horizontal_layout3 = new LinearLayout(this);
        horizontal_layout3.setOrientation(LinearLayout.HORIZONTAL);
        TextView location = new TextView(this);
        location.setText("Location: ");
        EditText location_edit = new EditText(this);
        horizontal_layout3.addView(location);
        horizontal_layout3.addView(location_edit);

        LinearLayout horizontal_layout4 = new LinearLayout(this);
        horizontal_layout4.setOrientation(LinearLayout.HORIZONTAL);
        TextView time = new TextView(this);
        time.setText("Time: ");
        EditText time_edit = new EditText(this);
        horizontal_layout4.addView(time);
        horizontal_layout4.addView(time_edit);

        LinearLayout horizontal_layout5 = new LinearLayout(this);
        horizontal_layout5.setOrientation(LinearLayout.HORIZONTAL);
        TextView date = new TextView(this);
        date.setText("Date: ");
        EditText date_edit = new EditText(this);
        horizontal_layout5.addView(date);
        horizontal_layout5.addView(date_edit);

        LinearLayout horizontal_layout6 = new LinearLayout(this);
        horizontal_layout6.setOrientation(LinearLayout.HORIZONTAL);
        TextView indoor = new TextView(this);
        indoor.setText("Indoor: ");
        CheckBox checkBox = new CheckBox(this);
        horizontal_layout6.addView(indoor);
        horizontal_layout6.addView(checkBox);

        LinearLayout horizontal_layout7 = new LinearLayout(this);
        horizontal_layout7.setOrientation(LinearLayout.HORIZONTAL);
        TextView spots = new TextView(this);
        spots.setText("Number of spots: ");
        EditText spots_edit = new EditText(this);
        horizontal_layout7.addView(spots);
        horizontal_layout7.addView(spots_edit);

        linearLayout.addView(horizontal_layout1);
        linearLayout.addView(horizontal_layout2);
        linearLayout.addView(horizontal_layout3);
        linearLayout.addView(horizontal_layout4);
        linearLayout.addView(horizontal_layout5);
        linearLayout.addView(horizontal_layout6);
        linearLayout.addView(horizontal_layout7);

        builder.setView(linearLayout);

        builder.setTitle("Add a new meal")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button clicked
                    addNewPost(date_edit.getText().toString(), checkBox.isChecked(), time_edit.getText().toString(), location_edit.getText().toString(), trainer_edit.getText().toString(), type_edit.getText().toString(), spots_edit.getText().toString());
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addNewPost(String date, boolean indoor, String time, String location, String trainer, String type, String spots){
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        DatabaseReference newPostRef = postsRef.push();

        Map<String, Object> postData = new HashMap<>();
        postData.put("Date", date);
        postData.put("Indoor", indoor);
        postData.put("Time", time);
        postData.put("Location", location);
        postData.put("Trainer", trainer);
        postData.put("Type", type);
        postData.put("Spots", spots);
        postData.put("Users", "");
        postData.put("Clicks", 0);

        newPostRef.setValue(postData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data successfully pushed to the database
                            Toast.makeText(Home_new.this, "Data added stored on database", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to push data to the database
                            Toast.makeText(Home_new.this, "Error storing data to database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadPosts(boolean indoor, List<String> post_list, WorkoutAdapter adapter, List<String> post_id, List<String> locations) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        ValueEventListener postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post_list.clear();
                post_id.clear();
                locations.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.child("Indoor").getValue(boolean.class) == indoor) {
                        StringBuilder post = new StringBuilder();

                        post.append(dataSnapshot.child("Type").getValue(String.class)).append("-");
                        post.append(dataSnapshot.child("Trainer").getValue(String.class)).append("-");
                        post.append(dataSnapshot.child("Location").getValue(String.class)).append("-");
                        post.append(dataSnapshot.child("Date").getValue(String.class)).append(", ").append(dataSnapshot.child("Time").getValue(String.class)).append("-");
                        post.append(dataSnapshot.child("Spots").getValue(String.class)).append("-").append("end");

                        post_list.add(post.toString());
                        post_id.add(dataSnapshot.getKey().toString());
                        locations.add(dataSnapshot.child("Location").getValue(String.class));
                    }
                }
                Collections.reverse(locations);
                Collections.reverse(post_id);
                Collections.reverse(post_list);
                workouts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error retrieving post data: " + error.getMessage());
            }
        };

        postsRef.addValueEventListener(postsListener);
    }

    private boolean checkIfUserIsAdmin(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("Role").getValue(String.class);

                    //only admin can publish new posts
                    Menu menu = bottomNavigationView.getMenu();
                    MenuItem profileItem = menu.findItem(R.id.profile);
                    MenuItem analysisItem = menu.findItem(R.id.analysis);
                    MenuItem messageItem = menu.findItem(R.id.messaging);

                    if (role == null) {
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else if (role.equals("Admin")) {
                        add_workout.setVisibility(View.VISIBLE);

                        profileItem.setVisible(false);
                        analysisItem.setVisible(false);
                        messageItem.setVisible(false);
                    } else {
                        add_workout.setVisibility(View.GONE);

                        if(role.equals("Regular")){
                            analysisItem.setVisible(false);
                            outdoor_btn.setVisibility(View.GONE);
                            indoor_btn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    System.out.println("Snapshot doesn't exist!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle error
                System.out.println("The read failed: " + error.getCode());
            }
        });

        boolean admin = false;

        if(add_workout.getVisibility() == View.VISIBLE){
            Log.d("Edit log", "zasto tu ude");
            return  true;
        }
        return false;
    }
}