package com.anod.car.home.prefs.preferences;

import com.anod.car.home.model.ShortcutInfo;

import java.io.Serializable;
import java.util.HashMap;

public class InCarBackup implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private HashMap<Integer, ShortcutInfo> notificationShortcuts;

    private InCar inCar;

    public InCarBackup(HashMap<Integer, ShortcutInfo> shortcuts, InCar inCar) {
        this.notificationShortcuts = shortcuts;
        this.inCar = inCar;
    }

    public HashMap<Integer, ShortcutInfo> getNotificationShortcuts() {
        return notificationShortcuts;
    }

    public void setNotificationShortcuts(HashMap<Integer, ShortcutInfo> shortcuts) {
        this.notificationShortcuts = shortcuts;
    }

    public InCar getInCar() {
        return inCar;
    }

    public void setInCar(InCar inCar) {
        this.inCar = inCar;
    }

}
