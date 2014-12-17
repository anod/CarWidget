package com.anod.car.home.utils;

import android.content.Context;
import android.graphics.Color;

public class ColorUtils {

    public static String toHex(int color, boolean addAlpha) {
        String hexStr = String.format("%08X", color);
        if (!addAlpha) {
            hexStr = hexStr.substring(2);
        }
        return hexStr;
    }

    public static int fromHex(String hexStr, boolean addAlpha, int defColor) {
        int intValue = defColor;
        try {
            intValue = Color.parseColor("#" + hexStr);
        } catch (IllegalArgumentException e) {
            AppLog.d(e.getMessage());
            return defColor;
        }
        if (!addAlpha) {
            intValue = (intValue & 0x00FFFFFF) + 0xFF000000;
        }
        return intValue;
    }

    /**
     * Create an array of int with colors
     *
     * @param context
     * @return
     */
    public static int[] colorChoice(Context context, int resId){

        int[] mColorChoices=null;
        String[] color_array = context.getResources().getStringArray(resId);

        if (color_array!=null && color_array.length>0) {
            mColorChoices = new int[color_array.length];
            for (int i = 0; i < color_array.length; i++) {
                mColorChoices[i] = Color.parseColor(color_array[i]);
            }
        }
        return mColorChoices;
    }

    /**
     * Parse whiteColor
     *
     * @return
     */
    public static int parseWhiteColor(){
        return Color.parseColor("#FFFFFF");
    }

}