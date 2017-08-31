package com.eaglesakura.android.apptour;

import com.vlonjatg.android.apptourlibrary.R;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class AppTourDelegate {

    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

    private LockableViewPager mIntroViewPager;
    private Button mSkipIntroButton;
    private Button mDoneSlideButton;
    private ImageButton mNextSlideImageButton;

    /**
     * Indicator
     */
    private AppTourIndicator mIndicator;

    /**
     * ドット表示
     */
    private PagerAdapter mPagerAdapter;
    private boolean mSkipForceHidden;
    private boolean mNextForceHidden;
    private boolean mDoneForceHidden;

    @NonNull
    private AppTourCompat mCompat;

    @Nullable
    private OnSlideChangeListener mSlideChangeListener;

    @Nullable
    private OnClickListener mNextClickListener;

    @Nullable
    private OnClickListener mSkipClickListener;

    @Nullable
    private OnClickListener mDoneClickListener;

    private View mRootView;

    @ColorInt
    private int mOldStatusBarColor;

    @ColorInt
    private int mOldNavigationBarColor;

    /**
     * ナビゲーションバーの色を変更する場合はtrue
     */
    private boolean mNavigationBarColorControl = true;

    public AppTourDelegate(@NonNull AppTourCompat compat) {
        mCompat = compat;
    }

    /**
     * AppTourを組み込む
     */
    public interface AppTourCompat {
        @LayoutRes
        int getLayoutId(@NonNull AppTourDelegate self);

        @NonNull
        Activity getActivity(@NonNull AppTourDelegate self);

        /**
         * AppTourとして表示対象のコンテンツを習得する
         */
        @NonNull
        PagerAdapter newPagerAdapter(@NonNull AppTourDelegate self);
    }

    public interface OnClickListener {
        /**
         * ボタンが押された
         */
        void onTourClick(@NonNull AppTourDelegate self);
    }

    /**
     * スライド変更時にコールバックする
     */
    public interface OnSlideChangeListener {
        void onTourSlideChanged(@NonNull AppTourDelegate self, int tourIndex, @NonNull Fragment slide);
    }

    public <T extends View> T onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        {
            int layoutId = mCompat.getLayoutId(this);
            if (layoutId == 0) {
                layoutId = R.layout.apptour;
            }
            Activity activity = mCompat.getActivity(this);
            mRootView = inflater.inflate(layoutId, container, false);
        }

        mIntroViewPager = mRootView.findViewById(R.id.AppTour_ViewPager);
        mSkipIntroButton = mRootView.findViewById(R.id.AppTour_Nav_SkipIntro);
        mNextSlideImageButton = mRootView.findViewById(R.id.AppTour_NextSlide);
        mDoneSlideButton = mRootView.findViewById(R.id.AppTour_Done);
        mIndicator = mRootView.findViewById(R.id.AppTour_Dots);

        //Instantiate the PagerAdapterImpl.
        mPagerAdapter = mCompat.newPagerAdapter(this);
        mIntroViewPager.setAdapter(mPagerAdapter);

        //Instantiate the indicator mDots if there are more than one slide
        if (mPagerAdapter.getCount() >= 2) {
            // 2ページ以上のスライドがある
            if (!mSkipForceHidden) {
                mSkipIntroButton.setVisibility(View.VISIBLE);
            }
        } else {
            // 1ページしかスライドがない
            mSkipIntroButton.setVisibility(View.INVISIBLE);
            mNextSlideImageButton.setVisibility(View.INVISIBLE);

            if (!mDoneForceHidden) {
                mDoneSlideButton.setVisibility(View.VISIBLE);
            }
        }

        // インジケータを初期化する
        if (mIndicator != null) {
            mIndicator.initializeIndicators(this, mPagerAdapter.getCount());
        }

        //  Dump status bar color
        Window window = mCompat.getActivity(AppTourDelegate.this).getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mOldNavigationBarColor = window.getNavigationBarColor();
            mOldStatusBarColor = window.getStatusBarColor();
        }

        setListeners();
        return (T) mRootView;
    }

    /**
     * ナビゲーションバー / ステータスバーの色を同期する場合true
     * Version >= Lollipop
     */
    public void setNavigationBarColorControl(boolean navigationBarColorControl) {
        mNavigationBarColorControl = navigationBarColorControl;
    }

    public void setOnNextClickListener(@Nullable OnClickListener nextClickListener) {
        mNextClickListener = nextClickListener;
    }

    public void setOnSkipClickListener(@Nullable OnClickListener skipClickListener) {
        mSkipClickListener = skipClickListener;
    }

    public void setOnDoneClickListener(@Nullable OnClickListener doneClickListener) {
        mDoneClickListener = doneClickListener;
    }

    /**
     * スライド変更時のハンドリングを行う
     */
    public void setSlideChangeListener(@Nullable OnSlideChangeListener slideChangeListener) {
        mSlideChangeListener = slideChangeListener;
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
     * Viewを生成する
     */
    @NonNull
    public View getView() {
        return mRootView;
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
     * 次のスライドへ進む
     */
    public int nextSlide() {
        int nextSlidePosition = getCurrentSlide() + 1;
        setCurrentSlide(nextSlidePosition);
        return nextSlidePosition;
    }

    /**
     * 前のスライドに移動する
     * 最初のページにいる場合、何もしない
     *
     * @return スライドページ
     */
    public int previousSlide() {
        int position = getCurrentSlide() - 1;
        if (position >= 0) {
            setCurrentSlide(position);
            return position;
        } else {
            return 0;
        }
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
    public void showIndicator() {
        mIndicator.setVisibility(this, View.VISIBLE);
    }

    /**
     * Hide indicator mDots
     */
    public void hideIndicator() {
        mIndicator.setVisibility(this, View.INVISIBLE);
    }

    /**
     * 変更された設定をすべて元に戻す
     */
    public void dispose() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        Window window = mCompat.getActivity(this).getWindow();
        window.setNavigationBarColor(mOldNavigationBarColor);
        window.setStatusBarColor(mOldStatusBarColor);
    }

    protected Fragment getSlide(int position) {
        if (mPagerAdapter instanceof FragmentPagerAdapter) {
            return ((FragmentPagerAdapter) mPagerAdapter).getItem(position);
        } else if (mPagerAdapter instanceof FragmentStatePagerAdapter) {
            return ((FragmentStatePagerAdapter) mPagerAdapter).getItem(position);
        } else {
            throw new IllegalStateException("fragment not support");
        }
    }

    /**
     * 各ページの背景色を習得する
     */
    private int getPageColor(int position) {
        Fragment item = getSlide(position);
        if (item instanceof AppTourPage) {
            return ((AppTourPage) item).getBackgroundColor(this);
        } else {
            return Color.TRANSPARENT;
        }
    }

    private void setListeners() {
        ViewGroup controlsRoot = getView().findViewById(R.id.AppTour_Nav_Root);
        mIntroViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                int color;
                if (position < (mPagerAdapter.getCount() - 1) && position < (mPagerAdapter.getCount() - 1)) {
                    color = (Integer) mArgbEvaluator.evaluate(positionOffset, getPageColor(position), getPageColor(position + 1));
                } else {
                    color = getPageColor(mPagerAdapter.getCount() - 1);
                }

                mIntroViewPager.setBackgroundColor(color);
                controlsRoot.setBackgroundColor(color);

                // setup statusbar color
                Window window = mCompat.getActivity(AppTourDelegate.this).getWindow();
                if (mNavigationBarColorControl && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(color);
                    window.setNavigationBarColor(color);
                }
            }

            @Override
            public void onPageSelected(int position) {
                //Hide SKIP button if last slide item, visible if not
                if (position == (mPagerAdapter.getCount() - 1)) {
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
                if (position == (mPagerAdapter.getCount() - 1)) {
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
                if (mIndicator != null) {
                    mIndicator.onPageSelected(AppTourDelegate.this, position);
                }

                // callback
                if (mSlideChangeListener != null) {
                    mSlideChangeListener.onTourSlideChanged(AppTourDelegate.this, position, getSlide(position));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mSkipIntroButton.setOnClickListener((it) -> {
            if (mSkipClickListener != null) {
                mSkipClickListener.onTourClick(this);
            }
        });
        mNextSlideImageButton.setOnClickListener((it) -> {
            if (mNextClickListener != null) {
                mNextClickListener.onTourClick(this);
            }
            mIntroViewPager.setCurrentItem(mIntroViewPager.getCurrentItem() + 1, true);
        });
        mDoneSlideButton.setOnClickListener((it) -> {
            if (mDoneClickListener != null) {
                mDoneClickListener.onTourClick(this);
            }
        });
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
