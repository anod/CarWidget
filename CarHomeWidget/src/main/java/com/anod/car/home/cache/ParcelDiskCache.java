package com.anod.car.home.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.anod.car.home.utils.AppLog;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final public class ParcelDiskCache<T extends Parcelable> implements DiskCache<Parcelable> {

    private static final int TYPE_LIST = 1;
    private static final int TYPE_PARCELABLE = 2;
    private static final Object sLock = new Object();
    private ClassLoader mClassLoader;
    private DiskLruCache mCache;
    private Executor mStoreExecutor;
    private boolean saveInUI = true;

    private ParcelDiskCache(File directory, int version, long maxSize, ClassLoader classLoader) throws IOException {
        mClassLoader = classLoader;
        mStoreExecutor = Executors.newSingleThreadExecutor();
        mCache = DiskLruCache.open(directory, version, 1, maxSize);
    }

    public static ParcelDiskCache open(Context context, String folder, long maxSize, ClassLoader classLoader) throws IOException {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        File directory = new File(cacheDir, folder);
        int version = getVersionCode(context) + Build.VERSION.SDK_INT + 1000;

        return new ParcelDiskCache(directory, version, maxSize, classLoader);
    }

    /**
     *
     * @param key [a-z0-9_-]{1,64}
     * @param value
     */
    public void set(String key, Parcelable value) {
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(TYPE_PARCELABLE);
        parcel.writeParcelable(value, 0);
        if (saveInUI) {
            saveValue(mCache, parcel, key);
        } else {
            mStoreExecutor.execute(new StoreParcelableValueTask(mCache, parcel, key));
        }
    }

    /**
     *
     * @param key [a-z0-9_-]{1,64}
     * @param values
     */
    public void set(String key, List<T> values) {
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(TYPE_LIST);
        parcel.writeList(values);
        if (saveInUI) {
            saveValue(mCache, parcel, key);
        } else {
            mStoreExecutor.execute(new StoreParcelableValueTask(mCache, parcel, key));
        }
    }

    public T get(String key) {
        Parcel parcel = getParcel(key);
        if (parcel != null) {
            try {
                int type = parcel.readInt();
                if (type == TYPE_LIST) {
                    throw new IllegalAccessError("get list data with getList method");
                }
                if (type != TYPE_PARCELABLE) {
                    throw new IllegalAccessError("Parcel doesn't contain parcelable data");
                }
                return parcel.readParcelable(mClassLoader);
            } catch (Exception e) {
                AppLog.ex(e);
            } finally {
                parcel.recycle();
            }
        }
        return null;
    }

    private Parcel getParcel(String key) {
        byte[] value = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mCache.get(key);
            if (snapshot == null) {
                return null;
            }
            value = getBytesFromStream(snapshot.getInputStream(0));
        } catch (IOException e) {
            AppLog.ex(e);
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(value, 0, value.length);
        parcel.setDataPosition(0);
        return parcel;
    }

    public boolean remove(String key) {
        try {
            return mCache.remove(key);
        } catch (IOException e) {
            AppLog.ex(e);
        }
        return false;
    }

    public List getAll() {
        return getAll(null);
    }

    public List getAll(String prefix) {
        List<T> list = new ArrayList<T>(1);
        File dir = mCache.getDirectory();
        File[] files = dir.listFiles();
        if (files != null) {
            list = new ArrayList<T>(files.length);
            for (File file : files) {
                String fileName = file.getName();
                if ((!TextUtils.isEmpty(prefix) && fileName.startsWith(prefix) && fileName.indexOf(".") > 0)
                        || (TextUtils.isEmpty(prefix) && fileName.indexOf(".") > 0)) {
                    String key = fileName.substring(0, fileName.indexOf("."));
                    T value = get(key);
                    list.add(value);
                }
            }
        }
        return list;
    }

    public void clear() {
        try {
            mCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String key) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mCache.get(key);
            if (snapshot == null) {
                return false;
            }
            return snapshot.getLength(0) > 0;
        } catch (IOException e) {
            AppLog.ex(e);
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            mCache.close();
        } catch (IOException e) {
            AppLog.ex(e);
        }
    }

    public void shouldSaveInUI() {
        this.saveInUI = true;
    }

    private static class StoreParcelableValueTask implements Runnable {

        private final DiskLruCache cache;
        private final Parcel value;
        private final String key;

        public StoreParcelableValueTask(DiskLruCache cache, Parcel value, String key) {
            this.value = value;
            this.key = key;
            this.cache = cache;
        }

        @Override
        public void run() {
            saveValue(cache, value, key);
        }
    }

    private static void saveValue(DiskLruCache cache, Parcel value, String key) {
        if (cache == null) return;
        try {
            synchronized (sLock) {
                DiskLruCache.Editor editor = cache.edit(key);
                OutputStream outputStream = editor.newOutputStream(0);
                writeBytesToStream(outputStream, value.marshall());
                editor.commit();
            }
        } catch (IOException e) {
            AppLog.ex(e);
        } finally {
            value.recycle();
        }
    }

    public static byte[] getBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[1024];
            int count;
            while ((count = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, count);
            }
            buffer.flush();
            return buffer.toByteArray();
        } finally {
            is.close();
            buffer.close();
        }
    }

    public static void writeBytesToStream(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    public static int getVersionCode(Context context) {
        int result = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            result = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}