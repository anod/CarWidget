package com.anod.car.home.prefs.detection;

/**
 * @author alex
 * @date 6/5/14
 */
public interface InCarItem {

    public abstract boolean isActive();

    public int getIconRes();

    public int getShortTitleRes();

    public int getSummaryRes();

    public void onClick();
}
