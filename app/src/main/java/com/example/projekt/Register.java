package com.example.projekt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Register extends AppCompatActivity {

    TextInputEditText reg_email_input, reg_password_input, reg_username_input, reg_birthdate_input;
    Button btn_register;
    FirebaseAuth mAuth;
    ProgressBar progress_bar_reg;
    TextView login_now;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Home_new.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        reg_email_input = findViewById(R.id.email_register);
        reg_password_input = findViewById(R.id.password_register);
        btn_register = findViewById(R.id.btn_register);
        progress_bar_reg = findViewById(R.id.progress_bar_reg);
        login_now = findViewById(R.id.login_now);
        reg_username_input = findViewById(R.id.username_register);
        reg_birthdate_input = findViewById(R.id.birthdate_register);

        login_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress_bar_reg.setVisibility(View.VISIBLE);
                String email, password, username, birth_date;
                email = String.valueOf(reg_email_input.getText());
                password = String.valueOf(reg_password_input.getText());
                username = String.valueOf(reg_username_input.getText());
                birth_date = String.valueOf(reg_birthdate_input.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progress_bar_reg.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(Register.this, "Authentication successful.",
                                            Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //get user email and uid from auth
                                    //String email = user.getEmail();
                                    String uid = user.getUid();

                                    //when user is registered store user info in firebase realtime database
                                    FirebaseDatabase database = FirebaseDatabase.getInstance(); //firebase database instance

                                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                                    databaseRef.child("Users").child(uid).child("Role").setValue("Regular");
                                    databaseRef.child("Users").child(uid).child("Name").setValue(username);
                                    databaseRef.child("Users").child(uid).child("Date of birth").setValue(birth_date);
                                    databaseRef.child("Users").child(uid).child("Conversations").setValue("");
                                    databaseRef.child("Users").child(uid).child("Bio").setValue("");
                                    Date today = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    String register_date = formatter.format(today);
                                    databaseRef.child("Users").child(uid).child("Date of membership").setValue(register_date);

                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                   Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}