package me.davisallen.oneanddone;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import timber.log.Timber;

import static me.davisallen.oneanddone.MainActivity.PARAM_CREATE_GOAL;
import static me.davisallen.oneanddone.utils.ToastUtils.showToast;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/17/17
 */

public class GoalViewFragment extends Fragment {

    private Goal mGoal;

    @BindView(R.id.goal_view_container)
    ConstraintLayout mContainer;
    @BindView(R.id.konfetti)
    KonfettiView mKonfetti;
    @BindView(R.id.clock_goal_view)
    TextClock mTextClock;
    @BindView(R.id.banner)
    FrameLayout mBanner;
    @BindView(R.id.tv_goal_view_date)
    TextView mDateTextView;
    @BindView(R.id.tv_goal_view_goal)
    TextView mGoalTextView;
    @BindView(R.id.pulsator)
    PulsatorLayout mPulsator;
    @BindView(R.id.button_complete)
    ImageView mCompleteButton;
    @BindView(R.id.iv_edit_goal)
    ImageView mEditGoalImageView;

    Context mContext;
    OnGoalCompleteListener mListener;
    AppCompatActivity mActivity;

    interface OnGoalCompleteListener {
        public void onGoalCompleted();

        public void onGoalEdited();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mActivity = (AppCompatActivity) getActivity();
        mListener = (OnGoalCompleteListener) getActivity();

        Bundle receivedArgs = getArguments();
        if (receivedArgs != null) {
            if (receivedArgs.containsKey(PARAM_CREATE_GOAL)) {
                mGoal = receivedArgs.getParcelable(PARAM_CREATE_GOAL);
            }
        } else {
            Timber.e("Did not receive goal, oh no! :O");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_view, container, false);
        ButterKnife.bind(this, view);

        if (mGoal.getIsCompleted()) {
            updateUIForCompletedGoal();
        } else {
            mGoalTextView.setPaintFlags(0);
//            mCompleteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_primary));
            mCompleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getResources().getString(R.string.confirm_completed_title));
                    builder.setMessage(getResources().getString(R.string.confirm_completed_message));
                    builder.setIcon(R.drawable.logo);
                    String positiveResponse = getResources().getString(R.string.confirm_completed_positive);
                    String negativeResponse = getResources().getString(R.string.confirm_completed_negative);
                    builder.setPositiveButton(positiveResponse, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            startAnimationToCompleteGoal(view);
                        }
                    });
                    builder.setNegativeButton(negativeResponse, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });
            mBanner.setVisibility(View.INVISIBLE);
            mPulsator.start();
        }

        mEditGoalImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle(getResources().getString(R.string.confirm_edit_goal_title));
//                builder.setMessage(getResources().getString(R.string.configm_edit_goal_prompt));
                builder.setIcon(R.drawable.logo);
                String positiveResponse = getResources().getString(R.string.confirm_edit_goal_positive);
                String negativeResponse = getResources().getString(R.string.confirm_edit_goal_negative);
                builder.setPositiveButton(positiveResponse, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        // Delete goal in database.
                        mListener.onGoalEdited();
                    }
                });
                builder.setNegativeButton(negativeResponse, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return view;
    }

    public void celebrate() {
        // Rain down the skies with confetti!
        mKonfetti.build()
                // pink, blue, yellow, orange, green, red, white
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.WHITE, Color.RED)
                .setDirection(120f, 60f)
                .setSpeed(1f, 6f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .setPosition(0f, mKonfetti.getWidth() / 1f, 0f, 0f)
                .stream(40, 5000L);
    }

    public void startAnimationToCompleteGoal(View view) {

        final AnimatedVectorDrawableCompat animatedVector = AnimatedVectorDrawableCompat.create(mContext, R.drawable.avd_anim);
        mCompleteButton.setImageDrawable(animatedVector);
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        if (animatedVector != null) {
            animatedVector.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(final Drawable drawable) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateUIForCompletedGoal();
                            celebrate();
                            // Update the goal in the cloud!
                            setGoalCompleted();
                        }
                    });
                }
            });
        }
        if (animatedVector != null) {
            animatedVector.start();
        }
    }

    private void updateUIForCompletedGoal() {

        mContainer.setBackground(getResources().getDrawable(R.drawable.blue_gradient));
        ActionBar toolbar = mActivity.getSupportActionBar();
        if (toolbar != null) {
            toolbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        mGoalTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        mCompleteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_gray));
        mCompleteButton.setClickable(false);
        mCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast(getActivity(), getRandomCongratulationsString());
            }
        });

        // Stop and hide the pulsing.
        mPulsator.setVisibility(View.INVISIBLE);
        mPulsator.stop();

        mBanner.setVisibility(View.VISIBLE);
    }

    public void setGoalCompleted() {
        mListener.onGoalCompleted();
    }

    private String getRandomCongratulationsString() {
        String[] array = getActivity().getResources().getStringArray(R.array.congrats_array);
        return array[new Random().nextInt(array.length)];
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the views with appropriate data
        setDate();
        mGoalTextView.setText(mGoal.getGoal());
    }

    private void setDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
        mDateTextView.setText(sdf.format(new Date()));
    }
}
