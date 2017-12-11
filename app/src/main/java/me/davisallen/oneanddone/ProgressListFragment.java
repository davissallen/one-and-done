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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class ProgressListFragment extends Fragment {

    private static final String GOALS_KEY = "goals_key";

    // Bind views with Butterknife
    @BindView(R.id.rv_progress_list) RecyclerView mRecyclerView;

    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }
    protected LayoutManagerType mCurrentLayoutManagerType;

    // Declare variables for fragment setup.
    private MainActivity mParentActivity;
    private RecyclerView.LayoutManager mLayoutManager;
    private GoalsAdapter mGoalsAdapter;
    private ArrayList<Goal> mGoals;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Bind views.
        View view = inflater.inflate(R.layout.fragment_progress_list, container, false);
        ButterKnife.bind(this, view);

        // Initialize variables.
        mParentActivity = (MainActivity) getActivity();
        mLayoutManager = new LinearLayoutManager(getActivity());
        setRecyclerViewLayoutManager();

        if (savedInstanceState != null && savedInstanceState.containsKey(GOALS_KEY)) {
            mGoals = savedInstanceState.getParcelableArrayList(GOALS_KEY);
            initGoals();
        } else {
            mParentActivity.mGoalsDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Create list of goals.
                    mGoals = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Goal goal = snapshot.getValue(Goal.class);
                        mGoals.add(goal);
                    }
                    initGoals();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



        return view;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mGoals != null) {
            outState.putParcelableArrayList(GOALS_KEY, mGoals);
        }
        super.onSaveInstanceState(outState);
    }

    private void initGoals() {
        mGoalsAdapter = new GoalsAdapter(mGoals);
        mRecyclerView.setAdapter(mGoalsAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    class GoalsAdapter extends RecyclerView.Adapter<GoalHolder> {

        ArrayList<Goal> mGoals;

        public GoalsAdapter(ArrayList<Goal> goals) {
            mGoals = goals;
        }

        @Override
        public GoalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_list_item, parent, false);
            return new GoalHolder(view);
        }

        @Override
        public void onBindViewHolder(GoalHolder holder, int position) {
            Goal goal = mGoals.get(position);
            holder.goalTextView.setText(goal.getGoal());
            holder.dayOfMonthTextView.setText(goal.getDayOfMonthFromMillis());
            holder.monthTextView.setText(goal.getMonthFromMillis());
            if (goal.getIsCompleted()) {
                holder.statusImageView.setImageResource(R.drawable.ic_check_green_36dp);
            } else {
                holder.statusImageView.setImageResource(R.drawable.ic_cancel_red_36dp);
            }
        }

        @Override
        public int getItemCount() {
            if (mGoals != null) {
                return mGoals.size();
            } else {
                return 0;
            }
        }
    }

    class GoalHolder extends RecyclerView.ViewHolder {
        TextView goalTextView;
        TextView dayOfMonthTextView;
        TextView monthTextView;
        ImageView statusImageView;

        GoalHolder(View itemView) {
            super(itemView);

            goalTextView = (TextView) itemView.findViewById(R.id.li_goal);
            dayOfMonthTextView = (TextView) itemView.findViewById(R.id.li_day_of_month);
            monthTextView = (TextView) itemView.findViewById(R.id.li_month);
            statusImageView = (ImageView) itemView.findViewById(R.id.li_status);
        }
    }

}
