package me.davisallen.oneanddone;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class GoalCreateFragment extends Fragment {

    DailyGoalCreatedListener mListener;

    @BindView(R.id.et_create_goal) EditText mEditTextDailyGoal;
    @BindView(R.id.button_create_goal) Button mCreateGoalButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal_create, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListener = (DailyGoalCreatedListener) getActivity();

        mEditTextDailyGoal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_GO) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    createGoal();
                    return true;
                } else {
                    return false;
                }
            }
        });

        mCreateGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGoal();
            }
        });
    }

    public void createGoal() {
        String goal = mEditTextDailyGoal.getText().toString();

        if (goal.length() > 0) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            mListener.onCreateGoal(goal);
        } else {
            // TODO: Make an error notification
        }
    }

    interface DailyGoalCreatedListener {
        public void onCreateGoal(String goal);
    }
}
