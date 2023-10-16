package com.example.projekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StrenghtItemAdapter extends ArrayAdapter<String> {

    public StrenghtItemAdapter(Context context, List<String> items){
        super(context, R.layout.strenght_analysis_list, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.strenght_analysis_list, parent, false);
        }

        String item = getItem(position);

        TextView type = convertView.findViewById(R.id.type);
        TextView weight = convertView.findViewById(R.id.weight);
        TextView series = convertView.findViewById(R.id.series);
        TextView reps = convertView.findViewById(R.id.reps);

        String[] workouts = item.split(";");

        for(int i = 0; i < workouts.length; i++){
            String[] current_workout = workouts[i].split("-");
            type.setText("Type: " + current_workout[0]);
            weight.setText("Weight: " + current_workout[1] + " kg");
            series.setText("Number of series: " + current_workout[2]);
            reps.setText("Number of reps: " + current_workout[3]);
        }

        return  convertView;
    }
}