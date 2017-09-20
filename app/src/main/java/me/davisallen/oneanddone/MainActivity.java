package me.davisallen.oneanddone;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Intent;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    // Firebase Cloud Storage instance
    private StorageReference mStorageRef;

    // Bind any views with Butterknife
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;

    FragmentManager mFragmentManager;

    // Choose an arbitrary request code value
    private static final int REQUEST_CODE_SIGN_IN = 1738;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

        // Sets toolbar elevation to 0 with state list animator
        setUpToolbar();
        setSupportActionBar(mToolbar);

        initializeNavigationMenu();

        openCreateGoalFragment();

        // TODO: open up createGoalFragment if there is no goal, or mainViewFragment if it already exists
        // maybe have a splash screen to hold the place while the lookup is done...
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUi(currentUser);
        // See FirebaseAssistant tool on auth
        // See getting started info here: https://firebase.goo gle.com/docs/auth/android/start
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle activity_main_drawer view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_daily_goal) {
            openViewGoalFragment("Bake cookies");
        } else if (id == R.id.nav_list) {
            openProgressListFragment();
        } else if (id == R.id.nav_calendar) {
            openCalendarFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(mAppBarLayout, "elevation", 0.1f));
            mAppBarLayout.setStateListAnimator(stateListAnimator);
        }
        mToolbar.setTitle("");
        mToolbar.setSubtitle("");
    }

    private void updateUi(FirebaseUser currentUser) {
        if (currentUser == null) {
            // TODO: use smart lock for production
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .build(),
                    REQUEST_CODE_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
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

    private void initializeNavigationMenu() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        openViewGoalFragment(goal);
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
