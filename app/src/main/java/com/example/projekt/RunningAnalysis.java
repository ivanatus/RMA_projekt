package com.example.projekt;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunningAnalysis#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunningAnalysis extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RunningAnalysis() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RunningAnalysis.
     */
    // TODO: Rename and change types and number of parameters
    public static RunningAnalysis newInstance(String param1, String param2) {
        RunningAnalysis fragment = new RunningAnalysis();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_analysis, container, false);

        TextView add_run = view.findViewById(R.id.add_run);

        TextView fastest = view.findViewById(R.id.fastest_run);
        TextView longest = view.findViewById(R.id.longest_run);
        TextView last = view.findViewById(R.id.last_run);

        LineChart run_graph = view.findViewById(R.id.run_graph);
        TextView graph_variable = view.findViewById(R.id.graph_vars);
        graph_variable.setText("distance");

        loadGraph(run_graph, graph_variable.getText().toString());

        loadRuns(fastest, longest, last);

        add_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRun();
            }
        });

        graph_variable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(graph_variable.getText().toString().equals("distance")){
                    graph_variable.setText("time");
                } else{
                    graph_variable.setText("distance");
                }

                loadGraph(run_graph, graph_variable.getText().toString());
            }
        });

        return view;
    }

    private void addRun() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setPadding(80, 80, 80, 80);
        final EditText lenght = new EditText(getActivity());
        lenght.setHint("Lenght of the run in km");
        lenght.setWidth(2000);
        final EditText time = new EditText(getActivity());
        time.setHint("Lenght of the run in minutes");
        time.setWidth(2000);

        linearLayout.addView(lenght);
        linearLayout.addView(time);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        builder.setView(linearLayout);

        builder.setTitle("Add a new run")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button clicked
                    Date today = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String workout_date = formatter.format(today);

                    DatabaseReference workoutRef = FirebaseDatabase.getInstance().getReference().child("Workouts");
                    DatabaseReference newWorkoutRef = workoutRef.push();

                    Map<String, Object> workoutData = new HashMap<>();
                    workoutData.put("Type", "run");
                    workoutData.put("Distance", lenght.getText().toString());
                    workoutData.put("Time", time.getText().toString());
                    workoutData.put("Date", workout_date);
                    workoutData.put("UserID", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    newWorkoutRef.setValue(workoutData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Data successfully pushed to the database
                                Log.d("Run", "dodalo u db");
                            } else {
                                // Failed to push data to the database
                                Log.d("Run", task.getException().getMessage());
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Negative button clicked
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static String findClosestDate(String dateStr1, String dateStr2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();

        try {
            Date date1 = sdf.parse(dateStr1);
            Date date2 = sdf.parse(dateStr2);

            long diff1 = Math.abs(date1.getTime() - today.getTime());
            long diff2 = Math.abs(date2.getTime() - today.getTime());

            if (diff1 < diff2) {
                return dateStr1;
            } else {
                return dateStr2;
            }
        } catch (ParseException e) {
            // Handle parsing errors here, if needed
            e.printStackTrace();
        }

        return null; // Return null if there are parsing errors
    }

    private void loadGraph(LineChart run_graph, String variable) {
        DatabaseReference runsRef = FirebaseDatabase.getInstance().getReference().child("Workouts");
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();

        ValueEventListener workoutListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.child("UserID").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        if (dataSnapshot.child("Type").getValue(String.class).equals("run")) {
                            xValues.add(xValues.size() + 1);
                            if(variable.equals("distance")){
                                yValues.add(Integer.parseInt(dataSnapshot.child("Distance").getValue(String.class)));
                            } else if(variable.equals("time")){
                                yValues.add(Integer.parseInt(dataSnapshot.child("Time").getValue(String.class)));
                            }
                        }
                    }
                }

                // Create an array to hold data points (entries)
                ArrayList<Entry> entries = new ArrayList<>();

                for(int i = 0; i < xValues.size(); i++){
                    entries.add(new Entry(xValues.get(i), yValues.get(i)));
                }

                // Create a LineDataSet from the entries
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setColor(Color.BLUE);
                dataSet.setCircleColor(Color.RED);
                dataSet.setCircleRadius(5f);

                // Create a LineData object and add the dataSet to it
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);
                LineData lineData = new LineData(dataSets);

                // Set up the X-axis (assuming x[] represents dates)
                XAxis xAxis = run_graph.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                // Set the data to the chart
                run_graph.setData(lineData);

                // Refresh the chart to display the data
                run_graph.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("GRAPH_load", error.getMessage());
            }
        };

        runsRef.addValueEventListener(workoutListener);
    }

    private void loadRuns(TextView fastest, TextView longest, TextView latest) {
        DatabaseReference runsRef = FirebaseDatabase.getInstance().getReference().child("Workouts");

        ValueEventListener workoutListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fast = new String();
                String dist = new String();
                String last = new String();

                StringBuilder fastest_info = new StringBuilder();
                StringBuilder distance_info = new StringBuilder();
                StringBuilder last_info = new StringBuilder();

                String fast_info_tv = new String();
                String dist_info_tv = new String();
                String last_info_tv = new String();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.child("UserID").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        if(dataSnapshot.child("Type").getValue(String.class).equals("run")) {
                            //find longest run
                            String distance = dataSnapshot.child("Distance").getValue(String.class);
                            if (dist.length() == 0 || Integer.parseInt(dist) < Integer.parseInt(distance)) {
                                dist = distance;
                                distance_info.append(dataSnapshot.child("Distance").getValue(String.class)).append(" km, ");
                                distance_info.append(dataSnapshot.child("Time").getValue(String.class)).append(" min, ");
                                distance_info.append(dataSnapshot.child("Date").getValue(String.class));

                                dist_info_tv = distance_info.toString();
                                distance_info.replace(0, distance_info.length(), "");
                            }

                            //find last run
                            String this_date = dataSnapshot.child("Date").getValue(String.class);
                            if (last.length() == 0 || findClosestDate(last, this_date).equals(this_date)) {
                                last_info.append(dataSnapshot.child("Distance").getValue(String.class)).append(" km, ");
                                last_info.append(dataSnapshot.child("Time").getValue(String.class)).append(" min, ");
                                last_info.append(dataSnapshot.child("Date").getValue(String.class));

                                last_info_tv = last_info.toString();
                                last_info.replace(0, last_info.length(), "");
                            }

                            //find fastest run
                            String time = dataSnapshot.child("Time").getValue(String.class);
                            String velocity = String.valueOf(Integer.parseInt(distance) / Integer.parseInt(time));
                            if (fast.length() == 0 || Float.parseFloat(fast) < Float.parseFloat(velocity)) {
                                fastest_info.append(dataSnapshot.child("Distance").getValue(String.class)).append(" km, ");
                                fastest_info.append(dataSnapshot.child("Time").getValue(String.class)).append(" min, ");
                                fastest_info.append(dataSnapshot.child("Date").getValue(String.class));

                                fast_info_tv = fastest_info.toString();
                                fastest_info.replace(0, fastest_info.length(), "");
                            }
                        }
                    }
                }
                distance_info.append(" Longest run: ").append(dist_info_tv);
                longest.setText(distance_info.toString());
                last_info.append(" Last run: ").append(last_info_tv);
                latest.setText(last_info);
                fastest_info.append(" Fastest run: ").append(fast_info_tv);
                fastest.setText(fastest_info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Load", error.getMessage());
            }
        };

        runsRef.addValueEventListener(workoutListener);
    }
}