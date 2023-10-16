package com.example.projekt;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.projekt.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserProfile extends AppCompatActivity {

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

    public String userProfileUsername;
    TextView username, user_bio, birth_date, membership_date, gender_info;
    de.hdodenhof.circleimageview.CircleImageView profile_picture;
    ListView posts;
    Button edit_username, edit_bio, edit_picture, btn_logout;
    BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private final int REQUEST_CODE_GALLERY = 1001;

    ActivityUserProfileBinding binding;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_profile);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);

        username = findViewById(R.id.username);
        user_bio = findViewById(R.id.user_bio);
        profile_picture = findViewById(R.id.profile_picture);
        edit_username = findViewById(R.id.edit_username);
        edit_bio = findViewById(R.id.edit_bio);
        edit_picture = findViewById(R.id.edit_image);
        //btn_logout = findViewById(R.id.btn_logout);
        birth_date = findViewById(R.id.birth_date);
        membership_date = findViewById(R.id.membership_date);
        posts = findViewById(R.id.posts);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        regularVsPremium();

        //create interactive list for posts and load them from database
        List<String> post_text = new ArrayList<>();
        List<String> post_id_list = new ArrayList<>();
        WorkoutAdapter adapter =  new WorkoutAdapter(getApplicationContext(), post_text, post_id_list, false);

        // Retrieve user details from database
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Use the retrieved value here
                    username.setText(dataSnapshot.child("Name").getValue(String.class));
                    user_bio.setText(dataSnapshot.child("Bio").getValue(String.class));
                    birth_date.setText(dataSnapshot.child("Date of birth").getValue(String.class));
                    membership_date.setText(dataSnapshot.child("Date of membership").getValue(String.class));

                    userProfileUsername = String.valueOf(username.getText());

                    // load posts
                    if(dataSnapshot.child("Classes").exists()) {
                        for (DataSnapshot post : dataSnapshot.child("Classes").getChildren()) {
                            post_id_list.add(post.toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors here
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        loadProfilePicture(uid);
        loadPosts(post_text, adapter, post_id_list);

        edit_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUsername(username);
            }
        });

        edit_bio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editBio(user_bio);
            }
        });

        edit_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open devices gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
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

    //load image from storage
    private void loadProfilePicture(String uid) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(uid);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(UserProfile.this).load(uri).into(profile_picture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOAD PIC", e.getMessage());
            }
        });
    }

    //upload profile picture to Firebase Storage
    private void uploadImage(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File....");
        progressDialog.show();

        String fileName = String.valueOf(FirebaseAuth.getInstance().getUid());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);


        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(UserProfile.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(UserProfile.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        Log.d("UPLOAD IMAGE", e.getMessage());

                    }
                });
    }

    private void editBio(TextView user_bio) {
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit bio");

        //set layout
        LinearLayout linearLayout = new LinearLayout(this);
        //views to set in dialog
        final EditText newBio = new EditText(this);
        newBio.setHint("New bio");
        newBio.setText(user_bio.getText().toString());
        newBio.setInputType(InputType.TYPE_CLASS_TEXT);
        newBio.setMinEms(5);

        linearLayout.addView(newBio);
        linearLayout.setPadding(20, 20, 20, 20);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save changes
                user_bio.setText(newBio.getText());

                String uid = FirebaseAuth.getInstance().getUid();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                databaseRef.child("Users").child(uid).child("Bio").setValue(user_bio.getText().toString());
            }
        });
        //buttons cancel
        builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog
                dialogInterface.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void editUsername(TextView username) {
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Uredi ime");

        //set layout
        LinearLayout linearLayout = new LinearLayout(this);
        //views to set in dialog
        final EditText newUsername = new EditText(this);
        newUsername.setHint("Novo ime");
        newUsername.setText(username.getText());
        newUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        newUsername.setMinEms(5);

        linearLayout.addView(newUsername);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Spremi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save changes
                username.setText(newUsername.getText());

                String uid = FirebaseAuth.getInstance().getUid();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                databaseRef.child("Users").child(uid).child("Name").setValue(username.getText().toString());

                userProfileUsername = String.valueOf(username.getText());
            }
        });
        //buttons cancel
        builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog
                dialogInterface.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            Uri imageUri = data.getData();
            String imagePath = getImagePath(imageUri);

            // Store the image path in Firebase Realtime Database
            String uid = FirebaseAuth.getInstance().getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> imagePathMap = new HashMap<>();
            imagePathMap.put("ImagePath", imagePath);
            databaseRef.child("Users").child(uid).updateChildren(imagePathMap);

            // Load the image into the ImageView using Glide or any other image loading library
            Glide.with(this).load(imageUri).into(profile_picture);
            uploadImage(imageUri);
        }
    }

    private String getImagePath(Uri imageUri) {
        String imagePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return imagePath;
    }

    private void loadPosts(List<String> post_list, WorkoutAdapter adapter, List<String> post_id) {
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("Classes");

        classesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post_list.clear();
                post_id.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    post_id.add(dataSnapshot.getKey());
                }

                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                postsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(post_id.contains(dataSnapshot.getKey())){
                                StringBuilder post = new StringBuilder();

                                post.append(dataSnapshot.child("Type").getValue(String.class)).append("-");
                                post.append(dataSnapshot.child("Trainer").getValue(String.class)).append("-");
                                post.append(dataSnapshot.child("Location").getValue(String.class)).append("-");
                                post.append(dataSnapshot.child("Date").getValue(String.class)).append(", ").append(dataSnapshot.child("Time").getValue(String.class)).append("-");

                               // int spots = dataSnapshot.child("Spots").getValue(Integer.class) - dataSnapshot.child("Clicks").getValue(Integer.class);

                                post.append(dataSnapshot.child("Spots").getValue(String.class)).append("-").append("end");

                                post_list.add(post.toString());
                            }
                        }
                        posts.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Profile", error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error retrieving post data: " + error.getMessage());
            }
        });
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