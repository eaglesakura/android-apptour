package com.vlonjatg.android.apptourlibrary;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Vlonjat Gashi (vlonjatg)
 */
public class AppTourDelegate {

    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private final ArrayList<Integer> mColors = new ArrayList<>();
    private final List<Fragment> mFragments = new Vector<>();
    private LockableViewPager mIntroViewPager;
    private RelativeLayout mControlsRelativeLayout;
    private Button mSkipIntroButton;
    private Button mDoneSlideButton;
    private ImageButton mNextSlideImageButton;
    private View mSeparatorView;
    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private PagerAdapter mPagerAdapter;
    private int mCurrentPosition;
    private int mActiveDotColor;
    private int mInactiveDocsColor;
    private int mNumberOfSlides;
    private boolean mSkipForceHidden;
    private boolean mNextForceHidden;
    private boolean mDoneForceHidden;

    @NonNull
    private AppTourCompat mCompat;

    @NonNull
    private View mRootView;

    /**
     * Immersive加工をする前のシステムUIフラグ
     */
    private int mOldSystemUiVisiblity;

    public AppTourDelegate(@NonNull AppTourCompat compat) {
        mCompat = compat;
    }

    /**
     * 互換性を保つ
     */
    public interface AppTourCompat {
        @NonNull
        Activity getActivity(@NonNull AppTourDelegate self);

        @NonNull
        FragmentManager getFragmentManager(@NonNull AppTourDelegate self);

        @NonNull
        LayoutInflater getLayoutInflater(@NonNull AppTourDelegate self);

        /**
         * 初期化を行わせる
         */
        void onTourInitialize(@NonNull AppTourDelegate self, @Nullable Bundle savedInstanceState);

        /**
         * click "Skip"
         */
        void onClickTourSkip(@NonNull AppTourDelegate self);

        /**
         * click "Done"
         */
        void onClickTourDone(@NonNull AppTourDelegate self);
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        mIntroViewPager = (LockableViewPager) mRootView.findViewById(R.id.AppTour_ViewPager);
        mControlsRelativeLayout = (RelativeLayout) mRootView.findViewById(R.id.controlsRelativeLayout);
        mSkipIntroButton = (Button) mRootView.findViewById(R.id.AppTour_Nav_SkipIntro);
        mNextSlideImageButton = (ImageButton) mRootView.findViewById(R.id.AppTour_Nav_NextSlide);
        mDoneSlideButton = (Button) mRootView.findViewById(R.id.AppTour_Nav_Done);
        mSeparatorView = mRootView.findViewById(R.id.AppTour_Separator);
        mDotsLayout = (LinearLayout) mRootView.findViewById(R.id.AppTour_Nav_Dots);

        mActiveDotColor = Color.RED;
        mInactiveDocsColor = Color.WHITE;

        //Instantiate the PagerAdapter.
        mPagerAdapter = new PagerAdapter(mCompat.getFragmentManager(this), mFragments);
        mIntroViewPager.setAdapter(mPagerAdapter);

        mCompat.onTourInitialize(this, savedInstanceState);

        mNumberOfSlides = mFragments.size();

        //Instantiate the indicator mDots if there are more than one slide
        if (mNumberOfSlides > 1) {
            setViewPagerDots();

            if (!mSkipForceHidden) {
                mSkipIntroButton.setVisibility(View.VISIBLE);
            }
        } else {
            mSkipIntroButton.setVisibility(View.INVISIBLE);
            mNextSlideImageButton.setVisibility(View.INVISIBLE);

            if (!mDoneForceHidden) {
                mDoneSlideButton.setVisibility(View.VISIBLE);
            }
        }

        setListeners();
    }

    /**
     * Immersiveモード起動を行う場合はtrue
     */
    public void setImmersive(boolean immersive) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return;
        }

        //Set status bar to semi-transparent
        View decorView = mCompat.getActivity(this).getWindow().getDecorView();
        if (immersive) {
            mOldSystemUiVisiblity = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else {
            decorView.setSystemUiVisibility(mOldSystemUiVisiblity);
        }
    }

    /**
     * ユーザーのスワイプ操作を行えなくする場合はtrue
     *
     * default = true
     */
    public void setSwipeLock(boolean lock) {
        mIntroViewPager.setLocked(lock);
    }

    /**
     * 戻るボタンが押されたら呼び出す。
     *
     * @return ハンドリングを行ったらtrue
     */
    public boolean onBackPressed() {
        int item = mIntroViewPager.getCurrentItem();
        if (item == 0) {
            return false;
        } else {
            mIntroViewPager.setCurrentItem(item - 1);
            return true;
        }
    }

    /**
     * Viewを生成する
     */
    @NonNull
    public View onCreateView() {
        mRootView = mCompat.getLayoutInflater(this).inflate(R.layout.activity_app_tour, null);
        return mRootView;
    }

    /**
     * Add a slide to the intro
     *
     * @param fragment Fragment of the slide to be added
     */
    public void addSlide(@NonNull Fragment fragment) {
        mFragments.add(fragment);
        addBackgroundColor(Color.TRANSPARENT);
        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Add a slide to the intro
     *
     * @param fragment Fragment of the slide to be added
     * @param color    Background color of the fragment
     */
    public void addSlide(@NonNull Fragment fragment, @ColorInt int color) {
        mFragments.add(fragment);
        addBackgroundColor(color);
        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Return slides
     *
     * @return Return slides
     */
    public List<Fragment> getSlides() {
        return mPagerAdapter.getFragments();
    }

    /**
     * Get which slide is currently active
     *
     * @return Returns the current active slide index
     */
    public int getCurrentSlide() {
        return mIntroViewPager.getCurrentItem();
    }

    /**
     * Set the currently selected slide
     *
     * @param position Item index to select
     */
    public void setCurrentSlide(int position) {
        mIntroViewPager.setCurrentItem(position, true);
    }

    /**
     * Set the string value of the skip button
     *
     * @param text String value to set
     */
    public void setSkipText(@NonNull String text) {
        mSkipIntroButton.setText(text);
    }

    /**
     * Set the string value of the done button
     *
     * @param text String value to set
     */
    public void setDoneText(@NonNull String text) {
        mDoneSlideButton.setText(text);
    }

    /**
     * Set the text color of the skip button
     *
     * @param color Color value to set
     */
    public void setSkipButtonTextColor(@ColorInt int color) {
        mSkipIntroButton.setTextColor(color);
    }

    /**
     * Set the next button color to white
     */
    public void setNextButtonColorToWhite() {
        mNextSlideImageButton.setImageResource(R.drawable.ic_next_white_24dp);
    }

    /**
     * Set the next button color to black
     */
    public void setNextButtonColorToBlack() {
        mNextSlideImageButton.setImageResource(R.drawable.ic_next_black_24dp);
    }

    /**
     * Set the text color of the done button
     *
     * @param color Color value to set
     */
    public void setDoneButtonTextColor(@ColorInt int color) {
        mDoneSlideButton.setTextColor(color);
    }

    /**
     * Set the color of the separator between slide content and bottom controls
     *
     * @param color Color value to set
     */
    public void setSeparatorColor(@ColorInt int color) {
        mSeparatorView.setBackgroundColor(color);
    }

    /**
     * Set the color of the active dot indicator
     *
     * @param color Color value to set
     */
    public void setActiveDotColor(@ColorInt int color) {
        mActiveDotColor = color;
    }

    /**
     * Set the color of the inactive dot indicator
     *
     * @param color Color value to set
     */
    public void setInactiveDocsColor(@ColorInt int color) {
        mInactiveDocsColor = color;
    }

    /**
     * Show the skip button
     */
    public void showSkip() {
        mSkipIntroButton.setVisibility(View.VISIBLE);
        mSkipForceHidden = false;
    }

    /**
     * Hide the skip button
     */
    public void hideSkip() {
        mSkipIntroButton.setVisibility(View.INVISIBLE);
        mSkipForceHidden = true;
    }

    /**
     * Show the next button
     */
    public void showNext() {
        mNextSlideImageButton.setVisibility(View.VISIBLE);
        mNextForceHidden = false;
    }

    /**
     * Hide the next button
     */
    public void hideNext() {
        mNextSlideImageButton.setVisibility(View.INVISIBLE);
        mNextForceHidden = true;
    }

    /**
     * Show the done button
     */
    public void showDone() {
        mDoneSlideButton.setVisibility(View.VISIBLE);
        mDoneForceHidden = false;
    }

    /**
     * Hide the done button
     */
    public void hideDone() {
        mDoneSlideButton.setVisibility(View.INVISIBLE);
        mDoneForceHidden = true;
    }

    /**
     * Show indicator mDots
     */
    public void showIndicatorDots() {
        mDotsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hide indicator mDots
     */
    public void hideIndicatorDots() {
        mDotsLayout.setVisibility(View.INVISIBLE);
    }

    private void addBackgroundColor(@ColorInt int color) {
        mColors.add(color);
    }

    private void setListeners() {
        mIntroViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (mPagerAdapter.getCount() - 1) && position < (mColors.size() - 1)) {
                    int color = (Integer)
                            mArgbEvaluator.evaluate(positionOffset, mColors.get(position), mColors.get(position + 1));
                    mIntroViewPager.setBackgroundColor(color);
                    mControlsRelativeLayout.setBackgroundColor(color);
                } else {
                    int color = mColors.get(mColors.size() - 1);
                    mIntroViewPager.setBackgroundColor(color);
                    mControlsRelativeLayout.setBackgroundColor(color);
                }
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;

                //Hide SKIP button if last slide item, visible if not
                if (position == mNumberOfSlides - 1) {
                    if (!mSkipForceHidden) {
                        fadeViewOut(mSkipIntroButton);
                    }
                } else {
                    if (mSkipIntroButton.getVisibility() == View.INVISIBLE && !mSkipForceHidden) {
                        fadeViewIn(mSkipIntroButton);
                    }
                }

                //Hide NEXT button if last slide item and set DONE button
                //visible, otherwise hide Done button and set NEXT button visible
                if (position == mNumberOfSlides - 1) {
                    if (!mNextForceHidden) {
                        fadeViewOut(mNextSlideImageButton);
                    }

                    if (!mDoneForceHidden) {
                        fadeViewIn(mDoneSlideButton);
                    }
                } else {
                    if (mNextSlideImageButton.getVisibility() == View.INVISIBLE && !mNextForceHidden) {
                        fadeViewIn(mNextSlideImageButton);
                    }

                    if (mDoneSlideButton.getVisibility() == View.VISIBLE && !mDoneForceHidden) {
                        fadeViewOut(mDoneSlideButton);
                    }
                }

                //Set mDots
                if (mNumberOfSlides > 1) {
                    //Set current inactive mDots color
                    for (int i = 0; i < mNumberOfSlides; i++) {
                        mDots[i].setTextColor(mInactiveDocsColor);
                    }

                    //Set current active dot color
                    mDots[position].setTextColor(mActiveDotColor);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mSkipIntroButton.setOnClickListener((it) -> mCompat.onClickTourSkip(this));
        mNextSlideImageButton.setOnClickListener((it) -> mIntroViewPager.setCurrentItem(mCurrentPosition + 1, true));
        mDoneSlideButton.setOnClickListener((it) -> mCompat.onClickTourDone(this));
    }

    private void setViewPagerDots() {
        mDots = new TextView[mNumberOfSlides];

        //Set first inactive mDots color
        for (int i = 0; i < mNumberOfSlides; i++) {
            mDots[i] = new TextView(mCompat.getActivity(this));
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(30);
            mDots[i].setTextColor(mInactiveDocsColor);
            mDotsLayout.addView(mDots[i]);
        }

        //Set first active dot color
        mDots[0].setTextColor(mActiveDotColor);
    }

    private void fadeViewOut(final View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(mCompat.getActivity(this).getApplicationContext(), android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    private void fadeViewIn(final View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(mCompat.getActivity(this).getApplicationContext(), android.R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }
}
