package com.example.covid_jobber.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.covid_jobber.R;
import com.example.covid_jobber.activities.MainActivity;
import com.example.covid_jobber.classes.Job;
import com.example.covid_jobber.classes.services.ApiCall;
import com.example.covid_jobber.classes.services.arrayAdapter;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwipeFragment extends Fragment {

    private MainActivity mainActivity;

    private arrayAdapter arrayAdapter;

    ListView listView;
    List<Job> jobitems;


    public SwipeFragment() {
        // Required empty public constructor
    }

    public static SwipeFragment newInstance() {
        return new SwipeFragment();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_swipe, container, false);

        mainActivity = (MainActivity) getActivity();

        jobitems = new ArrayList<Job>();
        jobitems.add(new Job());

        arrayAdapter = new arrayAdapter(getActivity(), R.layout.item, jobitems);

        SwipeFlingAdapterView flingContainer = v.findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                jobitems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Job currentjob = (Job) dataObject;
                Job favorite = currentjob;
                if(favorite != null){
                    mainActivity.addFavoriteJob(favorite);
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if(itemsInAdapter>=3){
                    return;
                }


//                Log.d("TAG", "CARDS ABOUT TO RUN OUT");

                mainActivity.getHandler().makeApiCall(new ApiCall(mainActivity.getFilterFragment().getFilter()) {
                    @Override
                    public void callback(JSONArray results) {
                        try {
                            mainActivity.resultsToJobs(results);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
            }

            //Durch die Farbänderung stürzt die App bisher manchmal ab
            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                if(view == null){
                    return;
                }
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener((itemPosition, dataObject) -> {
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void addJob(List<Job> newJobs) {
        jobitems.addAll(newJobs);
    }

}