package com.eaglesakura.android.apptour;

import com.vlonjatg.android.apptourlibrary.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * AppTour
 */
public class AppTourIndicatorView extends LinearLayout implements AppTourIndicator {

    @DrawableRes
    private int mActiveResourceId;

    @DrawableRes
    private int mInactiveResourceId;

    public AppTourIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.AppTourIndicator);
        try {
            mActiveResourceId = typedArray.getResourceId(R.styleable.AppTourIndicator_srcActive, R.drawable.apptour_indicator_active);
            mInactiveResourceId = typedArray.getResourceId(R.styleable.AppTourIndicator_srcActive, R.drawable.apptour_indicator_inactive);
        } finally {
            typedArray.recycle();
        }

        setOrientation(HORIZONTAL);
    }

    @Override
    public void setVisibility(AppTourDelegate self, int visibility) {
        setVisibility(visibility);
    }

    @Override
    public void initializeIndicators(AppTourDelegate self, int pages) {
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.AppTour_Indicator_Margin);
        for (int i = 0; i < pages; ++i) {
            Indicator indicator = new Indicator(getContext());
            LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = margin;
            params.rightMargin = margin;
            addView(indicator, params);
        }

        onPageSelected(self, 0);
    }

    @Override
    public void onPageSelected(AppTourDelegate self, int position) {
        for (int i = 0; i < getChildCount(); ++i) {
            Indicator indicator = (Indicator) getChildAt(i);
            if (i == position) {
                indicator.active();
            } else {
                indicator.inactive();
            }
        }
    }

    class Indicator extends FrameLayout {
        AppCompatImageView mActive;

        AppCompatImageView mInactive;

        Indicator(Context context) {
            super(context);

            // drawableの大きさが不揃いの場合に備えて、Gravity.CENTERしておく
            
            {
                mInactive = new AppCompatImageView(context);
                mInactive.setBackgroundDrawable(ResourcesCompat.getDrawable(context.getResources(), mInactiveResourceId, null));
                FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                addView(mInactive, params);
            }

            {
                mActive = new AppCompatImageView(context);
                mActive.setBackgroundDrawable(ResourcesCompat.getDrawable(context.getResources(), mActiveResourceId, null));
                FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                addView(mActive, params);
            }
        }

        private void stopAnim(View view) {
            Animation animation = view.getAnimation();
            if (animation != null) {
                animation.cancel();
            }
        }

        private void show(View view) {
            stopAnim(view);

            ViewPropertyAnimator animate = view.animate();
            animate.alphaBy(view.getAlpha());
            animate.alpha(1);
            animate.setDuration(getContext().getResources().getInteger(R.integer.AppTour_Indicator_Duration));
            animate.start();
        }

        private void hide(View view) {
            stopAnim(view);

            ViewPropertyAnimator animate = view.animate();
            animate.alphaBy(view.getAlpha());
            animate.alpha(0);
            animate.setDuration(getContext().getResources().getInteger(R.integer.AppTour_Indicator_Duration));
            animate.start();
        }

        void active() {
            show(mActive);
            hide(mInactive);
        }

        void inactive() {
            hide(mActive);
            show(mInactive);
        }
    }
}
