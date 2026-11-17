package info.anodsplace.carwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class WidgetConfigResultTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Test
    fun dismissReturnsCanceledWithWidgetIdAndApplyOverridesIt() {
        activity.setWidgetConfigResult(Activity.RESULT_CANCELED, 42)

        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
        assertEquals(42, shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))

        activity.setWidgetConfigResult(Activity.RESULT_OK, 42)

        assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
        assertEquals(42, shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
    }

    @Test
    fun invalidWidgetIdLeavesResultUnset() {
        activity.setWidgetConfigResult(Activity.RESULT_CANCELED, AppWidgetManager.INVALID_APPWIDGET_ID)

        assertNull(shadowOf(activity).resultIntent)
    }
}
