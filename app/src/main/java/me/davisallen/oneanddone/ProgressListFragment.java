package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    @BindView(R.id.goal_list_item) ConstraintLayout mGoalListItem;

    LinearLayoutManager mLayoutManager;
    GoalListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_list, container, false);
        ButterKnife.bind(this, view);

        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GoalListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public class GoalListAdapter extends RecyclerView.Adapter<GoalListAdapter.ViewHolder> {

        @Override
        public GoalListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}
