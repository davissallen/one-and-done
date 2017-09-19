package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class ProgressListFragment extends Fragment {

    // Bind views with Butterknife
    @BindView(R.id.rv_progress_list) RecyclerView mRecyclerView;

    LinearLayoutManager mLayoutManager;
    GoalListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_list, container, false);
        ButterKnife.bind(this, view);

        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // TODO: pass real goal information
        // a.k.a. init the dataset
        ArrayList<String> goals = new ArrayList<String>();
        goals.add("Bake cookies.");
        goals.add("Call mom.");
        goals.add("Make a cake.");
        mAdapter = new GoalListAdapter(goals);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public class GoalListAdapter extends RecyclerView.Adapter<GoalListAdapter.ViewHolder> {

        private ArrayList<String> mGoals;

        public GoalListAdapter(ArrayList<String> goals) {
            if (goals != null && goals.size() > 0) {
                mGoals = goals;
            } else {
                mGoals = new ArrayList<>();
            }
        }

        @Override
        public GoalListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mGoalTextView.setText(mGoals.get(position));
            holder.mDateTextView.setText("23");
            holder.mStatusImageView.setImageResource(R.drawable.ic_cancel_red_36dp);
        }

        @Override
        public int getItemCount() {
            return mGoals.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.li_goal) TextView mGoalTextView;
            @BindView(R.id.li_date) TextView mDateTextView;
            @BindView(R.id.li_status) ImageView mStatusImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }
    }

}
