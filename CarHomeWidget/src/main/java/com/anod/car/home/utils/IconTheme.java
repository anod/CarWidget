package com.anod.car.home.utils;

/**
 * @author alex
 * @date 5/25/13
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.SimpleArrayMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

public class IconTheme {

    private static final String ATTR_COMPONENT = "component";

    private static final String ATTR_DRAWABLE = "drawable";

    private static final String TAG_ITEM = "item";

    private static final String FILE_APPFILTER_NAME = "appfilter";

    private static final String FILE_APPFILTER_TYPE = "xml";

    Context mContext;

    String mPkgName;

    Resources mThemeResources;

    SimpleArrayMap<String, Integer> mIconMap;

    public final static Object sLock = new Object();

    public IconTheme(Context context, String packageName) {
        mContext = context;
        mPkgName = packageName;
    }

    public String getPackageName() {
        return mPkgName;
    }

    public boolean loadThemeResources() {
        try {
            mThemeResources = mContext.getPackageManager().getResourcesForApplication(mPkgName);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.d(e.getMessage());
        }
        return mThemeResources != null;
    }

    public void loadFromXml(SimpleArrayMap<String, Integer> cmpMap) {
        XmlPullParser xml = loadXmlResource();
        if (xml == null) {
            mIconMap = fallback(cmpMap);
            return;
        }
        try {
            synchronized (sLock) {
                mIconMap = parseXml(xml, cmpMap);
            }
        } catch (XmlPullParserException | IOException e) {
            AppLog.e(e);
        }
    }

    private SimpleArrayMap<String, Integer> fallback(SimpleArrayMap<String, Integer> cmpMap) {
        int found = 0;
        int required = cmpMap.size();
        SimpleArrayMap<String, Integer> iconMap = new SimpleArrayMap<String, Integer>(required);

        //Fallback
        for(int i=0; i< cmpMap.size(); i++) {
            String className = cmpMap.keyAt(i);
            if (!iconMap.containsKey(className)) {
                String resName = className.toLowerCase(Locale.US).replace(".", "_");
                AppLog.d("Look for icon for resource: R.drawable." + resName);
                int resourceId = mThemeResources.getIdentifier(resName, "drawable", mPkgName);
                if (resourceId != 0) {
                    iconMap.put(className, resourceId);
                    found++;
                }
            }
        }

        if (found == 0) {
            return null;
        }
        return iconMap;
    }

    private XmlPullParser loadXmlResource() {
        XmlPullParser xml = null;
        try {
            int appFilterId = mThemeResources
                    .getIdentifier(FILE_APPFILTER_NAME, FILE_APPFILTER_TYPE, mPkgName);
            if (appFilterId != 0) {
                xml = mThemeResources.getXml(appFilterId);
            } else {
                InputStream localInputStream = mThemeResources.getAssets()
                        .open(FILE_APPFILTER_NAME + "." + FILE_APPFILTER_TYPE);
                XmlPullParserFactory localXmlPullParserFactory = XmlPullParserFactory.newInstance();
                localXmlPullParserFactory.setNamespaceAware(true);
                xml = localXmlPullParserFactory.newPullParser();
                xml.setInput(localInputStream, "UTF-8");
            }
        } catch (IOException | XmlPullParserException e) {
            AppLog.e(e);
        }
        return xml;
    }

    private SimpleArrayMap<String, Integer> parseXml(XmlPullParser xml, SimpleArrayMap<String, Integer> cmpMap)
            throws XmlPullParserException, IOException {

        int required = cmpMap.size();
        SimpleArrayMap<String, Integer> iconMap = new SimpleArrayMap<String, Integer>(required);

        int eventType = xml.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && TAG_ITEM.equals(xml.getName())) {
                String component = xml.getAttributeValue(null, ATTR_COMPONENT);
                String drawable = xml.getAttributeValue(null, ATTR_DRAWABLE);
                ComponentName componentName = null;
                if (component != null && component.startsWith("ComponentInfo{")) {
                    componentName = ComponentName.unflattenFromString(component
                            .substring("ComponentInfo{".length(), -1 + component.length()));
                }
                if (componentName != null && cmpMap.containsKey(componentName.getClassName())) {
                    int drawableId = mThemeResources.getIdentifier(drawable, "drawable", mPkgName);
                    if (drawableId != 0) {
                        iconMap.put(componentName.getClassName(), drawableId);
                    }
                }
                if (iconMap.size() == required) {
                    break;
                }
            }
            eventType = xml.next();
        }

        if (iconMap.size() == required) {
            return iconMap;
        }

        if (iconMap.size() == 0) {
            return null;
        }
        return iconMap;
    }

    public int getIcon(String className) {
        if (mIconMap != null) {
            if (mIconMap.containsKey(className)) {
                return mIconMap.get(className);
            }
        }
        return 0;
    }

    public Drawable getDrawable(@DrawableRes int resId) {
        return ResourcesCompat.getDrawableForDensity(mThemeResources, resId, UtilitiesBitmap.getTargetDensity(mContext), null);
    }
}