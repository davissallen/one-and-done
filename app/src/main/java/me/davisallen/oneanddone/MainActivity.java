package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements GoalCreateFragment.DailyGoalCreatedListener {

    // Params to send data to fragments
    public static final String PARAM_CREATE_GOAL = "create_goal";

    // Firebase Analytics instance
    private FirebaseAnalytics mFirebaseAnalytics;
    // Firebase Authorization instance
    private FirebaseAuth mAuth;
    // Firebase Cloud Storage instance
    private StorageReference mStorageRef;

    // Bind any views with Butterknife

    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Obtain the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        // Obtain the FirebaseStorage instance.
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Set up Timber DebugTree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        openCreateGoalFragment();

        // TODO: open up createGoalFragment if there is no goal, or mainViewFragment if it already exists
        // maybe have a splash screen to hold the place while the lookup is done...
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // TODO: Do something with currentUser
        // See FirebaseAssistant tool on auth
        // See getting started info here: https://firebase.goo gle.com/docs/auth/android/start
    }


    @Override
    public void onCreateGoal(String goal) {
        openViewGoalFragment(goal);
    }

    private void openCreateGoalFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        GoalCreateFragment goalCreateFragment =  new GoalCreateFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, goalCreateFragment);
        transaction.commit();
    }

    private void openViewGoalFragment(String goal) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        GoalViewFragment goalViewFragment =  new GoalViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_CREATE_GOAL, goal);
        goalViewFragment.setArguments(bundle);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, goalViewFragment);
        transaction.commit();
    }
}
