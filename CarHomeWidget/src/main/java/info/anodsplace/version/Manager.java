package info.anodsplace.version;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import info.anodsplace.version.storage.Storage;

/**
 * @author alex
 * @date 11/26/13
 */
public class Manager {

    private final Storage mStorage;

    private final ArrayList<Action> mActions;

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        return info.versionCode;
    }

    public Manager(Storage storage) {
        mStorage = storage;
        mActions = new ArrayList<Action>();
    }

    public void addAction(Action action) {
        mActions.add(action);
    }

    public void check(int versionCode) {
        int oldVersion = mStorage.getVersion();
        if (versionCode > oldVersion) {
            onUpgrade(oldVersion, versionCode);
        }
        if (versionCode != oldVersion) {
            mStorage.persistVersion(versionCode);
        }
    }

    protected void onUpgrade(int oldVersion, int newVersion) {
        for (Action action : mActions) {
            action.onUpgrade(oldVersion, newVersion);
        }
    }


}
