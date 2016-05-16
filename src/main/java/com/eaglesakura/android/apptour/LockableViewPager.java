package com.eaglesakura.android.apptour;

import com.vlonjatg.android.apptourlibrary.R;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

public class LockableViewPager extends ViewPager {
    private boolean mLocked = true;

    public LockableViewPager(Context context) {
        super(context);
        init();
    }

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * http://stackoverflow.com/questions/11962268/viewpager-setcurrentitempageid-true-does-not-smoothscroll
     */
    void init() {
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(this, new Scroller(getContext(), new AccelerateInterpolator()) {
                int mDuration = getResources().getInteger(R.integer.AppTour_Pager_Duration);

                @Override
                public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                    super.startScroll(startX, startY, dx, dy, mDuration);
                }

                @Override
                public void startScroll(int startX, int startY, int dx, int dy) {
                    super.startScroll(startX, startY, dx, dy, mDuration);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLocked) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLocked) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
