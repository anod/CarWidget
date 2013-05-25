package com.anod.car.home.utils;

/**
 * @author alex
 * @date 5/25/13
 */
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import com.anod.car.home.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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

	public IconTheme(Context context, String packageName)
	{
		mContext = context;
		mPkgName = packageName;
	}

	public boolean loadThemeResources() {
		try {
			mThemeResources = mContext.getPackageManager().getResourcesForApplication(mPkgName);
		} catch (PackageManager.NameNotFoundException e) {
			Utils.logd(e.getMessage());
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
			mIconMap = parseXml(xml, cmpMap);
		} catch (XmlPullParserException e) {
			Utils.logd(e.getMessage());
		} catch (IOException e) {
			Utils.logd(e.getMessage());
		}

	}

	private HashMap<String, Integer> fallback(HashMap<String, Integer> cmpMap) {
		int found = 0;
		int required = cmpMap.size();
		HashMap<String, Integer> iconMap = new HashMap(required);

		//Fallback
		for(String className : cmpMap.keySet()) {
			if (!iconMap.containsKey(className)) {
				String resName= className.toLowerCase(Locale.US).replace(".", "_");
				Utils.logd("Look for icon for resource: R.drawable." + resName);
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
			Utils.logd(localIOException2.getMessage());
		}
		catch (XmlPullParserException localXmlPullParserException1)
		{
			Utils.logd(localXmlPullParserException1.getMessage());
		}
		return xml;
	}

	private HashMap<String, Integer> parseXml(XmlPullParser xml, HashMap<String, Integer> cmpMap) throws XmlPullParserException, IOException {

		int found = 0;
		int required = cmpMap.size();
		HashMap<String, Integer> iconMap = new HashMap(required);

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
						found++;
					}
				}
				if (found == required) {
					break;
				}
			}
			eventType = xml.next();
		}

		if (found == required) {
			return iconMap;
		}

		if (found == 0) {
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

	public Drawable getDrawable(int resId) {
		return mThemeResources.getDrawable(resId);
	}
}