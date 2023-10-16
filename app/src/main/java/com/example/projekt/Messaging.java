package com.example.projekt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messaging extends AppCompatActivity {

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

    private ListView user_list;
    String conversation_id = new String();
    private List<String> user_uid = new ArrayList<>();

    List<String> list = new ArrayList<>();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        user_list = findViewById(R.id.messaging_list);
        EditText search_message = findViewById(R.id.search_message_text);
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
                        break;
                }
                return false;
            }
        });

        UserAdapter adapter = new UserAdapter(this, list);

        /*new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Messaging.this);

                LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                linearLayout.setPadding(80, 80, 80, 80);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                ListView users = new ListView(getApplicationContext());
                List<String> user_list = new ArrayList<>();
                List<String> user_id = new ArrayList<>();
                UserAdapter adapter1 = new UserAdapter(getApplicationContext(), user_list);
                readUsers(users, user_list, adapter1, user_id, "");

                linearLayout.addView(users);
                builder.setView(linearLayout);

                users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String receiver_id = user_id.get(i);

                        Log.d("Messages", "in users on click");
                        findConversationId(receiver_id);
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });*/

        Button search = findViewById(R.id.search_message);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readUsers(user_list, list, adapter, user_uid, search_message.getText().toString());
            }
        });

        readUsers(user_list, list, adapter, user_uid, "");

        user_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String receiver_id = user_uid.get(i);

                findConversationId(receiver_id);
            }
        });
    }

    private void findConversationId(String otherUserId){
        String uid = FirebaseAuth.getInstance().getUid();

        DatabaseReference usersConvo = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Conversations");

        usersConvo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                String id = new String();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getKey().equals(otherUserId)){
                        found = true;
                        id = dataSnapshot.getValue(String.class);
                        break;
                    }
                }

                if(found){
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("receiver_id", otherUserId);
                    intent.putExtra("conversation_id", id);
                    startActivity(intent);
                    finish();
                } else {
                    startNewConversation(otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Messages", "Error finding conversations " + error.getMessage());
            }
        });
    }

    /*public void findConversationId(String otherUserId, ConversationIdCallback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("Messages", "in find convo id");

        DatabaseReference convoRef = FirebaseDatabase.getInstance().getReference().child("Conversations");

        convoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final boolean[] found = {false};
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.getKey();

                    DatabaseReference participantsRef = dataSnapshot.child("Participants").getRef();

                    participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean first_participant = false;
                            boolean second_participant = false;
                            for(DataSnapshot participantSnapshot : snapshot.getChildren()){
                                if(participantSnapshot.getValue(String.class).equals(currentUserId)){
                                    first_participant = true;
                                } else if (participantSnapshot.getValue(String.class).equals(otherUserId)) {
                                    second_participant = true;
                                }
                            }

                            if(first_participant && second_participant){
                                conversation_id = id;
                                Log.d("Messaging", "inside find conv: " + conversation_id);
                                handleConversation(conversation_id, otherUserId);
                                found[0] = true;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("Messaging", error.getMessage());
                        }
                    });
                }
                if(!found[0]){
                    Log.d("Messages", "in not found");
                    handleConversation(conversation_id, otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Messaging", error.getMessage());
            }
        });
    }*/

    private void startNewConversation(String otherUserId) {
        Log.d("Messages", "In start conversation");
        DatabaseReference convoRef = FirebaseDatabase.getInstance().getReference().child("Conversations");
        DatabaseReference newConvoRef = convoRef.push();

        List<String> participants = new ArrayList<>();
        participants.add(FirebaseAuth.getInstance().getUid());
        participants.add(otherUserId);

        Map<String, Object> convoData = new HashMap<>();
        convoData.put("Participants", participants);
        convoData.put("Messages", "");

        newConvoRef.setValue(convoData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data successfully pushed to the database
                            addConversationToUsers(FirebaseAuth.getInstance().getUid(), newConvoRef.getKey(), otherUserId);
                            addConversationToUsers(otherUserId, newConvoRef.getKey(), FirebaseAuth.getInstance().getUid());
                            Toast.makeText(Messaging.this, "Data added stored on database", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra("conversation_id", newConvoRef.getKey());
                            intent.putExtra("receiver_id", otherUserId);
                            startActivity(intent);
                            finish();
                        } else {
                            // Failed to push data to the database
                            Toast.makeText(Messaging.this, "Error storing data to database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addConversationToUsers(String userId, String conversationId, String otherUserId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("Conversations");

        DatabaseReference newConvoRef = userRef.push();

        Map<String, String> user_convo = new HashMap<>();
        user_convo.put(otherUserId, conversationId);

        // Set otherUserId as the key and conversationId as the value
        userRef.child(otherUserId).setValue(conversationId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Messaging.this, "Conversation ID added to user", Toast.LENGTH_SHORT).show();
                } else {
                    // Failed to update data for the user
                    Log.d("Messages", "Error updating user with conversation ID: " + userId);
                    Toast.makeText(Messaging.this, "Error updating user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*private void addConversationToUsers(String userId, String conversationId, String otherUserId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("Conversations", );

        userRef.updateChildren(userUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Data successfully updated for the user

                            Toast.makeText(Messaging.this, "Conversation ID added to user", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to update data for the user
                            Log.d("Messages", "Error updating user with conversation ID: " + userId);
                            Toast.makeText(Messaging.this, "Error updating user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }*/

    private void getConversationIds(List<String> conversations_id, UserAdapter adapter){
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference convoRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Conversations");

        convoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversations_id.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    conversations_id.add(dataSnapshot.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Messages", error.getMessage());
            }
        });
    }

    private void loadConversations(ListView conversations, List<String> id_list, List<String> convo_list, UserAdapter adapter, String search){
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference convoOfUser = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Conversations");
        convoOfUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    id_list.add(dataSnapshot.getKey());
                }
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(id_list.contains(dataSnapshot.getKey())){
                                StringBuilder user = new StringBuilder();

                                user.append(dataSnapshot.getKey()).append(";");
                                user.append(dataSnapshot.child("Name").getValue(String.class)).append(";");
                                user.append(dataSnapshot.child("Bio").getValue(String.class)).append(";");
                                //user.append(dataSnapshot.getKey().toString());
                                convo_list.add(user.toString());
                            }
                        }

                        adapter.clear();
                        conversations.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Messages", "Error fetching user data " + error.getMessage());
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Messages", "error loading conversations " + error.getMessage());
            }
        });
    }

    private void readUsers(ListView users, List<String> users_list, UserAdapter arrayAdapter, List<String> user_uid, String search_name) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users_list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StringBuilder user = new StringBuilder();
                    if(search_name.equals("")) {
                        if (!dataSnapshot.child("Role").getValue(String.class).equals("Admin") && !dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                            user.append(dataSnapshot.getKey().toString()).append(";");
                            user.append(dataSnapshot.child("Name").getValue(String.class)).append(";");
                            user.append(dataSnapshot.child("Bio").getValue(String.class)).append(";");
                            //user.append(dataSnapshot.getKey().toString());
                            users_list.add(user.toString());

                            //save user uid in new list with the correlation of the position
                            user_uid.add(dataSnapshot.getKey().toString());
                            //Collections.reverse(user_uid);
                        }
                    } else {
                        if(!dataSnapshot.child("Role").getValue(String.class).equals("Admin") && !dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                            String username = dataSnapshot.child("Name").getValue(String.class).toLowerCase();

                            if(username.substring(0, search_name.length()).equals(search_name)){
                                users_list.add(dataSnapshot.child("Name").getValue(String.class));

                                //save user uid in new list with the correlation of the position
                                user_uid.add(dataSnapshot.getKey());
                                Collections.reverse(user_uid);
                            }
                        }
                    }
                }

                Collections.reverse(user_uid);
                Collections.reverse(users_list);
                users.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error retrieving use data: " + error.getMessage());
            }
        };

        userRef.addValueEventListener(userListener);
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