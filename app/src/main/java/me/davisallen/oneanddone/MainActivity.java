package me.davisallen.oneanddone;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import timber.log.Timber;

// TODO: Create setting to change background color.

public class MainActivity extends AppCompatActivity implements
        GoalCreateFragment.DailyGoalCreatedListener,
        GoalViewFragment.OnGoalCompleteListener,
        NavigationView.OnNavigationItemSelectedListener {

    // Params to send data to fragments
    public static final String PARAM_CREATE_GOAL = "create_goal";
    private static final String GOALS_KEY = "goals_key";
    private static final String PREFS_NAME = "preferences";
    private static final String TODAYS_GOAL_KEY = "todays_goal_key";

    // Firebase Analytics instance
    private FirebaseAnalytics mFirebaseAnalytics;
    // Firebase Authorization instance
    private FirebaseAuth mAuth;
    // Firebase Database instance
    private FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mGoalsDbReference;

    private Context mContext;

    // Bind any views with Butterknife
    // Toolbar
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    // Nav drawer
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;

    ImageView mUserImage;
    TextView mUserName;
    TextView mUserEmail;
    TextView mSignOut;

    String mUserId;
    ArrayList<Goal> mGoals;
    Goal mDailyGoal;
    FragmentManager mFragmentManager;
    SharedPreferences mSettings;

    // Choose an arbitrary request code value
    private static final int REQUEST_CODE_SIGN_IN = 1738;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this;

        // Initializes Firebase instances.
        initializeFirebaseTools();

        // Sets toolbar elevation to 0 with state list animator.
        initializeToolbar();

        // Initializes nav drawer layout.
        initializeNavDrawer();

        // Initializes Timber debugger.
        initializeTimber();

        // Initialize Goals
        if (savedInstanceState != null && savedInstanceState.containsKey(GOALS_KEY)) {
            mGoals = savedInstanceState.getParcelableArrayList(GOALS_KEY);
        }

        // Restore preferences
        mSettings = getSharedPreferences(PREFS_NAME, 0);

        initializeMainScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Should I put this in onResume ?
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUi(currentUser);
    }

    private void saveGoalKeyToPreferences(String key) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(TODAYS_GOAL_KEY, key);

        // Commit the edits!
        editor.apply();
    }


    @Override
    public void onBackPressed() {
        if (mDrawer == null) {
            mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        }

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle activity_main_drawer view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_daily_goal) {
            initializeMainScreen();
        } else if (id == R.id.nav_list) {
            openProgressListFragment();
        } else if (id == R.id.nav_calendar) {
            openCalendarFragment();
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO: Save goals to persistent state. always grab goals from the main activity in fragments.
        if (mGoals != null) {
            outState.putParcelableArrayList(GOALS_KEY, mGoals);
        }
        super.onSaveInstanceState(outState);
    }


    private void updateUi(FirebaseUser currentUser) {
        if (currentUser == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .build(),
                    REQUEST_CODE_SIGN_IN);
        } else {
            // TODO: get those views and update them in nav drawer
            // Name, email address, and profile photo Url
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            mUserName.setText(name);
            mUserEmail.setText(email);
//            Picasso.with(this).load(photoUrl).into(mUserImage);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                // TODO: what to do here?
//                startActivity(SignedInActivity.createIntent(this, response));
//                finish();
//                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(R.string.unknown_sign_in_response);
        }
    }

    private void showSnackbar(int stringId) {
        String message = getString(stringId);
        Snackbar snackbar = Snackbar.make(new CoordinatorLayout(this), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void initializeToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(mAppBarLayout, "elevation", 0.1f));
            mAppBarLayout.setStateListAnimator(stateListAnimator);
        }

        mToolbar.setTitle("");
        mToolbar.setSubtitle("");
        setSupportActionBar(mToolbar);
    }

    private void initializeNavDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        LinearLayout navHeaderLayout = (LinearLayout) mNavigationView.getHeaderView(0);
        // TODO: Remove this bc Butterknife!!!
        mUserImage = (ImageView) navHeaderLayout.findViewById(R.id.nav_user_image);
        mUserName = (TextView) navHeaderLayout.findViewById(R.id.nav_user_name);
        mUserEmail = (TextView) navHeaderLayout.findViewById(R.id.nav_user_email);
        mSignOut = (TextView) navHeaderLayout.findViewById(R.id.nav_sign_out);

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUi(mAuth.getCurrentUser());
            }
        });
    }

    private void initializeFirebaseTools() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Obtain the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getUid();
        // Obtain the FirebaseStorage instance.
        mFirebaseDatabase = FirebaseUtils.getDatabase();
        // Obtain DatabaseReference to "goals"
        // TODO: Make this db reference "goals" a project-wide constant.
        mGoalsDbReference = mFirebaseDatabase.getReference("goals");
    }

    private void initializeTimber() {
        // Set up Timber DebugTree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initializeMainScreen() {
        // Get the most recent Goal from the database.
        if (mGoals != null) {
            selectFragmentBasedOnGoal(mGoals.get(mGoals.size()-1));
        } else {
            mGoalsDbReference.orderByChild("userId").equalTo(mUserId).limitToLast(1).
                    addChildEventListener(initializeMainScreenListener);
            getGoalsFromServer();
        }
    }

    private void getGoalsFromServer() {
        if (mGoals == null) {
            mGoals = new ArrayList<>();
            mGoalsDbReference.orderByChild("userId").equalTo(mUserId).addChildEventListener(saveAllGoalsByUserListener);
        }
    }

    private void selectFragmentBasedOnGoal(Goal goal) {
        if (goal != null) {
            long lastGoalCreatedMillis = goal.getDateInMillis();
            if (DateUtils.isToday(lastGoalCreatedMillis)) {
                mDailyGoal = goal;
                openViewGoalFragment(mDailyGoal.getGoal());
            } else {
                openCreateGoalFragment();
            }
        } else {
            Timber.w("Did not get a most recent goal.");
            openCreateGoalFragment();
        }
    }

    private void openProgressListFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        ProgressListFragment progressListFragment = new ProgressListFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, progressListFragment);
        transaction.commit();
    }

    private void openCalendarFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        CalendarFragment calendarFragment = new CalendarFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, calendarFragment);
        transaction.commit();
    }

    private void openCreateGoalFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        GoalCreateFragment goalCreateFragment = new GoalCreateFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, goalCreateFragment);
        transaction.commit();
    }

    @Override
    public void onCreateGoal(String goal) {
        // TODO: Think about making this an asynctask (?)
        Timber.d(String.format("Sending goal '%s' to database.", goal));

        // Write a message to the database
        if (mGoalsDbReference != null && mUserId != null) {
            mDailyGoal = new Goal(goal, mUserId);

            DatabaseReference ref = mGoalsDbReference.push();
            ref.setValue(new Goal(goal, mUserId));
            saveGoalKeyToPreferences(ref.getKey());

            initializeMainScreen();
        } else {
            Timber.e("Could not get reference to goals database.");
        }
    }

    private void openViewGoalFragment(String goal) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        GoalViewFragment goalViewFragment = new GoalViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_CREATE_GOAL, goal);
        goalViewFragment.setArguments(bundle);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, goalViewFragment);
        transaction.commit();

    }

    ChildEventListener initializeMainScreenListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Goal goal = dataSnapshot.getValue(Goal.class);
            selectFragmentBasedOnGoal(goal);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.e("Data download cancelled in intialize main screen.");
        }
    };

    ChildEventListener saveAllGoalsByUserListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Goal goal = dataSnapshot.getValue(Goal.class);
            if (goal != null) {
                mGoals.add(goal);
            } else {
                Timber.e("Got null goal from server :(");
            }
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.e("Data download cancelled in intialize main screen.");
        }
    };

    public void completeGoal(View view) {
        // TODO: Look at camera button for inspiration!
        // TODO: Have the button click update the data in the cloud.

        Animation anim = new ScaleAnimation(
                1f, 1.2f, // Start and end values for the X axis scaling
                1f, 1.2f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(400);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(anim);
    }

    @Override
    public void onGoalCompleted() {
        // TODO: Update the goal!
        mDailyGoal.setIsCompleted(true);
        String goalDbLocation = mSettings.getString(TODAYS_GOAL_KEY, "UNKNOWN") + "/" + "isCompleted";
        mGoalsDbReference.child(goalDbLocation).setValue(true);


    }
}
