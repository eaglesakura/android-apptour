package com.vlonjatg.android.apptourlibrary;

import com.eaglesakura.android.apptour.AppTourDelegate;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;

import java.util.List;

/**
 * Fork元のAppTourとの互換性を取るためのActivity
 *
 * @author Vlonjat Gashi (vlonjatg)
 */
public abstract class AppTour extends AppCompatActivity implements AppTourDelegate.AppTourCompat {

    private AppTourDelegate mTourDelegate = new AppTourDelegate(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mTourDelegate.getView());
        mTourDelegate.onCreate(savedInstanceState);
        mTourDelegate.setSwipeLock(false);  // unlock user swipe
        mTourDelegate.setNextButtonColorToWhite();
        mTourDelegate.setSkipButtonTextColor(Color.WHITE);
        mTourDelegate.setDoneButtonTextColor(Color.WHITE);
    }

    @NonNull
    @Override
    public Activity getActivity(@NonNull AppTourDelegate self) {
        return this;
    }

    @NonNull
    @Override
    public FragmentManager getFragmentManager(@NonNull AppTourDelegate self) {
        return getSupportFragmentManager();
    }

    @NonNull
    @Override
    public LayoutInflater getLayoutInflater(@NonNull AppTourDelegate self) {
        return getLayoutInflater();
    }

    @Override
    public final void onTourInitialize(@NonNull AppTourDelegate self, @Nullable Bundle savedInstanceState) {
        init(savedInstanceState);
    }

    @Override
    public final void onClickTourSkip(@NonNull AppTourDelegate self, int tourPosition) {
        onSkipPressed();
    }

    @Override
    public final void onClickTourDone(@NonNull AppTourDelegate self) {
        onDonePressed();
    }

    public void addSlide(@NonNull Fragment fragment) {
        mTourDelegate.addSlide(fragment);
    }

    public void addSlide(@NonNull Fragment fragment, @ColorInt int color) {
        mTourDelegate.addSlide(fragment, color);
    }

    public List<Fragment> getSlides() {
        return mTourDelegate.getSlides();
    }

    public int getCurrentSlide() {
        return mTourDelegate.getCurrentSlide();
    }

    public void setCurrentSlide(int position) {
        mTourDelegate.setCurrentSlide(position);
    }

    public void setSkipText(@NonNull String text) {
        mTourDelegate.setSkipText(text);
    }

    public void setDoneText(@NonNull String text) {
        mTourDelegate.setDoneText(text);
    }

    public void setSkipButtonTextColor(@ColorInt int color) {
        mTourDelegate.setSkipButtonTextColor(color);
    }

    public void setNextButtonColorToWhite() {
        mTourDelegate.setNextButtonColorToWhite();
    }

    public void setNextButtonColorToBlack() {
        mTourDelegate.setNextButtonColorToBlack();
    }

    public void setDoneButtonTextColor(@ColorInt int color) {
        mTourDelegate.setDoneButtonTextColor(color);
    }

    public void setSeparatorColor(@ColorInt int color) {
        mTourDelegate.setSeparatorColor(color);
    }

    public void setActiveDotColor(@ColorInt int color) {
        mTourDelegate.setActiveDotColor(color);
    }

    public void setInactiveDocsColor(@ColorInt int color) {
        mTourDelegate.setInactiveDocsColor(color);
    }

    public void showSkip() {
        mTourDelegate.showSkip();
    }

    public void hideSkip() {
        mTourDelegate.hideSkip();
    }

    public void showNext() {
        mTourDelegate.showNext();
    }

    public void hideNext() {
        mTourDelegate.hideNext();
    }

    public void showDone() {
        mTourDelegate.showDone();
    }

    public void hideDone() {
        mTourDelegate.hideDone();
    }

    public void showIndicatorDots() {
        mTourDelegate.showIndicatorDots();
    }

    public void hideIndicatorDots() {
        mTourDelegate.hideIndicatorDots();
    }

    @Override
    public void onBackPressed() {
        if (mTourDelegate.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    public void setSwipeLock(boolean lock) {
        mTourDelegate.setSwipeLock(lock);
    }

    @Override
    public void setImmersive(boolean immersive) {
        mTourDelegate.setImmersive(immersive);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public abstract void init(@Nullable Bundle savedInstanceState);

    /**
     * Perform action when skip button is pressed
     */
    public abstract void onSkipPressed();

    /**
     * Perform action when done button is pressed
     */
    public abstract void onDonePressed();

}
