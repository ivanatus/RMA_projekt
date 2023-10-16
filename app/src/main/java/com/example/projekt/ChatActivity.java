package com.example.projekt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

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

    String receiver_id, conversation_id;
    ListView messages;
    EditText new_chat;
    ImageButton send;
    BottomNavigationView bottomNavigationView;
    List<String> list = new ArrayList<>();
    MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messages = findViewById(R.id.past_chats);
        new_chat = findViewById(R.id.new_chat);
        send = findViewById(R.id.send_chat);

        adapter = new MessagesAdapter(ChatActivity.this, list);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        regularVsPremium();
        // on navigation bar click
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent intentHome = new Intent(getApplicationContext(), Home_new.class);
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

        Intent intent = getIntent();

        Log.d("Messages", "Intent: " + intent.getExtras().toString());

        receiver_id = intent.getStringExtra("receiver_id");
        conversation_id = intent.getStringExtra("conversation_id");

        loadPreviousConversation(conversation_id, messages, adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!new_chat.getText().toString().equals("")){
                    sendMessage(FirebaseAuth.getInstance().getUid(), new_chat.getText().toString(), conversation_id);
                }
            }
        });
    }

    private void sendMessage(String uid, String message, String conversation_id) {
        DatabaseReference convoRef = FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversation_id).child("Messages");
        DatabaseReference newMessRef = convoRef.push();

        HashMap<String, String> new_message = new HashMap<>();
        new_message.put("Sender_id", uid);
        new_message.put("Text", message);

        newMessRef.setValue(new_message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Data successfully pushed to the database
                    //Toast.makeText(ChatActivity.this, "Messages sent", Toast.LENGTH_SHORT).show();
                } else {
                    // Failed to push data to the database
                    Toast.makeText(ChatActivity.this, "Error storing data to database", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new_chat.setText("");

        loadPreviousConversation(conversation_id, messages, adapter);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String title = snapshot.child("Name").getValue(String.class);
                String content = message;

                // Calculate the time 3 seconds from now in milliseconds
                long delayInMillis = 1000; // 3 seconds
                long notificationTimeInMillis = System.currentTimeMillis() + delayInMillis;

                scheduleNotification(getApplicationContext(), notificationTimeInMillis, title, content, receiver_id);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void scheduleNotification(Context context, long notificationTimeInMillis, String title, String content, String receiver_id) {
        // Create an intent that will be triggered when the notification fires
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.example.projekt.NOTIFICATION_ACTION");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("receiver_id", receiver_id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        // Schedule the notification at the specified time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTimeInMillis, pendingIntent);

    }

    private void loadPreviousConversation(String conversation_id, ListView messages, MessagesAdapter adapter) {
        List<String> message_list = new ArrayList<>();

        Log.d("Messaging", "ude u load conversation");

        DatabaseReference convoRef = FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversation_id).child("Messages");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StringBuilder message = new StringBuilder();

                    Log.d("Messaging", "ude u citanje");

                    message.append(dataSnapshot.child("Sender_id").getValue(String.class)).append(";");
                    message.append(dataSnapshot.child("Text").getValue(String.class));

                    message_list.add(message.toString());
                }
                // Set the adapter after loading the data
                adapter.clear(); // Clear previous data
                adapter.addAll(message_list); // Add new data
                adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed

                messages.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Messages", "Error reading past messages: " + error.getMessage());
            }
        };
        convoRef.addValueEventListener(listener);
    }

    private void regularVsPremium() {
        String uid = FirebaseAuth.getInstance().getUid();
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
                    } else if (role.equals("Regular")) {
                        analysisItem.setVisible(false);
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
    }
}