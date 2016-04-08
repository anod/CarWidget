package com.anod.car.home.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public class ApiCompat {

    public static Drawable getDrawable(@NonNull Resources res, @DrawableRes int id) {
        return res.getDrawable(id);
    }

    public static int getColor(@NonNull Resources res, @ColorRes int id) {
        return res.getColor(id);
    }

    public static Drawable getDrawableForDensity(@NonNull Resources res, @DrawableRes int id, int targetDensity) {
        return res.getDrawableForDensity(id, targetDensity);
    }
}
