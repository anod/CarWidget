package info.anodsplace.version;

/**
 * @author alex
 * @date 11/26/13
 */
public interface Action {

    void onUpgrade(int oldVersion, int newVersion);

}
