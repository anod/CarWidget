package info.anodsplace.version.storage;

/**
 * @author alex
 * @date 11/26/13
 */
public interface Storage {

	int getVersion();

	void persistVersion(int versionCode);
}
