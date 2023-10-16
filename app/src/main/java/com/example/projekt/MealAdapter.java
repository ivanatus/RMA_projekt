package com.example.projekt;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends ArrayAdapter {

    public MealAdapter(@NonNull Context context, List<String> items) {
        super(context, R.layout.meal_list, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.meal_list, parent, false);
        }


        String currentItemText = getItem(position).toString();

        TextView time_and_date = convertView.findViewById(R.id.date_and_time);
        TextView food1 = convertView.findViewById(R.id.food);
        TextView weight1 = convertView.findViewById(R.id.amount);
        LinearLayout layout2 = convertView.findViewById(R.id.layout2);
        TextView food2 = convertView.findViewById(R.id.food2);
        TextView weight2 = convertView.findViewById(R.id.amount2);
        LinearLayout layout3 = convertView.findViewById(R.id.layout3);
        TextView food3 = convertView.findViewById(R.id.food3);
        TextView weight3 = convertView.findViewById(R.id.amount3);
        LinearLayout layout4 = convertView.findViewById(R.id.layout4);
        TextView food4 = convertView.findViewById(R.id.food4);
        TextView weight4 = convertView.findViewById(R.id.amount4);
        LinearLayout layout5 = convertView.findViewById(R.id.layout5);
        TextView food5 = convertView.findViewById(R.id.food5);
        TextView weight5 = convertView.findViewById(R.id.amount5);

        String[] meals = currentItemText.split(";");

        if (meals.length >= 1) {
            Log.d("Meals", "Count meals: " + meals.length);
            for(int i = 0; i < meals.length; i++){
                layout2.setVisibility(View.VISIBLE);
                layout3.setVisibility(View.VISIBLE);
                layout4.setVisibility(View.VISIBLE);
                layout5.setVisibility(View.VISIBLE);
                String[] current_meal = meals[i].split("-");
                Log.d("Meals", "Count of each meal parts: " + current_meal.length);
                for (int j = 0; j < current_meal.length; j++){
                    Log.d("Meals", "Each part of current meal: " + current_meal[j]);
                }
                time_and_date.setText(current_meal[0]);
                food1.setText(current_meal[1]);
                weight1.setText(current_meal[2]);
                if(current_meal.length == 3){
                    layout2.setVisibility(View.GONE);
                    layout3.setVisibility(View.GONE);
                    layout4.setVisibility(View.GONE);
                    layout5.setVisibility(View.GONE);
                } else if (current_meal.length == 5) {
                    food2.setText(current_meal[3]);
                    weight2.setText(current_meal[4]);
                    layout3.setVisibility(View.GONE);
                    layout4.setVisibility(View.GONE);
                    layout5.setVisibility(View.GONE);
                } else if (current_meal.length == 7) {
                    food2.setText(current_meal[3]);
                    weight2.setText(current_meal[4]);
                    food3.setText(current_meal[5]);
                    weight3.setText(current_meal[6]);
                    layout4.setVisibility(View.GONE);
                    layout5.setVisibility(View.GONE);
                } else if (current_meal.length == 9) {
                    food2.setText(current_meal[3]);
                    weight2.setText(current_meal[4]);
                    food3.setText(current_meal[5]);
                    weight3.setText(current_meal[6]);
                    food4.setText(current_meal[7]);
                    weight4.setText(current_meal[8]);
                    layout5.setVisibility(View.GONE);
                } else{
                    food2.setText(current_meal[3]);
                    weight2.setText(current_meal[4]);
                    food3.setText(current_meal[5]);
                    weight3.setText(current_meal[6]);
                    food4.setText(current_meal[7]);
                    weight4.setText(current_meal[8]);
                    food5.setText(current_meal[9]);
                    weight5.setText(current_meal[10]);
                }
            }
        }

        Button edit_meal = convertView.findViewById(R.id.edit_meal);
        edit_meal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMeal(food1, food2, food3, food4, food5, weight1, weight2, weight3, weight4, weight5);
            }
        });

        return  convertView;
    }

    private void editMeal(TextView meal_food1, TextView meal_food2, TextView meal_food3, TextView meal_food4, TextView meal_food5, TextView meal_weight1, TextView meal_weight2, TextView meal_weight3, TextView meal_weight4, TextView meal_weight5) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(80, 80, 80, 80);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontal_layout1 = new LinearLayout(getContext());
        horizontal_layout1.setOrientation(LinearLayout.HORIZONTAL);

        TextView food = new TextView(getContext());
        food.setText("Food: ");
        EditText food_name = new EditText(getContext());
        food_name.setWidth(200);
        food_name.setText(meal_food1.getText());
        TextView amount = new TextView(getContext());
        amount.setText("Amount: ");
        EditText weight = new EditText(getContext());
        weight.setWidth(200);
        weight.setText(meal_weight1.getText().toString());
        TextView grams = new TextView(getContext());
        grams.setText("grams");

        horizontal_layout1.addView(food);
        horizontal_layout1.addView(food_name);
        horizontal_layout1.addView(amount);
        horizontal_layout1.addView(weight);
        horizontal_layout1.addView(grams);

        LinearLayout horizontal_layout2 = new LinearLayout(getContext());
        horizontal_layout2.setOrientation(LinearLayout.HORIZONTAL);

        TextView food2 = new TextView(getContext());
        food2.setText("Food: ");
        EditText food_name2 = new EditText(getContext());
        food_name2.setWidth(200);
        food_name2.setText(meal_food2.getText());
        TextView amount2 = new TextView(getContext());
        amount2.setText("Amount: ");
        EditText weight2 = new EditText(getContext());
        weight2.setWidth(200);
        weight2.setText(meal_weight2.getText().toString());
        TextView grams2 = new TextView(getContext());
        grams2.setText("grams");

        horizontal_layout2.addView(food2);
        horizontal_layout2.addView(food_name2);
        horizontal_layout2.addView(amount2);
        horizontal_layout2.addView(weight2);
        horizontal_layout2.addView(grams2);

        LinearLayout horizontal_layout3 = new LinearLayout(getContext());
        horizontal_layout3.setOrientation(LinearLayout.HORIZONTAL);

        TextView food3 = new TextView(getContext());
        food3.setText("Food: ");
        EditText food_name3 = new EditText(getContext());
        food_name3.setWidth(200);
        food_name3.setText(meal_food3.getText());
        TextView amount3 = new TextView(getContext());
        amount3.setText("Amount: ");
        EditText weight3 = new EditText(getContext());
        weight3.setWidth(200);
        weight3.setText(meal_weight3.getText().toString());
        TextView grams3 = new TextView(getContext());
        grams3.setText("grams");

        horizontal_layout3.addView(food3);
        horizontal_layout3.addView(food_name3);
        horizontal_layout3.addView(amount3);
        horizontal_layout3.addView(weight3);
        horizontal_layout3.addView(grams3);

        LinearLayout horizontal_layout4 = new LinearLayout(getContext());
        horizontal_layout4.setOrientation(LinearLayout.HORIZONTAL);

        TextView food4 = new TextView(getContext());
        food4.setText("Food: ");
        EditText food_name4 = new EditText(getContext());
        food_name4.setWidth(200);
        food_name.setText(meal_food4.getText());
        TextView amount4 = new TextView(getContext());
        amount4.setText("Amount: ");
        EditText weight4 = new EditText(getContext());
        weight4.setWidth(200);
        weight4.setText(meal_weight4.getText().toString());
        TextView grams4 = new TextView(getContext());
        grams4.setText("grams");

        horizontal_layout4.addView(food4);
        horizontal_layout4.addView(food_name4);
        horizontal_layout4.addView(amount4);
        horizontal_layout4.addView(weight4);
        horizontal_layout4.addView(grams4);

        LinearLayout horizontal_layout5 = new LinearLayout(getContext());
        horizontal_layout5.setOrientation(LinearLayout.HORIZONTAL);

        TextView food5 = new TextView(getContext());
        food5.setText("Food: ");
        EditText food_name5 = new EditText(getContext());
        food_name5.setWidth(200);
        food_name.setText(meal_food5.getText());
        TextView amount5 = new TextView(getContext());
        amount5.setText("Amount: ");
        EditText weight5 = new EditText(getContext());
        weight5.setWidth(200);
        weight5.setText(meal_weight5.getText().toString());
        TextView grams5 = new TextView(getContext());
        grams5.setText("grams");

        horizontal_layout5.addView(food5);
        horizontal_layout5.addView(food_name5);
        horizontal_layout5.addView(amount5);
        horizontal_layout5.addView(weight5);
        horizontal_layout5.addView(grams5);


        linearLayout.addView(horizontal_layout1);
        linearLayout.addView(horizontal_layout2);
        linearLayout.addView(horizontal_layout3);
        linearLayout.addView(horizontal_layout4);
        linearLayout.addView(horizontal_layout5);
        builder.setView(linearLayout);

        builder.setTitle("Add a new meal")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button clicked
                    List<String> foods = new ArrayList<>();
                    List<String> weights = new ArrayList<>();

                    if(!food_name.getText().toString().isEmpty()){
                        foods.add(food_name.getText().toString());
                    }
                    if(!food_name2.getText().toString().isEmpty()){
                        foods.add(food_name2.getText().toString());
                    }
                    if(!food_name3.getText().toString().isEmpty()){
                        foods.add(food_name3.getText().toString());
                    }
                    if(!food_name4.getText().toString().isEmpty()){
                        foods.add(food_name4.getText().toString());
                    }
                    if(!food_name5.getText().toString().isEmpty()){
                        foods.add(food_name5.getText().toString());
                    }

                    if(!weight.getText().toString().isEmpty()){
                        weights.add(weight.getText().toString());
                    }
                    if(!weight2.getText().toString().isEmpty()){
                        weights.add(weight.getText().toString());
                    }
                    if(!weight3.getText().toString().isEmpty()){
                        weights.add(weight3.getText().toString());
                    }
                    if(!weight4.getText().toString().isEmpty()){
                        weights.add(weight4.getText().toString());
                    }
                    if(!weight5.getText().toString().isEmpty()){
                        weights.add(weight5.getText().toString());
                    }

                    //addMealToDB(foods, weights);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
