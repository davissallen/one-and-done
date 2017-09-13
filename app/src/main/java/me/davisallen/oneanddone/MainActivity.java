package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    // Firebase Analytics instance
    private FirebaseAnalytics mFirebaseAnalytics;
    // Firebase Authorization instance
    private FirebaseAuth mAuth;
    // Firebase Cloud Storage instance
    private StorageReference mStorageRef;

    // Bind views with Butterknife
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigation;

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

        if (mBottomNavigation != null) {
            mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // TODO: Do something with currentUser
        // See FirebaseAssistant tool on auth
        // See getting started info here: https://firebase.google.com/docs/auth/android/start
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Timber.d("Timberrrrr: you just pressed %d", item.getItemId());

            switch (item.getItemId()) {
                case R.id.navigation_list:
                    return true;
                case R.id.navigation_calendar:
                    return true;
            }


            return false;
        }
    };


}
