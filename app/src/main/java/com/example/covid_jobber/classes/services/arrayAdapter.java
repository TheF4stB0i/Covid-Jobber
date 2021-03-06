package com.example.covid_jobber.classes.services;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.covid_jobber.R;
import com.example.covid_jobber.activities.MainActivity;
import com.example.covid_jobber.classes.Job;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<Job> {

    Context context;
    MainActivity mainActivity;

    public arrayAdapter(Context context, int resourceId, List<Job> items, MainActivity mainActivity) {
        super(context, resourceId, items);
        this.mainActivity = mainActivity;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Job job_item  = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.txt_item_title);
        TextView location = convertView.findViewById(R.id.txt_item_location);
        TextView employer = convertView.findViewById(R.id.txt_item_company);
        TextView workingperiod = convertView.findViewById(R.id.txt_item_contracttime);
        TextView salary = convertView.findViewById(R.id.txt_item_salary);

        //        If it is first item
        if(job_item.getId() == -1){
            title.setText(job_item.getTitle());

//            Change title layout (Start Swiping Text)
            ConstraintLayout constraintLayout = convertView.findViewById(R.id.layout_item_constraint);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.txt_item_title,ConstraintSet.RIGHT,R.id.layout_item_constraint,ConstraintSet.RIGHT,0);
            constraintSet.connect(R.id.txt_item_title,ConstraintSet.TOP,R.id.layout_item_constraint,ConstraintSet.TOP,0);
            constraintSet.connect(R.id.txt_item_title,ConstraintSet.LEFT,R.id.layout_item_constraint,ConstraintSet.LEFT,0);
            constraintSet.connect(R.id.txt_item_title,ConstraintSet.BOTTOM,R.id.layout_item_constraint,ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
            title.setTextSize(32);

            TextView location_des = convertView.findViewById(R.id.txt_item_location_description);
            TextView employer_des = convertView.findViewById(R.id.txt_item_company_description);
            TextView workingperiod_des = convertView.findViewById(R.id.txt_item_contracttime_description);
            TextView salary_des = convertView.findViewById(R.id.txt_item_salary_description);

            location.setVisibility(View.GONE);
            employer.setVisibility(View.GONE);
            workingperiod.setVisibility(View.GONE);
            salary.setVisibility(View.GONE);

            location_des.setVisibility(View.GONE);
            employer_des.setVisibility(View.GONE);
            workingperiod_des.setVisibility(View.GONE);
            salary_des.setVisibility(View.GONE);

            Button btn = convertView.findViewById(R.id.btn_item_more);
            btn.setVisibility(View.GONE);
            return convertView;
        }

        boolean locationActive = mainActivity.getFilterFragment().isLocationActive();
        double latlocation = mainActivity.getFilterFragment().getLatitude();
        double lonlocation = mainActivity.getFilterFragment().getLongitude();

        double latitude = job_item.getLatitude();
        double longitude = job_item.getLongitude();

        title.setText(job_item.getTitle());

        if (locationActive) {
            //Berechnung des Abstandes vom persönlichen Standort zur Arbeitsstätte (Luftlinie)
            double dx, dy, lat, distance;
            lat = (latitude + latlocation) / 2 * (Math.PI/180);
            dx = 111.3 * Math.cos(lat) * (longitude - lonlocation);
            dy = 111.3 * (latitude - latlocation);
            distance = Math.sqrt(dx * dx + dy * dy);
            distance = Math.round(distance *100.0) /100.0;
            String dist = " (" + distance + " Km)";
            String locationString = job_item.getcity() + dist;
            location.setText(locationString);
        } else {
            location.setText(job_item.getcity());
        }

        employer.setText(job_item.getCompany());

        workingperiod.setText(job_item.getContractTime().getTranslation(mainActivity.language));

        if(job_item.getSalary() < 0){
            String text = "";
            switch (mainActivity.language){
                case GERMAN:
                    text = "Unbekannt"; break;
                case ENGLISH:
                    text = "Unknown"; break;
            }
            salary.setText(text);
        }else {
            String salaryString = job_item.getSalary() + " €";
            salary.setText(salaryString);
        }

        return convertView;
    }

}