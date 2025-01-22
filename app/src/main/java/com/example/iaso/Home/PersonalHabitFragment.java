package com.example.iaso.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iaso.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalHabitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalHabitFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PersonalHabitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalHabitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalHabitFragment newInstance(String param1, String param2) {
        PersonalHabitFragment fragment = new PersonalHabitFragment();
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
        View view = inflater.inflate(R.layout.fragment_personal_habit, container, false);

        TextView upcoming = view.findViewById(R.id.upcomingDescription1);
        upcoming.setText("You Are Currently On The BETA Model. \n" +
                "The Dynamic Habit Feature " + "Will Release Spring 2024\n" +
                "Additionally, All BETA users Will Recieve IASO X\n" +
                "For Absolutely Free.\n" +
                "Thanks For Being With Us!");


        Animation fadeInLiftAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_lift);
        upcoming.startAnimation(fadeInLiftAnimation);

        ImageView beta = view.findViewById(R.id.betaLogo3);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_animation);
        beta.startAnimation(fadeInAnimation);

        return view;
    }

    public void goBack(View v){
        Intent b = new Intent(getContext(), PersonalHomePageFragment.class);
        startActivity(b);
    }
}