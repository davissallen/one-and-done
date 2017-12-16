package me.davisallen.oneanddone;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import timber.log.Timber;

import static me.davisallen.oneanddone.utils.SnackbarUtils.showSnackbar;

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
    public DatabaseReference mGoalsByUserDbReference;

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

        // Get the goals from the saved instance state if they exist.
        if (savedInstanceState != null && savedInstanceState.containsKey(GOALS_KEY)) {
            mGoals = savedInstanceState.getParcelableArrayList(GOALS_KEY);
        }

        // Initializes Firebase instances.
        initializeFirebaseTools();
        // Initializes Timber debugger.
        initializeTimber();
        // Initialize the Toolbar, NavBar, and Main UI.
        initializeUI();

        // Get reference the shared preferences
        mSettings = getSharedPreferences(PREFS_NAME, 0);
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
        mGoalsByUserDbReference = mFirebaseDatabase.getReference(
                getString(R.string.goals_db_name)).child(mUserId);
    }

    private void initializeUI() {
        // Sets toolbar elevation to 0 with state list animator.
        initializeToolbar();
        // Initializes nav drawer layout.
        initializeNavDrawer();
        // Initialize main screen.
        initializeMainScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: Should I put this in onResume ?
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUiForUserAuth(currentUser);
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
            ProgressListFragment progressListFragment = new ProgressListFragment();
            openFragment(progressListFragment);
        } else if (id == R.id.nav_calendar) {
            CalendarFragment calendarFragment = new CalendarFragment();
            openFragment(calendarFragment);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mGoals != null) {
            outState.putParcelableArrayList(GOALS_KEY, mGoals);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateUiForUserAuth(FirebaseUser currentUser) {
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
                    showSnackbar(this, R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(this, R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(this, R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(this, R.string.unknown_sign_in_response);
        }
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
        mUserImage = (ImageView) navHeaderLayout.findViewById(R.id.nav_user_image);
        mUserName = (TextView) navHeaderLayout.findViewById(R.id.nav_user_name);
        mUserEmail = (TextView) navHeaderLayout.findViewById(R.id.nav_user_email);
        mSignOut = (TextView) navHeaderLayout.findViewById(R.id.nav_sign_out);

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUiForUserAuth(mAuth.getCurrentUser());
            }
        });
    }

    private void initializeTimber() {
        // Set up Timber DebugTree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initializeMainScreen() {
        if (mGoals != null) {
            selectFragmentBasedOnGoal();
        } else {
            mGoalsByUserDbReference.orderByChild("dateInMillis").addListenerForSingleValueEvent(getAllGoalsByUserListener);
        }
    }

    private void selectFragmentBasedOnGoal() {

        if (mGoals != null && mGoals.size() > 0) {

            Goal mostRecentGoal = mGoals.get(mGoals.size() - 1);
            long lastGoalCreatedMillis = mostRecentGoal.getDateInMillis();

            if (DateUtils.isToday(lastGoalCreatedMillis)) {

                GoalViewFragment goalViewFragment = new GoalViewFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(PARAM_CREATE_GOAL, mostRecentGoal);
                goalViewFragment.setArguments(bundle);
                openFragment(goalViewFragment);

            } else {

                GoalCreateFragment goalCreateFragment = new GoalCreateFragment();
                openFragment(goalCreateFragment);

            }

        } else {

            Timber.w("Did not get a most recent goal.");
            GoalCreateFragment goalCreateFragment = new GoalCreateFragment();
            openFragment(goalCreateFragment);

        }
    }

    private void openFragment(Fragment fragment) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onCreateGoal(String goal) {
        // This is called when a goal is created by the user in GoalCreateFragment.
        Timber.d(String.format("Sending goal '%s' to database.", goal));

        // Write a message to the database
        if (mGoalsByUserDbReference != null && mUserId != null) {
            mGoalsByUserDbReference.push().setValue(new Goal(goal));
            initializeMainScreen();
        } else {
            Timber.e("Could not get reference to goals database or user ID.");
        }
    }

//    ChildEventListener getAllGoalsByUserListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            Goal goal = dataSnapshot.getValue(Goal.class);
//            if (goal != null) {
//                mGoals.add(goal);
//            } else {
//                Timber.e("Got null goal from server :(");
//            }
//            selectFragmentBasedOnGoal();
//        }
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//        }
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//        }
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Timber.e("Data download cancelled in intialize main screen.");
//        }
//    };

    ValueEventListener getAllGoalsByUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mGoals = new ArrayList<>();
            // TODO: See if the behavior is different for one goal vs many goals bc getChildren()
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                try {
                    Goal goal = snapshot.getValue(Goal.class);
                    mGoals.add(goal);
                } catch (DatabaseException e) {
                    Timber.e("Data could not be converted to Goal!");
                    Timber.d(snapshot.toString());
                }
            }
            selectFragmentBasedOnGoal();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener getMostRecentGoalByUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getChildren().iterator().next().getKey();
            setMostRecentGoalCompleted(key);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void setMostRecentGoalCompleted(String key) {
        Map<String, Object> goalUpdate = new HashMap<>();
        goalUpdate.put(key + "/" + "isCompleted", true);
        mGoalsByUserDbReference.updateChildren(goalUpdate);
    }

    @Override
    public void onGoalCompleted() {
        // TODO: Update the goal in the cloud!

        mGoalsByUserDbReference.orderByChild("dateInMillis").limitToLast(1).addListenerForSingleValueEvent(getMostRecentGoalByUserListener);

        // Update value in cloud to completed.
        Timber.e("Goal is doing nothing");
        System.out.println();
    }
}
