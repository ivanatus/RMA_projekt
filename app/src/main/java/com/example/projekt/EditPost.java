package com.example.projekt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditPost extends AppCompatActivity {

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

    EditText edit_type, edit_trainer, edit_location, edit_time, edit_spots;
    Button save, delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        Intent intent = getIntent();

        String id = intent.getStringExtra("post id");
        String type = intent.getStringExtra("type");
        String trainer = intent.getStringExtra("trainer");
        String location = intent.getStringExtra("location");
        String date_and_time = intent.getStringExtra("date and time");
        String spots = intent.getStringExtra("spots");

        edit_type = findViewById(R.id.edit_type);
        edit_trainer = findViewById(R.id.edit_trainer);
        edit_location = findViewById(R.id.edit_location);
        edit_time = findViewById(R.id.edit_time);
        edit_spots = findViewById(R.id.edit_spots);
        save = findViewById(R.id.save_changes);
        delete = findViewById(R.id.delete_post);

        edit_type.setText(type);
        edit_trainer.setText(trainer);
        edit_location.setText(location);
        edit_time.setText(date_and_time);
        edit_spots.setText(spots);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePost(id, type, location, date_and_time);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(id, type, trainer, location, date_and_time);
            }
        });
    }

    private void save(String post_id, String type, String trainer, String location, String date_and_time) {
        String title = "Workout details changed";
        StringBuilder content_builder = new StringBuilder();
        content_builder.append("Workout details have been edited.\nWorkout: " +  "\n" +
                "Previously: " + type + ", " + date_and_time + ", " + location + ", lead by " + trainer + "\n");

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post_id);
        postRef.child("Type").setValue(edit_type.getText().toString());
        postRef.child("Trainer").setValue(edit_trainer.getText().toString());
        postRef.child("Location").setValue(edit_location.getText().toString());
        postRef.child("Spots").setValue(edit_spots.getText().toString());

        String[] time_parts = edit_time.getText().toString().split(", ");

        postRef.child("Date").setValue(time_parts[0]);
        postRef.child("Time").setValue(time_parts[1]);

        content_builder.append("Now: " + edit_type.getText().toString() + ", " + edit_time.getText().toString() + ", " +
                edit_location.getText().toString() + ", lead by " + edit_trainer.getText().toString());

        // Calculate the time 3 seconds from now in milliseconds
        long delayInMillis = 3000; // 3 seconds
        long notificationTimeInMillis = System.currentTimeMillis() + delayInMillis;

        scheduleNotification(this, notificationTimeInMillis, title, content_builder.toString());

        Intent intent = new Intent(this, Home_new.class);
        startActivity(intent);
        finish();
    }

    private void deletePost(String post_id, String type, String location, String date_and_time) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post_id);
        String title = "Workout canceled";
        String content = "Workout has been canceled.\nWorkout: " + type + "\nDate and time: " + date_and_time + "\nLocation: " + location;

        postRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@com.google.firebase.database.annotations.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Post deleted successfully
                    System.out.println("Post deleted successfully");
                } else {
                    // Error occurred while deleting the post
                    System.out.println("Failed to delete post: " + error.getMessage());
                }
            }
        });

        // Calculate the time 3 seconds from now in milliseconds
        long delayInMillis = 3000; // 3 seconds
        long notificationTimeInMillis = System.currentTimeMillis() + delayInMillis;

        scheduleNotification(this, notificationTimeInMillis, title, content);
    }

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
}