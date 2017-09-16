package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class DailyGoalCreateFragment extends Fragment {

    DailyGoalCreatedListener mListener;

    @BindView(R.id.et_create_goal) EditText mEditTextDailyGoal;

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
                // 6, 66 && 0
                if ((actionId == EditorInfo.IME_ACTION_GO) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    String goal = textView.getText().toString();
                    mListener.onCreateGoal(goal);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    interface DailyGoalCreatedListener {
        public void onCreateGoal(String goal);
    }
}
