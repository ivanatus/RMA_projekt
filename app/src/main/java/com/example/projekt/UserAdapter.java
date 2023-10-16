package com.example.projekt;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UserAdapter extends ArrayAdapter<String> {

    public UserAdapter(@NonNull Context context, List<String> items) {
        super(context, R.layout.user_list, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list, parent, false);
        }

        ImageView profile_picture = convertView.findViewById(R.id.user_picture_list);
        TextView username = convertView.findViewById(R.id.username_list);
        TextView last_message = convertView.findViewById(R.id.username_message);

        String currentItemText = getItem(position).toString();
        String[] parts = currentItemText.split(";");

        if(parts.length >= 3) {
            loadProfilePicture(parts[0], profile_picture);
            username.setText(parts[1]);
            if(parts[2].equals(" ")){
                last_message.setVisibility(View.GONE);
            } else {
                last_message.setText(parts[2]);
            }
        }

        return convertView;
    }

    //load image from storage
    private void loadProfilePicture(String uid, ImageView profile_picture) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(uid);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).into(profile_picture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOAD PIC", e.getMessage());
            }
        });
    }
}
