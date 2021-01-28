package com.example.covid_jobber.fragments;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.covid_jobber.R;
import com.example.covid_jobber.classes.Job;
import com.example.covid_jobber.databinding.FragmentFavoritesBinding;
import com.example.covid_jobber.databinding.FragmentFiltersBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment implements View.OnClickListener {

//    variables
    private final List<Job> favoriteJobs = new ArrayList<>();
    private final List<FrameLayout> frames = new ArrayList<>();
    private final List<FavoriteJobFragment> fragments = new ArrayList<>();

    private FragmentFavoritesBinding binding;

    private boolean deleting = false;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);

        deleting = false;
        binding.btnFavoritesEdit.setOnClickListener(this);

        frames.clear();
        fragments.clear();

        for (int i = 0; i < favoriteJobs.size(); i++) {
            Job j = favoriteJobs.get(i);
            addJobToLayout(j);
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if(v == binding.btnFavoritesEdit){
            if(!deleting){
                deleting = true;
                binding.btnFavoritesEdit.setBackgroundTintList(ContextCompat.getColorStateList(this.getContext(), R.color.primary_dark));
                for (FavoriteJobFragment fragment : fragments) {
                    fragment.setDeletable(true);
                }
            } else {
                deleting = false;
                binding.btnFavoritesEdit.setBackgroundTintList(ContextCompat.getColorStateList(this.getContext(), R.color.primary));
                for (FavoriteJobFragment fragment : fragments) {
                    fragment.setDeletable(false);
                }
            }
        }
    }

    public void addFavorite(Job job){
        favoriteJobs.add(job);
    }

    public void deleteFavorite(FavoriteJobFragment fragment){
        favoriteJobs.remove(fragment.getJob());
        int index = fragments.indexOf(fragment);
        FrameLayout frame = frames.get(index);
        binding.layoutFavoritesJobs.removeView(frame);
        fragments.remove(fragment);
        frames.remove(frame);
    }

    private void addJobToLayout(Job j){
//            generate FrameLayout
        FrameLayout newFrame = new FrameLayout(this.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        newFrame.setLayoutParams(params);
        newFrame.setId(View.generateViewId());
        frames.add(newFrame);

//            add FrameLayout to LayoutFavoriteJobs
        binding.layoutFavoritesJobs.addView(newFrame, 0);

//            generate FavoriteJobFragment
        FavoriteJobFragment newJobFragment = new FavoriteJobFragment(j);
        fragments.add(newJobFragment);

//            Place FavoriteJobFragment in the new frame
        getActivity().getSupportFragmentManager().beginTransaction().replace(newFrame.getId(),newJobFragment).commitAllowingStateLoss();
    }

}