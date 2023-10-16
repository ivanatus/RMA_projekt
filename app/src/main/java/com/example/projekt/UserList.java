package com.example.projekt;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserList extends AppCompatActivity {

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
    private String search_name = "";
    private List<String> user_uid = new ArrayList<>();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        user_list = findViewById(R.id.user_list);
        Button search_btn = findViewById(R.id.search_btn);
        EditText search_username = findViewById(R.id.search_username);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        regularVsPremium();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(String.valueOf(FirebaseAuth.getInstance().getUid()));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("Role").getValue(String.class);

                    if (role != null && role.equals("Admin")) {
                        setProfileVisibility(true);
                    } else {
                        setProfileVisibility(false);
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

        List<String> users = new ArrayList<>();
        UserAdapter adapter = new UserAdapter(this, users);

        readUsers(user_list, users, adapter, user_uid, search_name);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_name = String.valueOf(search_username.getText()).toLowerCase();
                readUsers(user_list, users, adapter, user_uid, search_name);
            }
        });

        user_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object user = user_list.getItemAtPosition(i);
                //get users uid based on the position
                String uid = user_uid.get(i);

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(String.valueOf(FirebaseAuth.getInstance().getUid()));
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.child("Role").getValue(String.class);

                            if (role != null && role.equals("Admin")) {
                                removeUser(uid);
                            } else {
                                showUserProfile(uid);
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
        });

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
                            user.append(" ").append(";");
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

    private void showUserProfile(String uid) {
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        //set layout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //views to set in dialog
        final TextView users_name = new TextView(this);
        final TextView users_bio = new TextView(this);
        final TextView users_birthday = new TextView(this);
        final TextView users_membership_date = new TextView(this);
        StringBuilder birthdate = new StringBuilder();
        StringBuilder membership = new StringBuilder();
        final ListView users_signed_classes = new ListView(this);
        final CircleImageView users_profile_picture = new CircleImageView(this);

        users_name.setPadding(10, 20, 10, 75);
        users_name.setTextSize(25);
        users_bio.setPadding(10, 30, 10, 10);
        users_bio.setTextSize(15);
        users_birthday.setPadding(10, 30, 10, 10);
        users_birthday.setTextSize(15);
        users_membership_date.setPadding(10, 30, 10, 10);
        users_membership_date.setTextSize(15);
        users_profile_picture.setMinimumHeight(350);
        users_profile_picture.setMinimumWidth(350);

        birthdate.append("Date of birth: ");
        membership.append("Date of membership: ");

        //read user data
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                birthdate.append(snapshot.child("Date of birth").getValue(String.class));
                membership.append(snapshot.child("Date of membership").getValue(String.class));

                users_name.setText(snapshot.child("Name").getValue(String.class));
                users_bio.setText(snapshot.child("Bio").getValue(String.class));
                users_birthday.setText(birthdate);
                users_membership_date.setText(membership);
                loadProfilePicture(uid, users_profile_picture);
                // Retrieve the list of classes the user is signed up to
                List<String> classesList = new ArrayList<>();
                for (DataSnapshot classSnapshot : snapshot.child("Classes").getChildren()) {
                    String classId = classSnapshot.getKey();
                    classesList.add(classId);
                }

                // Display the list of classes in the ListView
                List<String> list_of_signedup_classes = new ArrayList<>();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UserList.this, android.R.layout.simple_list_item_1, list_of_signedup_classes);
                loadPosts(list_of_signedup_classes, adapter, users_signed_classes, classesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserList.this, "Error retrieving data " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        userRef.addValueEventListener(userListener);
        linearLayout.addView(users_profile_picture);
        linearLayout.addView(users_name);
        linearLayout.addView(users_bio);
        linearLayout.addView(users_birthday);
        linearLayout.addView(users_membership_date);
        linearLayout.addView(users_signed_classes);
        linearLayout.setPadding(100, 100, 100, 100);


        builder.setView(linearLayout);

        //buttons cancel
        builder.setNegativeButton("Zatvori", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog
                dialogInterface.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private ArrayList<String> classesList;

    private void removeUser(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        DatabaseReference userClassesRef = userRef.child("Classes");

        AlertDialog.Builder builder = new AlertDialog.Builder(UserList.this);
        builder.setTitle("");

        LinearLayout linearLayout = new LinearLayout(UserList.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nameText = new TextView(UserList.this);
        TextView bioText = new TextView(UserList.this);
        TextView birthDateText = new TextView(UserList.this);
        TextView membershipDateText = new TextView(UserList.this);
        ListView classesListView = new ListView(UserList.this);
        CircleImageView users_profile_picture = new CircleImageView(this);
        Button btn_premium = new Button(this);

        nameText.setPadding(10, 20, 10, 75);
        nameText.setTextSize(25);
        bioText.setPadding(10, 30, 10, 10);
        bioText.setTextSize(15);
        birthDateText.setPadding(10, 30, 10, 10);
        birthDateText.setTextSize(15);
        membershipDateText.setPadding(10, 30, 10, 10);
        membershipDateText.setTextSize(15);
        users_profile_picture.setMinimumHeight(350);
        users_profile_picture.setMinimumWidth(350);

        linearLayout.addView(users_profile_picture);
        linearLayout.addView(nameText);
        linearLayout.addView(bioText);
        linearLayout.addView(birthDateText);
        linearLayout.addView(membershipDateText);
        linearLayout.addView(classesListView);
        linearLayout.addView(btn_premium);

        linearLayout.setPadding(100, 100, 100, 100);

        builder.setView(linearLayout);

        // Retrieve user details and classes
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("Name").getValue(String.class);
                    String bio = snapshot.child("Bio").getValue(String.class);
                    String birthDate = snapshot.child("Date of birth").getValue(String.class);
                    String membershipDate = snapshot.child("Date of membership").getValue(String.class);
                    String role = snapshot.child("Role").getValue(String.class);

                    nameText.setText(name);
                    bioText.setText(bio);
                    birthDateText.setText("Date of birth: " + birthDate);
                    membershipDateText.setText("Date of membership: " + membershipDate);
                    loadProfilePicture(uid, users_profile_picture);
                    if(role.equals("Regular")) {
                        btn_premium.setText("Update to premium");
                    } else {
                        btn_premium.setVisibility(View.GONE);
                    }

                    // Retrieve the list of classes the user is signed up to
                    List<String> classesList = new ArrayList<>();
                    for (DataSnapshot classSnapshot : snapshot.child("Classes").getChildren()) {
                        String classId = classSnapshot.getKey();
                        classesList.add(classId);
                    }

                    // Display the list of classes in the ListView
                    List<String> list_of_signedup_classes = new ArrayList<>();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(UserList.this, android.R.layout.simple_list_item_1, list_of_signedup_classes);
                    loadPosts(list_of_signedup_classes, adapter, classesListView, classesList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(UserList.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btn_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference premiumUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                premiumUserRef.child("Role").setValue("Premium");
            }
        });

        builder.setPositiveButton("Delete user", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Remove the user from the database
                userRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Decrease the Clicks counters for all classes the user was signed up to
                                for (String classId : classesList) {
                                    DatabaseReference classRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(classId);
                                    classRef.child("Clicks").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                int clicks = snapshot.getValue(Integer.class);
                                                classRef.child("Clicks").setValue(clicks - 1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle the error
                                            Toast.makeText(UserList.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                Toast.makeText(UserList.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), UserList.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserList.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
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

    //load image from storage
    private void loadProfilePicture(String uid, CircleImageView profile_picture) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(uid);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(UserList.this).load(uri).into(profile_picture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOAD PIC", e.getMessage());
            }
        });
    }

    private void loadPosts(List<String> post_list, ArrayAdapter<String> adapter, ListView posts, List<String> post_id) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        ValueEventListener postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post_list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (post_id.contains(dataSnapshot.getKey())) {
                        String description = dataSnapshot.child("Description").getValue(String.class);
                        String date = dataSnapshot.child("Date").getValue(String.class);
                        String time = dataSnapshot.child("Time").getValue(String.class);
                        int clicks = dataSnapshot.child("Clicks").getValue(Integer.class);

                        StringBuilder post = new StringBuilder();
                        post.append(description).append("\n");
                        post.append("Date: ").append(date).append("\n");
                        post.append("Time: ").append(time).append("\n");
                        post.append("Prijavljenih: ").append(clicks);

                        post_list.add(post.toString());
                    }
                }

                Collections.reverse(post_list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error retrieving post data: " + error.getMessage());
            }
        };

        postsRef.addValueEventListener(postsListener);
        posts.setAdapter(adapter);
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
