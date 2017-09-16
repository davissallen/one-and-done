package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;

import static me.davisallen.oneanddone.MainActivity.PARAM_CREATE_GOAL;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/14/17
 */

public class MainViewFragment extends Fragment {

    private AppCompatActivity mActivity;

    private String mGoal;

    private TextView mBannerGoalTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (AppCompatActivity) getActivity();

        Bundle receivedBundle = getArguments();
        if (receivedBundle != null && receivedBundle.containsKey(PARAM_CREATE_GOAL)) {
            mGoal = receivedBundle.getString(PARAM_CREATE_GOAL);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBannerGoalTextView = mActivity.findViewById(R.id.tv_banner_goal);
        mBannerGoalTextView.setText(mGoal);
    }
}
