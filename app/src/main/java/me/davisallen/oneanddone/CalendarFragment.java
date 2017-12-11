package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class CalendarFragment extends Fragment {

    private static final String GOALS_KEY = "goals_key";

    @BindView(R.id.count_completed) TextView mCountCompleted;
    @BindView(R.id.count_neutral) TextView mCountNeutral;
    @BindView(R.id.count_uncompleted) TextView mCountUncompleted;

    private ArrayList<Goal> mGoals;
    private MainActivity mParentActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this, view);

        mParentActivity = (MainActivity) getActivity();

        return view;
    }



    private void updateCountViews() {


//        mCountCompleted.setText(String.valueOf(count_completed));
//        mCountNeutral.setText(String.valueOf(count_neutral));
//        mCountUncompleted.setText(String.valueOf(count_uncompleted));
    }
}
