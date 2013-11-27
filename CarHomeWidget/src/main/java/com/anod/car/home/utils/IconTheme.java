package com.anod.car.home.utils;

/**
 * @author alex
 * @date 5/25/13
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

public class IconTheme
{
	private static final String ATTR_COMPONENT = "component";
	private static final String ATTR_DRAWABLE = "drawable";
	private static final String TAG_ITEM = "item";
	private static final String FILE_APPFILTER_NAME = "appfilter";
	private static final String FILE_APPFILTER_TYPE = "xml";

	Context mContext;
	String mPkgName;
	Resources mThemeResources;
	HashMap<String, Integer> mIconMap;
	public static Object sLock = new Object();
	public IconTheme(Context context, String packageName)
	{
		mContext = context;
		mPkgName = packageName;
	}

	public boolean loadThemeResources() {
		try {
			mThemeResources = mContext.getPackageManager().getResourcesForApplication(mPkgName);
		} catch (PackageManager.NameNotFoundException e) {
			AppLog.d(e.getMessage());
		}
		return mThemeResources != null;
	}

	public void loadFromXml(HashMap<String, Integer> cmpMap)
	{
		XmlPullParser xml = loadXmlResource();
		if (xml == null) {
			mIconMap = fallback(cmpMap);
			return;
		}
		try {
			synchronized (sLock) {
				mIconMap = parseXml(xml, cmpMap);
			}
		} catch (XmlPullParserException e) {
			AppLog.d(e.getMessage());
		} catch (IOException e) {
			AppLog.d(e.getMessage());
		}
	}

	private HashMap<String, Integer> fallback(HashMap<String, Integer> cmpMap) {
		int found = 0;
		int required = cmpMap.size();
		HashMap<String, Integer> iconMap = new HashMap<String, Integer>(required);

		//Fallback
		for(String className : cmpMap.keySet()) {
			if (!iconMap.containsKey(className)) {
				String resName= className.toLowerCase(Locale.US).replace(".", "_");
				AppLog.d("Look for icon for resource: R.drawable." + resName);
				int resourceId = mThemeResources.getIdentifier(resName, "drawable", mPkgName);
				if(resourceId!=0){
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
		try
		{
			int appFilterId = mThemeResources.getIdentifier(FILE_APPFILTER_NAME, FILE_APPFILTER_TYPE, mPkgName);
			if (appFilterId != 0)
			{
				xml = mThemeResources.getXml(appFilterId);
			}
			else
			{
				InputStream localInputStream = mThemeResources.getAssets().open(FILE_APPFILTER_NAME + "." + FILE_APPFILTER_TYPE);
				XmlPullParserFactory localXmlPullParserFactory = XmlPullParserFactory.newInstance();
				localXmlPullParserFactory.setNamespaceAware(true);
				xml = localXmlPullParserFactory.newPullParser();
				xml.setInput(localInputStream, "UTF-8");
			}
		} catch (IOException localIOException2)
		{
			AppLog.d(localIOException2.getMessage());
		}
		catch (XmlPullParserException localXmlPullParserException1)
		{
			AppLog.d(localXmlPullParserException1.getMessage());
		}
		return xml;
	}

	private HashMap<String, Integer> parseXml(XmlPullParser xml, HashMap<String, Integer> cmpMap) throws XmlPullParserException, IOException {

		int required = cmpMap.size();
		HashMap<String, Integer> iconMap = new HashMap<String, Integer>(required);

		int eventType = xml.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG && TAG_ITEM.equals(xml.getName())) {
				String component = xml.getAttributeValue(null, ATTR_COMPONENT);
				String drawable = xml.getAttributeValue(null, ATTR_DRAWABLE);
				ComponentName componentName = null;
				if (component != null && component.startsWith("ComponentInfo{")) {
					componentName = ComponentName.unflattenFromString(component.substring("ComponentInfo{".length(), -1 + component.length()));
				}
				if (componentName != null && cmpMap.containsKey(componentName.getClassName()))
				{
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

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public Drawable getDrawable(int resId) {

		if (UtilitiesBitmap.HAS_HIRES_SUPPORT) {
			return mThemeResources.getDrawableForDensity(resId, UtilitiesBitmap.getTargetDensity(mContext));
		}

		return mThemeResources.getDrawable(resId);
	}
}