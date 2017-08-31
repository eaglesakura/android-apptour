package com.eaglesakura.android.apptour;

import android.support.annotation.ColorInt;

/**
 * AppTourのページごとの要素を習得する
 *
 * `ExampleFragment implements AppTourPage`
 */
public interface AppTourPage {
    @ColorInt
    int getBackgroundColor(AppTourDelegate self);
}
