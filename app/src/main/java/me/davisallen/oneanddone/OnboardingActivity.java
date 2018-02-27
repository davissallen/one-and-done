package me.davisallen.oneanddone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OnboardingActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Button mGetStartedButton;

    private int[] mOnboardingFragmentLayoutIds = {
            R.layout.onboarding_layout_1,
            R.layout.onboarding_layout_2,
            R.layout.onboarding_layout_3,
            R.layout.onboarding_layout_4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.onboarding_container_vp);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.onboarding_dots);
        tabLayout.setupWithViewPager(mViewPager, true);

        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });
    }

    private void finishOnboarding() {
        // Write false to first launch shared preferences.
        SharedPreferences sharedPreferences = OnboardingActivity.this.getSharedPreferences(getString(R.string.file_shared_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_prefs_first_launch), false);
        editor.apply();

        // Finish this activity
        Intent signInIntent = new Intent(OnboardingActivity.this, SignInActivity.class);
        signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signInIntent);
        ActivityCompat.finishAffinity(OnboardingActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_onboarding, menu);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OnboardingFragment extends Fragment {

        private static final String ONBOARDING_LAYOUT_ID = "onboarding_layout_id";

        public OnboardingFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static OnboardingFragment newInstance(int layoutId) {
            OnboardingFragment fragment = new OnboardingFragment();
            Bundle args = new Bundle();
            args.putInt(ONBOARDING_LAYOUT_ID, layoutId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int layoutId;

            Bundle receivedArgs = getArguments();
            if (receivedArgs != null) {
                layoutId = getArguments().getInt(ONBOARDING_LAYOUT_ID);
            } else {
                // TODO: Make it the layout ID of the last screen so it can skip and continue to main app.
                layoutId = -1;
            }

            View rootView = inflater.inflate(layoutId, container, false);
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return OnboardingFragment.newInstance(mOnboardingFragmentLayoutIds[position]);
        }

        @Override
        public int getCount() {
            return mOnboardingFragmentLayoutIds.length;
        }
    }
}
