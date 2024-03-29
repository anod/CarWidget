package info.anodsplace.carwidget.content

/**
 * @author alex
 * @date 5/25/13
 */

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.applog.AppLog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.*

class IconTheme(private var context: Context, packageName: String) {

    var packageName: String
        internal set

    private var themeResources: Resources? = null

    private var iconMap: androidx.collection.SimpleArrayMap<String, Int>? = null

    init {
        this.packageName = packageName
    }

    fun loadThemeResources(): Boolean {
        try {
            themeResources = context.packageManager.getResourcesForApplication(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.w(e.message ?: "theme package not found")
        }

        return themeResources != null
    }

    fun loadFromXml(cmpMap: androidx.collection.SimpleArrayMap<String, Int>) {
        val xml = loadXmlResource()
        if (xml == null) {
            iconMap = fallback(cmpMap)
            return
        }
        try {
            synchronized(sLock) {
                iconMap = parseXml(xml, cmpMap)
            }
        } catch (e: XmlPullParserException) {
            AppLog.e(e)
        } catch (e: IOException) {
            AppLog.e(e)
        }

    }

    private fun fallback(cmpMap: androidx.collection.SimpleArrayMap<String, Int>): androidx.collection.SimpleArrayMap<String, Int>? {
        var found = 0
        val required = cmpMap.size()
        val iconMap = androidx.collection.SimpleArrayMap<String, Int>(required)

        //Fallback
        for (i in 0 until cmpMap.size()) {
            val className = cmpMap.keyAt(i)
            if (!iconMap.containsKey(className)) {
                val resName = className.lowercase(Locale.US).replace(".", "_")
                AppLog.d("Look for icon for resource: R.drawable.$resName")
                val resourceId = themeResources!!.getIdentifier(resName, "drawable", packageName)
                if (resourceId != 0) {
                    iconMap.put(className, resourceId)
                    found++
                }
            }
        }

        return if (found == 0) {
            null
        } else iconMap
    }

    private fun loadXmlResource(): XmlPullParser? {
        var xml: XmlPullParser? = null
        try {
            val appFilterId = themeResources!!
                    .getIdentifier(FILE_APPFILTER_NAME, FILE_APPFILTER_TYPE, packageName)
            if (appFilterId != 0) {
                xml = themeResources!!.getXml(appFilterId)
            } else {
                val localInputStream = themeResources!!.assets
                        .open("$FILE_APPFILTER_NAME.$FILE_APPFILTER_TYPE")
                val localXmlPullParserFactory = XmlPullParserFactory.newInstance()
                localXmlPullParserFactory.isNamespaceAware = true
                xml = localXmlPullParserFactory.newPullParser()
                xml!!.setInput(localInputStream, "UTF-8")
            }
        } catch (e: IOException) {
            AppLog.e(e)
        } catch (e: XmlPullParserException) {
            AppLog.e(e)
        }

        return xml
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseXml(xml: XmlPullParser, cmpMap: androidx.collection.SimpleArrayMap<String, Int>): androidx.collection.SimpleArrayMap<String, Int>? {

        val required = cmpMap.size()
        val iconMap = androidx.collection.SimpleArrayMap<String, Int>(required)

        var eventType = xml.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && TAG_ITEM == xml.name) {
                val component = xml.getAttributeValue(null, ATTR_COMPONENT)
                val drawable = xml.getAttributeValue(null, ATTR_DRAWABLE)
                var componentName: ComponentName? = null
                if (component != null && component.startsWith("ComponentInfo{")) {
                    componentName = ComponentName.unflattenFromString(component
                            .substring("ComponentInfo{".length, -1 + component.length))
                }
                if (componentName != null && cmpMap.containsKey(componentName.className)) {
                    val drawableId = themeResources!!.getIdentifier(drawable, "drawable", packageName)
                    if (drawableId != 0) {
                        iconMap.put(componentName.className, drawableId)
                    }
                }
                if (iconMap.size() == required) {
                    break
                }
            }
            eventType = xml.next()
        }

        if (iconMap.size() == required) {
            return iconMap
        }

        return if (iconMap.size() == 0) {
            null
        } else iconMap
    }

    fun getIcon(className: String): Int {
        val iconMap = this.iconMap ?: return 0
        return iconMap.get(className) ?: 0
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ResourcesCompat.getDrawableForDensity(themeResources!!, resId, UtilitiesBitmap.getTargetDensity(context), null)
    }

    companion object {

        private const val ATTR_COMPONENT = "component"
        private const val ATTR_DRAWABLE = "drawable"
        private const val TAG_ITEM = "item"
        private const val FILE_APPFILTER_NAME = "appfilter"
        private const val FILE_APPFILTER_TYPE = "xml"

        val sLock = Any()
    }
}