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
import android.view.WindowManager;
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

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private final ArrayList<Integer> colors = new ArrayList<>();
    private final List<Fragment> fragments = new Vector<>();
    private LockableViewPager introViewPager;
    private RelativeLayout controlsRelativeLayout;
    private Button skipIntroButton;
    private Button doneSlideButton;
    private ImageButton nextSlideImageButton;
    private View separatorView;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private PagerAdapter pagerAdapter;
    private int currentPosition;
    private int activeDotColor;
    private int inactiveDocsColor;
    private int numberOfSlides;
    private boolean isSkipForceHidden;
    private boolean isNextForceHidden;
    private boolean isDoneForceHidden;

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
        Activity activity = mCompat.getActivity(this);
//        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);   // call Activity!!

        introViewPager = (LockableViewPager) mRootView.findViewById(R.id.AppTour_ViewPager);
        controlsRelativeLayout = (RelativeLayout) mRootView.findViewById(R.id.controlsRelativeLayout);
        skipIntroButton = (Button) mRootView.findViewById(R.id.AppTour_Nav_SkipIntro);
        nextSlideImageButton = (ImageButton) mRootView.findViewById(R.id.AppTour_Nav_NextSlide);
        doneSlideButton = (Button) mRootView.findViewById(R.id.AppTour_Nav_Done);
        separatorView = mRootView.findViewById(R.id.AppTour_Separator);
        dotsLayout = (LinearLayout) mRootView.findViewById(R.id.AppTour_Nav_Dots);

        activeDotColor = Color.RED;
        inactiveDocsColor = Color.WHITE;

        //Instantiate the PagerAdapter.
        pagerAdapter = new PagerAdapter(mCompat.getFragmentManager(this), fragments);
        introViewPager.setAdapter(pagerAdapter);

        mCompat.onTourInitialize(this, savedInstanceState);

        numberOfSlides = fragments.size();

        //Instantiate the indicator dots if there are more than one slide
        if (numberOfSlides > 1) {
            setViewPagerDots();

            if (!isSkipForceHidden) {
                skipIntroButton.setVisibility(View.VISIBLE);
            }
        } else {
            skipIntroButton.setVisibility(View.INVISIBLE);
            nextSlideImageButton.setVisibility(View.INVISIBLE);

            if (!isDoneForceHidden) {
                doneSlideButton.setVisibility(View.VISIBLE);
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
        introViewPager.setLocked(lock);
    }

    /**
     * 戻るボタンが押されたら呼び出す。
     *
     * @return ハンドリングを行ったらtrue
     */
    public boolean onBackPressed() {
        int item = introViewPager.getCurrentItem();
        if (item == 0) {
            return false;
        } else {
            introViewPager.setCurrentItem(item - 1);
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
        fragments.add(fragment);
        addBackgroundColor(Color.TRANSPARENT);
        pagerAdapter.notifyDataSetChanged();
    }

    /**
     * Add a slide to the intro
     *
     * @param fragment Fragment of the slide to be added
     * @param color    Background color of the fragment
     */
    public void addSlide(@NonNull Fragment fragment, @ColorInt int color) {
        fragments.add(fragment);
        addBackgroundColor(color);
        pagerAdapter.notifyDataSetChanged();
    }

    /**
     * Return slides
     *
     * @return Return slides
     */
    public List<Fragment> getSlides() {
        return pagerAdapter.getFragments();
    }

    /**
     * Get which slide is currently active
     *
     * @return Returns the current active slide index
     */
    public int getCurrentSlide() {
        return introViewPager.getCurrentItem();
    }

    /**
     * Set the currently selected slide
     *
     * @param position Item index to select
     */
    public void setCurrentSlide(int position) {
        introViewPager.setCurrentItem(position, true);
    }

    /**
     * Set the string value of the skip button
     *
     * @param text String value to set
     */
    public void setSkipText(@NonNull String text) {
        skipIntroButton.setText(text);
    }

    /**
     * Set the string value of the done button
     *
     * @param text String value to set
     */
    public void setDoneText(@NonNull String text) {
        doneSlideButton.setText(text);
    }

    /**
     * Set the text color of the skip button
     *
     * @param color Color value to set
     */
    public void setSkipButtonTextColor(@ColorInt int color) {
        skipIntroButton.setTextColor(color);
    }

    /**
     * Set the next button color to white
     */
    public void setNextButtonColorToWhite() {
        nextSlideImageButton.setImageResource(R.drawable.ic_next_white_24dp);
    }

    /**
     * Set the next button color to black
     */
    public void setNextButtonColorToBlack() {
        nextSlideImageButton.setImageResource(R.drawable.ic_next_black_24dp);
    }

    /**
     * Set the text color of the done button
     *
     * @param color Color value to set
     */
    public void setDoneButtonTextColor(@ColorInt int color) {
        doneSlideButton.setTextColor(color);
    }

    /**
     * Set the color of the separator between slide content and bottom controls
     *
     * @param color Color value to set
     */
    public void setSeparatorColor(@ColorInt int color) {
        separatorView.setBackgroundColor(color);
    }

    /**
     * Set the color of the active dot indicator
     *
     * @param color Color value to set
     */
    public void setActiveDotColor(@ColorInt int color) {
        activeDotColor = color;
    }

    /**
     * Set the color of the inactive dot indicator
     *
     * @param color Color value to set
     */
    public void setInactiveDocsColor(@ColorInt int color) {
        inactiveDocsColor = color;
    }

    /**
     * Show the skip button
     */
    public void showSkip() {
        skipIntroButton.setVisibility(View.VISIBLE);
        isSkipForceHidden = false;
    }

    /**
     * Hide the skip button
     */
    public void hideSkip() {
        skipIntroButton.setVisibility(View.INVISIBLE);
        isSkipForceHidden = true;
    }

    /**
     * Show the next button
     */
    public void showNext() {
        nextSlideImageButton.setVisibility(View.VISIBLE);
        isNextForceHidden = false;
    }

    /**
     * Hide the next button
     */
    public void hideNext() {
        nextSlideImageButton.setVisibility(View.INVISIBLE);
        isNextForceHidden = true;
    }

    /**
     * Show the done button
     */
    public void showDone() {
        doneSlideButton.setVisibility(View.VISIBLE);
        isDoneForceHidden = false;
    }

    /**
     * Hide the done button
     */
    public void hideDone() {
        doneSlideButton.setVisibility(View.INVISIBLE);
        isDoneForceHidden = true;
    }

    /**
     * Show indicator dots
     */
    public void showIndicatorDots() {
        dotsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hide indicator dots
     */
    public void hideIndicatorDots() {
        dotsLayout.setVisibility(View.INVISIBLE);
    }

    private void addBackgroundColor(@ColorInt int color) {
        colors.add(color);
    }

    private void setListeners() {
        introViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (pagerAdapter.getCount() - 1) && position < (colors.size() - 1)) {
                    int color = (Integer)
                            argbEvaluator.evaluate(positionOffset, colors.get(position), colors.get(position + 1));
                    introViewPager.setBackgroundColor(color);
                    controlsRelativeLayout.setBackgroundColor(color);
                } else {
                    int color = colors.get(colors.size() - 1);
                    introViewPager.setBackgroundColor(color);
                    controlsRelativeLayout.setBackgroundColor(color);
                }
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;

                //Hide SKIP button if last slide item, visible if not
                if (position == numberOfSlides - 1) {
                    if (!isSkipForceHidden) {
                        fadeViewOut(skipIntroButton);
                    }
                } else {
                    if (skipIntroButton.getVisibility() == View.INVISIBLE && !isSkipForceHidden) {
                        fadeViewIn(skipIntroButton);
                    }
                }

                //Hide NEXT button if last slide item and set DONE button
                //visible, otherwise hide Done button and set NEXT button visible
                if (position == numberOfSlides - 1) {
                    if (!isNextForceHidden) {
                        fadeViewOut(nextSlideImageButton);
                    }

                    if (!isDoneForceHidden) {
                        fadeViewIn(doneSlideButton);
                    }
                } else {
                    if (nextSlideImageButton.getVisibility() == View.INVISIBLE && !isNextForceHidden) {
                        fadeViewIn(nextSlideImageButton);
                    }

                    if (doneSlideButton.getVisibility() == View.VISIBLE && !isDoneForceHidden) {
                        fadeViewOut(doneSlideButton);
                    }
                }

                //Set dots
                if (numberOfSlides > 1) {
                    //Set current inactive dots color
                    for (int i = 0; i < numberOfSlides; i++) {
                        dots[i].setTextColor(inactiveDocsColor);
                    }

                    //Set current active dot color
                    dots[position].setTextColor(activeDotColor);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        skipIntroButton.setOnClickListener((it) -> mCompat.onClickTourSkip(this));
        nextSlideImageButton.setOnClickListener((it) -> introViewPager.setCurrentItem(currentPosition + 1, true));
        doneSlideButton.setOnClickListener((it) -> mCompat.onClickTourDone(this));
    }

    private void setViewPagerDots() {
        dots = new TextView[numberOfSlides];

        //Set first inactive dots color
        for (int i = 0; i < numberOfSlides; i++) {
            dots[i] = new TextView(mCompat.getActivity(this));
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(inactiveDocsColor);
            dotsLayout.addView(dots[i]);
        }

        //Set first active dot color
        dots[0].setTextColor(activeDotColor);
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
