package com.anod.car.home.utils;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * @author algavris
 * @date 13/08/2016.
 */
public class HtmlCompat {
    public static Spanned fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= 24)
        {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(text);
    }
}
