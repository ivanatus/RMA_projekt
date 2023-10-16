package com.example.projekt;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends ArrayAdapter<String>  {

    public MessagesAdapter(Context context, List<String> items){
        super(context, R.layout.messaging_list, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.messaging_list, parent, false);
        }

        String currentItemText = getItem(position).toString();

        CircleImageView picture = convertView.findViewById(R.id.sender);
        TextView message = convertView.findViewById(R.id.message_text);

        String[] parts = currentItemText.split(";");
        for(int i = 0; i < parts.length; i++){
            Log.d("Adapter tag", parts[i]);
        }

        if(parts.length >= 2){
            loadProfilePicture(parts[0], picture);
            message.setText(parts[1]);
            if(parts[0].equals(FirebaseAuth.getInstance().getUid())){
                message.setBackgroundColor(Color.parseColor("#7393B3"));
            }
        }

        return convertView;
    }

    private void loadProfilePicture(String uid, CircleImageView picture) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(uid);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (!isActivityDestroyed()) {
                    Glide.with(getContext()).load(uri).into(picture);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOAD PIC", e.getMessage());
            }
        });
    }
    @SuppressLint("RestrictedApi")
    private boolean isActivityDestroyed() {
        return getActivity(getContext()) == null || getActivity(getContext()).isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && getActivity(getContext()).isDestroyed());
    }
}
