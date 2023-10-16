package com.example.projekt;

import static android.text.InputType.TYPE_DATETIME_VARIATION_DATE;
import static android.text.InputType.TYPE_DATETIME_VARIATION_TIME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WorkoutAdapter extends ArrayAdapter<String> {

    boolean isAdmin = false;
    List<String> id_list;
    LinearLayout edit;
    TextView spots;
    Button save, cancel, delete;
    Context context;
    Button select;

    public WorkoutAdapter(Context context, List<String> items, List<String> id_list, boolean admin){
        super(context, R.layout.workouts_list, items);
        this.id_list = id_list;
        this.isAdmin = admin;
        Log.d("Workouts", String.valueOf(this.isAdmin));
        this.context = getContext();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.workouts_list, parent, false);
        }

        String currentItemText = getItem(position).toString();

        TextView type = convertView.findViewById(R.id.workout_type);
        TextView trainer = convertView.findViewById(R.id.workout_trainer);
        TextView location = convertView.findViewById(R.id.workout_location);
        TextView date_and_time = convertView.findViewById(R.id.workout_time);
        spots = convertView.findViewById(R.id.workout_spots);
        select = convertView.findViewById(R.id.workout_select);

        checkIfUserIsAdmin(FirebaseAuth.getInstance().getUid());

        if(isAdmin){ select.setText("Edit"); }

        String[] parts = currentItemText.split("-");
        Log.d("PARTS", String.valueOf(isAdmin));

        if (parts.length >= 3) {
            type.setText(parts[0]);
            trainer.setText(parts[1]);
            location.setText(parts[2]);
            date_and_time.setText((parts[3]));
            spots.setText(parts[4]);
        }

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("target_location", location.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAdmin){
                    Intent intent = new Intent(context, EditPost.class);
                    intent.putExtra("post id", id_list.get(position));
                    intent.putExtra("type", type.getText().toString());
                    intent.putExtra("trainer", trainer.getText().toString());
                    intent.putExtra("location", location.getText().toString());
                    intent.putExtra("date and time", date_and_time.getText().toString());
                    intent.putExtra("spots", spots.getText().toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    signUpUser(id_list.get(position), select.getText().toString());

                    String[] time_and_date_split = date_and_time.getText().toString().split(", ");

                    String time = "08:00:00";
                    String[] date = time_and_date_split[0].split("/");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date targetDate = null;
                    try {
                        targetDate = sdf.parse(date[2] + "-" + date[1] + "-" + date[0] + " " + time);
                    } catch (ParseException e) {
                        Log.d("Notification", e.getMessage());
                        Log.d("Notification", "Doslo je do messagea");
                    }
                    long targetTimeMillis = targetDate.getTime();


                    // Calculate the time 3 seconds from now in milliseconds
                    long delayInMillis = 3000; // 3 seconds
                    long notificationTimeInMillis = System.currentTimeMillis() + delayInMillis;

                    String title = "Prijava na novi workout";
                    String content = "Workout: " + type.getText().toString() + "\nDate and time: " + date_and_time.getText().toString() + "\nTrainer: " + trainer.getText().toString();

                    scheduleNotification(getContext(), notificationTimeInMillis, title, content);

                    title = "Prijavljeni ste na današnji workout";

                    scheduleNotification(getContext(), targetTimeMillis, title, content);

                    if(select.getText().toString().equals("Select")){
                        select.setText("Deselect");
                    } else{
                        select.setText("Select");
                    }
                }
            }
        });

        return  convertView;
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

    private void editPosts(String post_id){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post_id);

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("");

        //set layout
        LinearLayout linearLayout = new LinearLayout(getContext());
        final EditText type = new EditText(getContext());
        final EditText date = new EditText(getContext());
        final EditText time = new EditText(getContext());
        final EditText trainer = new EditText(getContext());
        final EditText location = new EditText(getContext());
        final EditText spots = new EditText(getContext());
        final CheckBox indoor = new CheckBox(getContext());
        final Button delete = new Button(getContext());
        delete.setText("Delete workout");

        date.setInputType(TYPE_DATETIME_VARIATION_DATE);
        time.setInputType(TYPE_DATETIME_VARIATION_TIME);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    type.setText(snapshot.child("Type").getValue(String.class));
                    date.setText(snapshot.child("Date").getValue(String.class));
                    time.setText(snapshot.child("Time").getValue(String.class));
                    trainer.setText(snapshot.child("Trainer").getValue(String.class));
                    location.setText(snapshot.child("Location").getValue(String.class));
                    spots.setText(snapshot.child("Spots").getValue(String.class));
                } else{
                    System.out.println("Snapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error retrieving data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        linearLayout.addView(type);
        linearLayout.addView(date);
        linearLayout.addView(time);
        linearLayout.addView(trainer);
        linearLayout.addView(location);
        linearLayout.addView(spots);
        linearLayout.addView(delete);
        linearLayout.setPadding(50, 50, 50, 50);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        builder.setView(linearLayout);

        builder.setPositiveButton("Save changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save changes
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                postRef.child("Description").setValue(type.getText().toString());
                postRef.child("Date").setValue(date.getText().toString());
                postRef.child("Time").setValue(time.getText().toString());
                postRef.child("Location").setValue(location.getText().toString());

            }
        });

        builder.setNegativeButton("Obriši trening", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //show dialog
        builder.create().show();
    }

    private void checkIfUserIsAdmin(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("Role").getValue(String.class);

                    //only admin can publish new posts

                    if (role.equals("Admin")) {
                        select.setText("Edit");
                        setAdmin(true);
                    } else {
                        setAdmin(false);
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

    private void setAdmin(boolean admin){
        isAdmin = admin;
    }

    private void signUpUser(String post_id, String selected) {
        String uid = FirebaseAuth.getInstance().getUid();
        boolean sign_up = false;

        if(selected.equals("Select")){ sign_up = true; }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id);
        DatabaseReference userClassesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Classes");
        int clicks = Integer.parseInt(spots.getText().toString());

        if(sign_up) {
            postRef.child("Spots").setValue(String.valueOf(clicks - 1));
        } else {
            postRef.child("Spots").setValue(String.valueOf(clicks + 1));
        }

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
                                    postRef.child("Clicks").setValue(String.valueOf(clicks + 1));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                } else {
                    // User is not signed up, so add the class
                    userClassesRef.child(post_id).setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Increase the Clicks counter by 1
                                    postRef.child("Clicks").setValue(String.valueOf(clicks - 1));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }
}
