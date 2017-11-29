package me.davisallen.oneanddone;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        GoalCreateFragment.DailyGoalCreatedListener,
        NavigationView.OnNavigationItemSelectedListener {

    // Params to send data to fragments
    public static final String PARAM_CREATE_GOAL = "create_goal";

    // Firebase Analytics instance
    private FirebaseAnalytics mFirebaseAnalytics;
    // Firebase Authorization instance
    private FirebaseAuth mAuth;
    // Firebase Database instance
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGoalsDbReference;

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
    String mGoalForToday;

    FragmentManager mFragmentManager;

    // Choose an arbitrary request code value
    private static final int REQUEST_CODE_SIGN_IN = 1738;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this;

        // Sets toolbar elevation to 0 with state list animator.
        initializeToolbar();

        // Initializes nav drawer layout.
        initializeNavDrawer();

        // Initializes Firebase instances.
        initializeFirebaseTools();

        // Initializes Timber debugger.
        initializeTimber();

        // Initializes the main screen.
        // If a goal was already set today, open up the GoalViewFragment.
        // Else, open the GoalCreateFragment.
        initializeMainScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUi(currentUser);
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
            openViewGoalFragment();
        } else if (id == R.id.nav_list) {
            openProgressListFragment();
        } else if (id == R.id.nav_calendar) {
            openCalendarFragment();
        }

        if (mDrawer == null) {
            mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
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
        // TODO: Check server to see if goal exists for today.
        // If so, open viewgoal, else, open creategoal
        boolean goalCreatedToday = false;
        if (goalCreatedToday) {
            openViewGoalFragment();
        } else {
            openCreateGoalFragment();
        }
    }

    private void openProgressListFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        ProgressListFragment progressListFragment =  new ProgressListFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, progressListFragment);
        transaction.commit();
    }

    private void openCalendarFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        CalendarFragment calendarFragment =  new CalendarFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, calendarFragment);
        transaction.commit();
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

    @Override
    public void onCreateGoal(String goal) {
        if (goalNotSetToday()) {
            saveGoalToCloud(goal);
        }
        openViewGoalFragment();
    }

    private boolean goalNotSetToday() {
        // TODO: Query db to see if goal was set today
        return true;
    }

    private void saveGoalToCloud(String goal) {
        // TODO: Think about making this an asynctask
        Timber.d(String.format("Sending goal '%s' to database.", goal));

        // Write a message to the database
        if (mGoalsDbReference != null && mUserId != null) {
            mGoalsDbReference.push().setValue(new Goal(goal, mUserId));
        } else {
            Timber.e("Could not get reference to goals database.");
        }
    }

    private void openViewGoalFragment() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }

        if (mUserId != null) {
            mUserId = mAuth.getUid();
        }

        // TODO: fix this...

        mGoalsDbReference.orderByChild("date").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Goal goal = snapshot.getValue(Goal.class);
                    if (goal != null) {
                        Timber.d(goal.getGoal());
                        GoalViewFragment goalViewFragment =  new GoalViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(PARAM_CREATE_GOAL, goal.getGoal());
                        goalViewFragment.setArguments(bundle);

                        FragmentTransaction transaction = mFragmentManager.beginTransaction();
                        transaction.replace(R.id.main_fragment_container, goalViewFragment);
                        transaction.commit();
                    } else {
                        GoalViewFragment goalViewFragment =  new GoalViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(PARAM_CREATE_GOAL, "No goal found :(");
                        goalViewFragment.setArguments(bundle);

                        FragmentTransaction transaction = mFragmentManager.beginTransaction();
                        transaction.replace(R.id.main_fragment_container, goalViewFragment);
                        transaction.commit();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
