package me.davisallen.oneanddone;

//region imports
//---------------------------------------------------------------------------------------

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import me.davisallen.oneanddone.utils.FirebaseUtils;
import timber.log.Timber;

import static com.firebase.ui.auth.ui.ExtraConstants.EXTRA_IDP_RESPONSE;
import static me.davisallen.oneanddone.AppWidgetGoalIntentService.ACTION_GET_MOST_RECENT_GOAL;
import static me.davisallen.oneanddone.DailyGoalAppWidget.EXTRA_GOAL;
//---------------------------------------------------------------------------------------
//endregion

// TODO: Create setting to change background color.
// TODO: Add notifications.
// TODO: Hide keyboard when leave main screen if open.
// TODO: Goal streaks
// TODO: Add feature to edit goal once set (but not completed)
// TODO: Start service at every midnight to update UI and widget.
// TODO: Delay banner entrance upon completion.

public class MainActivity extends AppCompatActivity implements
        GoalCreateFragment.DailyGoalCreatedListener,
        GoalViewFragment.OnGoalCompleteListener,
        NavigationView.OnNavigationItemSelectedListener {

    //region Class objects
    //---------------------------------------------------------------------------------------
    // Fragment tags
    public static final String GOAL_VIEW_TAG = "goal_view_tag";
    public static final String GOAL_CREATE_TAG = "goal_create_tag";
    public static final String PROGRESS_LIST_TAG = "progress_list_tag";
    public static final String CALENDAR_TAG = "calendar_tag";

    // Params to send data to fragments
    public static final String PARAM_CREATE_GOAL = "create_goal";
    private static final String SAVE_GOALS_KEY = "save_goals_key";
    public static final int RC_COMPLETE_GOAL_FROM_WIDGET = 55;

    // Firebase class objects
    // TODO: Implement firebase analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser mUser;
    public DatabaseReference mGoalsByUserDbReference;

    // Butterknife view binding
    // Toolbar
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    // Nav drawer
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;

    // Nav drawer views not caught by Butterknife
    ImageView mUserImage;
    TextView mUserName;
    TextView mUserSignInId;
    TextView mSignOut;

    // MainActivity class objects
    ArrayList<Goal> mGoals;
    FragmentManager mFragmentManager;
    MainActivity mActivity;
    Boolean mOpenedFromWidget = false;
    //---------------------------------------------------------------------------------------
    //endregion

    //region createIntent methods for starting MainActivity from another activity
    //---------------------------------------------------------------------------------------
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
    //---------------------------------------------------------------------------------------
    //endregion

    //region Overridden lifecycle methods
    //---------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // TODO: Force the goals to update if it is the next day.
        // Get the goals from the saved instance state if they exist.
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_GOALS_KEY)) {
            mGoals = savedInstanceState.getParcelableArrayList(SAVE_GOALS_KEY);
        }

        // Initializes Firebase instances.
        initializeFirebaseTools();
        // Initializes Timber debugger.
        initializeTimber();
        // Initialize class objects
        mActivity = this;
        // Initialize the Toolbar, NavBar, and Main UI.
        initializeUI();

        // TODO: Move this somewhere else so the alarm doesn't restart every minute lol
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        long oneDayInMillis = (60 * 60 * 24) * 1000;
        long tomorrowInMillis = System.currentTimeMillis() + oneDayInMillis;
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 33);
        calendar.set(Calendar.SECOND, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent alarmIntent = new Intent(this, MainActivity.class);
            PendingIntent alarmPendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_COMPLETE_GOAL_FROM_WIDGET) {
            mOpenedFromWidget = true;
        }
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
            outState.putParcelableArrayList(SAVE_GOALS_KEY, mGoals);
        }
        super.onSaveInstanceState(outState);
    }
    //---------------------------------------------------------------------------------------
    //endregion

    //region Activity initialization methods
    //---------------------------------------------------------------------------------------
    private void initializeFirebaseTools() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Get the user info from FirebaseAuth.
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Timber.e("Did not get user! What!");
        }
        // Obtain the FirebaseStorage instance.
        FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
        mGoalsByUserDbReference = firebaseDatabase.getReference(getString(R.string.goals_db_name)).child(mUser.getUid());
    }

    private void initializeUI() {
        // Sets toolbar elevation to 0 with state list animator.
        initializeToolbar();
        // Initializes nav drawer layout.
        initializeNavDrawer();
        // Initialize main screen.
        initializeMainScreen();
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
        mGoalsByUserDbReference.orderByChild("dateInMillis").addValueEventListener(getAllGoalsByUserValueListener);
    }
    //---------------------------------------------------------------------------------------
    //endregion

    //region Fragment tools
    //---------------------------------------------------------------------------------------
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

        // TODO: Fix bug where device crashes here. Find it!
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
        // TODO: Is this right???
        // https://stackoverflow.com/a/10261438/2457426
        transaction.commitAllowingStateLoss();
    }
    //---------------------------------------------------------------------------------------
    //endregion

    //region Overriden fragment listener methods
    //---------------------------------------------------------------------------------------
    @Override
    public void onCreateGoal(String goal) {
        // Write a message to the database
        if (mGoalsByUserDbReference != null && mUser != null) {
            mGoalsByUserDbReference.push().setValue(new Goal(goal));
            initializeMainScreen();
        } else {
            Timber.e("Could not get reference to goals database or user ID.");
        }
    }

    @Override
    public void onGoalCompleted() {
        // Updates goal in database to completed.
        mGoalsByUserDbReference.orderByChild("dateInMillis").limitToLast(1).addListenerForSingleValueEvent(setMostRecentGoalCompletedValueListener);
    }
    //---------------------------------------------------------------------------------------
    //endregion

    //region Custom goal listeners
    //---------------------------------------------------------------------------------------
    ValueEventListener getAllGoalsByUserValueListener = new ValueEventListener() {
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

            Goal todaysGoal = null;
            if (mGoals.size() > 0) {
                Goal mostRecentGoal = mGoals.get(mGoals.size() - 1);
                if (DateUtils.isToday(mostRecentGoal.getDateInMillis())) {
                    todaysGoal = mostRecentGoal;
                } else {
                    todaysGoal = null;
                }
            }

            Intent widgetServiceIntent = new Intent(mActivity, AppWidgetGoalIntentService.class);
            widgetServiceIntent.setAction(ACTION_GET_MOST_RECENT_GOAL);
            widgetServiceIntent.putExtra(EXTRA_GOAL, todaysGoal);
            startService(widgetServiceIntent);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener setMostRecentGoalCompletedValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get the key of the most recent goal, then use it to update the value.
            String mostRecentGoalKey = dataSnapshot.getChildren().iterator().next().getKey();
            setMostRecentGoalCompleted(mostRecentGoalKey);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };
    //---------------------------------------------------------------------------------------
    //endregion

    //region Miscellaneous methods
    //---------------------------------------------------------------------------------------
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

    public void setMostRecentGoalCompleted(String mostRecentGoalKey) {
        Map<String, Object> goalUpdate = new HashMap<>();
        goalUpdate.put(mostRecentGoalKey + "/" + "isCompleted", true);
        mGoalsByUserDbReference.updateChildren(goalUpdate);
    }
    //---------------------------------------------------------------------------------------
    //endregion

}
