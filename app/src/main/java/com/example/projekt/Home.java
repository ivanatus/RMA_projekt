package com.example.projekt;

import static android.text.InputType.TYPE_DATETIME_VARIATION_DATE;
import static android.text.InputType.TYPE_DATETIME_VARIATION_TIME;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Home extends AppCompatActivity {

    ListView posts;
    EditText new_post, new_date, new_time;
    Button publish_post, publish_date, publish_time;

    //Firebase Database variables
    private FirebaseDatabase myFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    BottomNavigationView bottomNavigationView;
    private LinearLayout fragmentContainer;
    private List<String> post_id = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = new Intent(getApplicationContext(), Home_new.class);
        startActivity(intent);
        finish();

        posts = findViewById(R.id.posts);
        new_post = findViewById(R.id.new_post);
        new_date = findViewById(R.id.new_date);
        new_time = findViewById(R.id.new_time);
        publish_post = findViewById(R.id.btn_new_post);
        publish_date = findViewById(R.id.btn_new_date);
        publish_time = findViewById(R.id.btn_new_time);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.home_layout);

        //set input types for posts
        new_post.setInputType(TYPE_DATETIME_VARIATION_DATE);
        new_time.setInputType(TYPE_DATETIME_VARIATION_TIME);

        //declare database variables
        mAuth = FirebaseAuth.getInstance();
        myFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = myFirebaseDatabase.getReference();

        //check if user is admin to see if they can publish new posts
        String uid = mAuth.getUid();
        boolean admin = checkIfUserIsAdmin(uid);

        //create interactive list for posts and load them from database
        List<String> post_list = new ArrayList<>();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, post_list);
//        loadPosts(post_list, arrayAdapter, post_id);

        //publish new post - only admin
        publish_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new_post.getText().length() == 0 || new_date.getText().length() == 0 || new_time.getText().length() == 0) {
                    Toast.makeText(Home.this, "Dodaj trening, datum i vrijeme!", Toast.LENGTH_SHORT).show();
                } else{
                    addPost(uid, new_post, new_date, new_time, post_list, posts, arrayAdapter);
                    //sendNewPostNotification();
                }
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("Posts")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        System.out.println(msg);
                    }
                });



        //ADMIN - can edit posts, USER - can sign up for training
        posts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = posts.getItemAtPosition(i);
                // Assuming the item is of type String
                String value = (String) item;
                //if regular user, not admin
                if(new_date.getVisibility() == View.VISIBLE){
                    //TO DO - add editing of the posts
                    editPosts(post_id.get(i));
                    Log.d("VISIBILITY view", String.valueOf(View.INVISIBLE));
                } else {
                    //users signing up for the class
                    signUpUser(uid, post_id.get(i));
                    //loadPosts(post_list, arrayAdapter, post_id);
                }
            }
        });

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
                    /*case R.id.logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                        break;*/
                }
                return false;
            }
        });

    }

    private void deletePost(String post_id) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post_id);

        postRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Post deleted successfully
                    System.out.println("Post deleted successfully");
                } else {
                    // Error occurred while deleting the post
                    System.out.println("Failed to delete post: " + error.getMessage());
                }
            }
        });
    }

    private void editPosts(String post_id){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post_id);

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        //set layout
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText desc = new EditText(this);
        final EditText date = new EditText(this);
        final EditText time = new EditText(this);

        date.setInputType(TYPE_DATETIME_VARIATION_DATE);
        time.setInputType(TYPE_DATETIME_VARIATION_TIME);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    desc.setText(snapshot.child("Description").getValue(String.class));
                    date.setText(snapshot.child("Date").getValue(String.class));
                    time.setText(snapshot.child("Time").getValue(String.class));
                } else{
                    System.out.println("Snapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Error retrieving data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        linearLayout.addView(desc);
        linearLayout.addView(date);
        linearLayout.addView(time);
        linearLayout.setPadding(50, 50, 50, 50);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        builder.setView(linearLayout);

        builder.setPositiveButton("Spremi promjene", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(isValidDateFormat(date.getText().toString()) /*isValidTimeFormat(time.getText().toString())*/) {
                    //save changes
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    postRef.child("Description").setValue(desc.getText().toString());
                    postRef.child("Date").setValue(date.getText().toString());
                    postRef.child("Time").setValue(time.getText().toString());
                } else {
                    Toast.makeText(Home.this, "Upišite valjani datum i vrijeme!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Obriši trening", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePost(post_id);
                dialogInterface.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void signUpUser(String uid, String post_id) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id);
        DatabaseReference userClassesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Classes");

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String description = snapshot.child("Description").getValue(String.class);
                    String date = snapshot.child("Date").getValue(String.class);
                    String time = snapshot.child("Time").getValue(String.class);
                    int clicks = snapshot.child("Clicks").getValue(Integer.class);

                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setTitle("");

                    LinearLayout linearLayout = new LinearLayout(Home.this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView descriptionText = new TextView(Home.this);
                    descriptionText.setText("Trening: " + description);
                    TextView dateText = new TextView(Home.this);
                    dateText.setText("Datum: " + date);
                    TextView timeText = new TextView(Home.this);
                    timeText.setText("Vrijeme: " + time);

                    descriptionText.setTextSize(17);
                    dateText.setTextSize(17);
                    timeText.setTextSize(17);

                    linearLayout.addView(descriptionText);
                    linearLayout.addView(dateText);
                    linearLayout.addView(timeText);

                    linearLayout.setPadding(75, 75, 75, 75);

                    builder.setView(linearLayout);

                    builder.setPositiveButton("Prijavi se/Odjavi se", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userClassesRef.child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // User is already signed up, so remove the class
                                        userClassesRef.child(post_id).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Decrease the Clicks counter by 1
                                                        postRef.child("Clicks").setValue(clicks - 1);
                                                        Toast.makeText(Home.this, "Odjavljeni ste s treninga", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Home.this, "Failed to sign out of the class", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // User is not signed up, so add the class
                                        userClassesRef.child(post_id).setValue(true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Increase the Clicks counter by 1
                                                        postRef.child("Clicks").setValue(clicks + 1);
                                                        Toast.makeText(Home.this, "Prijavljeni ste na trening", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Home.this, "Failed to sign up for the class", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle the error
                                    Toast.makeText(Home.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(Home.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    /*private void loadPosts(List<String> post_list, ArrayAdapter arrayAdapter, List<String> post_id) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        ValueEventListener postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post_list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StringBuilder post = new StringBuilder();
                    post.append(dataSnapshot.child("Description").getValue(String.class)).append("\n");
                    post.append(dataSnapshot.child("Date").getValue(String.class)).append("\n");
                    post.append(dataSnapshot.child("Time").getValue(String.class)).append("\n");
                    post.append("Prijavljenih: ").append(dataSnapshot.child("Clicks").getValue(Long.class));

                    post_list.add(post.toString());
                    post_id.add(dataSnapshot.getKey().toString());
                    //System.out.println(post);
                }

                Collections.reverse(post_id);
                Collections.reverse(post_list);
                posts.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error retrieving post data: " + error.getMessage());
            }
        };

        postsRef.addValueEventListener(postsListener);
    }

    public static boolean isValidTimeFormat(String time) {
        // Define the regular expression pattern for "hh:mm"
        String regexPattern = "^\\d{2}:\\d{2}$";

        // Create a Pattern object from the regex pattern
        Pattern pattern = Pattern.compile(regexPattern);

        // Create a Matcher object to match the input time against the pattern
        Matcher matcher = pattern.matcher(time);

        if(Integer.parseInt(time.substring(0, 1)) > 23 || Integer.parseInt(time.substring(3, 4)) > 59){
            return false;
        }

        // Check if the input time matches the pattern
        return matcher.matches();
    }*/


    public static boolean isValidDateFormat(String date) {
        // Define the regular expression pattern for "dd/mm/yyyy"
        String regexPattern = "^\\d{2}/\\d{2}/\\d{4}$";

        // Create a Pattern object from the regex pattern
        Pattern pattern = Pattern.compile(regexPattern);

        // Create a Matcher object to match the input date against the pattern
        Matcher matcher = pattern.matcher(date);

        // Check if the input date matches the pattern
        return matcher.matches();
    }
    private void addPost(String uid, EditText post, EditText date, EditText time, List<String> post_list, ListView posts, ArrayAdapter arrayAdapter){
        StringBuilder new_post = new StringBuilder();
        boolean correctly_written = true;

        if(!isValidDateFormat(String.valueOf(date.getText())) /*|| !isValidTimeFormat(String.valueOf(time.getText()))*/){
            correctly_written = false;
            Toast.makeText(this, "Napiši valjani datum i vrijeme!", Toast.LENGTH_SHORT).show();
        }

        if(correctly_written) {
            new_post.append(post.getText() + "\n" + time.getText() + "\n" + date.getText());

            //add to realtime database
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            DatabaseReference newPostRef = postsRef.push();

            Map<String, Object> postData = new HashMap<>();
            postData.put("Clicks", 0);
            postData.put("Date", date.getText().toString());
            postData.put("Time", time.getText().toString());
            postData.put("Description", post.getText().toString());

            newPostRef.setValue(postData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Data successfully pushed to the database
                                Toast.makeText(Home.this, "Data added stored on database", Toast.LENGTH_SHORT).show();
                            } else {
                                // Failed to push data to the database
                                Toast.makeText(Home.this, "Error storing data to database", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            //add new post to the top of the list
            Collections.reverse(post_list);
            post_list.add(new_post.toString());
            Collections.reverse(post_list);
            posts.setAdapter(arrayAdapter);
        }
    }

    private boolean checkIfUserIsAdmin(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("Role").getValue(String.class);

                    //only admin can publish new posts

                    if (role == null) {
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else if (role != null && !role.equals("Admin")) {
                        new_post.setVisibility(View.GONE);
                        new_date.setVisibility(View.GONE);
                        new_time.setVisibility(View.GONE);
                        publish_date.setVisibility(View.GONE);
                        publish_post.setVisibility(View.GONE);
                        publish_time.setVisibility(View.GONE);
                        setProfileVisibility(false);
                    } else if (role.equals("Admin")) {
                        new_post.setVisibility(View.VISIBLE);
                        new_date.setVisibility(View.VISIBLE);
                        new_time.setVisibility(View.VISIBLE);
                        publish_date.setVisibility(View.INVISIBLE);
                        publish_post.setVisibility(View.INVISIBLE);
                        publish_time.setVisibility(View.VISIBLE);
                        setProfileVisibility(true);
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

        if(new_post.getVisibility() == View.VISIBLE){
            return  true;
        }
        return false;
    }

    private void setProfileVisibility(boolean isAdmin) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem profileItem = menu.findItem(R.id.profile);

        if (isAdmin) {
            profileItem.setVisible(false);
        } else {
            profileItem.setVisible(true);
        }
    }
}