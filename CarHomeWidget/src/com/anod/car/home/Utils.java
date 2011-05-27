package com.anod.car.home;

public class Utils {
	public static float calcIconsScale(String scaleString) {
        return 1.0f+0.1f*Integer.valueOf(scaleString);
	}
}
