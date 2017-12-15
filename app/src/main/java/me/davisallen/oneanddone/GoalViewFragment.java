package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static me.davisallen.oneanddone.MainActivity.PARAM_CREATE_GOAL;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/17/17
 */

public class GoalViewFragment extends Fragment {

    private String mGoal;

    @BindView(R.id.goal_view_container) ConstraintLayout mContainer;
    @BindView(R.id.clock_goal_view) TextClock mTextClock;
    @BindView(R.id.tv_goal_view_date) TextView mDateTextView;
    @BindView(R.id.tv_goal_view_goal) TextView mGoalTextView;
    @BindView(R.id.pulsator) PulsatorLayout mPulsator;
    @BindView(R.id.button_complete) ImageView mCompleteButton;

    OnGoalCompleteListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener = (OnGoalCompleteListener) getActivity();

        Bundle receivedArgs = getArguments();
        if (receivedArgs != null && receivedArgs.containsKey(PARAM_CREATE_GOAL)) {
            mGoal = receivedArgs.getString(PARAM_CREATE_GOAL);
        } else {
            mGoal = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_view, container, false);
        ButterKnife.bind(this, view);

        mPulsator.start();
        mCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContainer.setBackgroundColor(R.drawable.blue_gradient);
                mCompleteButton.setClickable(false);
                setGoalCompleted();
            }
        });

        return view;
    }

    public void setGoalCompleted() {
        mListener.onGoalCompleted();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDate();
        mGoalTextView.setText(mGoal);
    }

    private void setDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
        mDateTextView.setText(sdf.format(new Date()));
    }

    interface OnGoalCompleteListener {
        public void onGoalCompleted();
    }
}
