package com.anod.car.home;

import android.content.ComponentName;

public class Utils {
	public static float calcIconsScale(String scaleString) {
        return 1.0f+0.1f*Integer.valueOf(scaleString);
	}
	
    public static String componentToString(ComponentName component) {
    	return component.getPackageName() + "/" + component.getClassName();
    }
    
    public static ComponentName stringToComponent(String compString) {
        String[] compParts = compString.split("/");
        return new ComponentName(compParts[0],compParts[1]);
    }
}
