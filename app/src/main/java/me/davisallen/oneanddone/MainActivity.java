package me.davisallen.oneanddone;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import timber.log.Timber;

import static com.firebase.ui.auth.ui.ExtraConstants.EXTRA_IDP_RESPONSE;

// TODO: Create setting to change background color.
// TODO: Add notifications.
// TODO: Hide keyboard when leave main screen if open.

public class MainActivity extends AppCompatActivity implements
        GoalCreateFragment.DailyGoalCreatedListener,
        GoalViewFragment.OnGoalCompleteListener,
        NavigationView.OnNavigationItemSelectedListener {

    // Fragment tags
    public static final String GOAL_VIEW_TAG = "goal_view_tag";
    public static final String GOAL_CREATE_TAG = "goal_create_tag";
    public static final String PROGRESS_LIST_TAG = "progress_list_tag";
    public static final String CALENDAR_TAG = "calendar_tag";

    // Params to send data to fragments
    public static final String PARAM_CREATE_GOAL = "create_goal";
    public static final String PARAM_GOAL_JUST_COMPLETED = "goal_just_completed";
    private static final String GOALS_KEY = "goals_key";
    private static final String PREFS_NAME = "preferences";
    private static final String TODAYS_GOAL_KEY = "todays_goal_key";

    // Firebase Analytics instance
    private FirebaseAnalytics mFirebaseAnalytics;
    // Firebase Authorization instance
    private FirebaseUser mUser;
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
    TextView mUserSignInId;
    TextView mSignOut;

    ArrayList<Goal> mGoals;
    FragmentManager mFragmentManager;
    SharedPreferences mSettings;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent();
        return intent.setClass(context, MainActivity.class);
    }

    public static Intent createIntent(Context context, IdpResponse idpResponse) {

        Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }

        return startIntent.setClass(context, MainActivity.class);
    }

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

        // Get reference the shared preferences.
        mSettings = getSharedPreferences(PREFS_NAME, 0);
    }

    private void initializeFirebaseTools() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Get the user info from FirebaseAuth.
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Timber.e("Did not get user! What!");
        }
        // Obtain the FirebaseStorage instance.
        mFirebaseDatabase = FirebaseUtils.getDatabase();
        mGoalsByUserDbReference = mFirebaseDatabase.getReference(getString(R.string.goals_db_name)).child(mUser.getUid());
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
            openFragment(progressListFragment, PROGRESS_LIST_TAG);
        } else if (id == R.id.nav_calendar) {
            CalendarFragment calendarFragment = new CalendarFragment();
            openFragment(calendarFragment, CALENDAR_TAG);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // https://stackoverflow.com/a/10261438/2457426
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        if (mGoals != null) {
            outState.putParcelableArrayList(GOALS_KEY, mGoals);
        }
        super.onSaveInstanceState(outState);
    }

    public void onSignOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Timber.d("Successfully signed out!");
                    }
                });
        startActivity(SignInActivity.createIntent(this));
        finish();
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
        mUserSignInId = (TextView) navHeaderLayout.findViewById(R.id.nav_user_sign_in_id);
        mSignOut = (TextView) navHeaderLayout.findViewById(R.id.nav_sign_out);

        // Set user image
        if (mUser.getPhotoUrl() != null) {
            Picasso.with(this).load(mUser.getPhotoUrl()).into(mUserImage);
        }
        // Set user name
        mUserName.setText(mUser.getDisplayName());
        // Set user sign in ID (phone number or email)
        if (mUser.getEmail() != null) {
            mUserSignInId.setText(mUser.getEmail());
        } else if (mUser.getPhoneNumber() != null) {
            mUserSignInId.setText(mUser.getPhoneNumber());
        }

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignOut(view);
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
        if (mGoals != null && mGoals.size() != 0) {
            selectFragmentBasedOnGoal();
        } else {
            mGoalsByUserDbReference.orderByChild("dateInMillis").addValueEventListener(getAllGoalsByUserListener);
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
                openFragment(goalViewFragment, GOAL_VIEW_TAG);

            } else {

                GoalCreateFragment goalCreateFragment = new GoalCreateFragment();
                openFragment(goalCreateFragment, GOAL_CREATE_TAG);

            }

        } else {

            Timber.w("Did not get a most recent goal.");
            GoalCreateFragment goalCreateFragment = new GoalCreateFragment();
            openFragment(goalCreateFragment, GOAL_CREATE_TAG);

        }
    }

    private void openFragment(Fragment fragment, String tag) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        switch(tag) {
            case GOAL_CREATE_TAG:
                GoalCreateFragment goalCreateFragment = (GoalCreateFragment) mFragmentManager.findFragmentByTag(tag);
                if (goalCreateFragment != null && goalCreateFragment.isVisible()) {
                    return;
                }
            case GOAL_VIEW_TAG:
                GoalViewFragment goalViewFragment = (GoalViewFragment) mFragmentManager.findFragmentByTag(tag);
                if (goalViewFragment != null && goalViewFragment.isVisible()) {
                    return;
                }
            case PROGRESS_LIST_TAG:
                ProgressListFragment progressListFragment = (ProgressListFragment) mFragmentManager.findFragmentByTag(tag);
                if (progressListFragment != null && progressListFragment.isVisible()) {
                    return;
                }
            case CALENDAR_TAG:
                CalendarFragment calendarFragment = (CalendarFragment) mFragmentManager.findFragmentByTag(tag);
                if (calendarFragment != null && calendarFragment.isVisible()) {
                    return;
                }
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment, tag);
        // https://stackoverflow.com/a/10261438/2457426
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onCreateGoal(String goal) {
        // This is called when a goal is created by the user in GoalCreateFragment.
        Timber.d(String.format("Sending goal '%s' to database.", goal));

        // Write a message to the database
        if (mGoalsByUserDbReference != null && mUser != null) {
            mGoalsByUserDbReference.push().setValue(new Goal(goal));
            initializeMainScreen();
        } else {
            Timber.e("Could not get reference to goals database or user ID.");
        }
    }

    ValueEventListener getAllGoalsByUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mGoals = new ArrayList<>();
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
        mGoalsByUserDbReference.orderByChild("dateInMillis").limitToLast(1).addListenerForSingleValueEvent(getMostRecentGoalByUserListener);
    }
}
