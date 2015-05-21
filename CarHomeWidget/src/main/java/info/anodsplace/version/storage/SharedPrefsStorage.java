package info.anodsplace.version.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author alex
 * @date 11/26/13
 */
public class SharedPrefsStorage implements Storage {

    public static final String VERSION_CODE = "version_code";

    private final SharedPreferences mPrefs;

    public SharedPrefsStorage(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getVersion() {
        return mPrefs.getInt(VERSION_CODE, 0);
    }

    @Override
    public void persistVersion(int versionCode) {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putInt(VERSION_CODE, 0);
        edit.commit();
    }
}
