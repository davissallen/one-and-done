package me.davisallen.oneanddone;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class GoalCreateFragment extends Fragment {

    DailyGoalCreatedListener mListener;

    @BindView(R.id.et_create_goal) TextInputEditText mEditTextDailyGoal;
    @BindView(R.id.button_create_goal) Button mCreateGoalButton;
    private AppCompatActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal_create, container, false);
        ButterKnife.bind(this, view);

        mActivity = (AppCompatActivity) getActivity();
        ActionBar toolbar = mActivity.getSupportActionBar();
        if (toolbar != null) {
            toolbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }

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
            Timber.e("The goal entered has 0 length.");
        }
    }

    interface DailyGoalCreatedListener {
        public void onCreateGoal(String goal);
    }
}
