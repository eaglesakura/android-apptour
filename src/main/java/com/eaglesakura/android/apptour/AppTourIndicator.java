package com.eaglesakura.android.apptour;

import android.support.annotation.UiThread;

/**
 * ViewPagerのページ設定用Indicator
 */
public interface AppTourIndicator {
    /**
     * 可視状態を更新する
     *
     * @param visibility {@link android.view.View#VISIBLE}, {@link android.view.View#INVISIBLE}
     */
    @UiThread
    void setVisibility(AppTourDelegate self, int visibility);

    /**
     * Indicatorを初期化する
     *
     * @param pages 全ページ数
     */
    @UiThread
    void initializeIndicators(AppTourDelegate self, int pages);

    /**
     * ページが更新された
     *
     * @param position 新しいページ
     */
    @UiThread
    void onPageSelected(AppTourDelegate self, int position);
}
