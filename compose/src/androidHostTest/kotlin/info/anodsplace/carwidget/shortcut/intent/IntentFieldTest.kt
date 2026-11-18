package info.anodsplace.carwidget.shortcut.intent

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntentPolicy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class IntentFieldTest {
    @Test
    fun editingIntentFieldsPreservesUneditedFieldsAndOriginalIntent() {
        val original = Intent("before")
            .setDataAndType(Uri.parse("https://example.com"), "text/plain")
            .addCategory(Intent.CATEGORY_DEFAULT)
            .putExtra("obsolete", true)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val editedAction = updateIntentField(IntentField.Action("after", "Action"), original)
        val editedMimeType = updateIntentField(IntentField.MimeType("image/png", "MIME type"), original)
        val editedData = updateIntentField(IntentField.Data(Uri.parse("content://example/item"), "Data"), original)
        val editedCategories = updateIntentField(
            IntentField.Categories(setOf("example.category")),
            original
        )
        val editedFlags = updateIntentField(IntentField.Flags(Intent.FLAG_ACTIVITY_CLEAR_TOP), original)
        val editedExtras = updateIntentField(IntentField.Extras(Bundle().apply { putString("current", "value") }), original)
        val editedComponent = updateIntentField(
            IntentField.Component(ComponentName("com.example.app", "com.example.app.MainActivity")),
            original
        )

        assertEquals("before", original.action)
        assertEquals(Intent.CATEGORY_DEFAULT, original.categories?.single())
        assertEquals(true, original.getBooleanExtra("obsolete", false))
        assertEquals("after", editedAction.action)
        assertEquals(Uri.parse("https://example.com"), editedMimeType.data)
        assertEquals("image/png", editedMimeType.type)
        assertEquals(Uri.parse("content://example/item"), editedData.data)
        assertEquals("text/plain", editedData.type)
        assertEquals(setOf("example.category"), editedCategories.categories)
        assertEquals(Intent.FLAG_ACTIVITY_CLEAR_TOP, editedFlags.flags)
        assertFalse(editedExtras.hasExtra("obsolete"))
        assertEquals("value", editedExtras.getStringExtra("current"))
        assertEquals("com.example.app.MainActivity", editedComponent.component?.className)
    }

    @Test
    fun extrasEditorUsesADraftAndEmptyValuesClearOptionalFields() {
        val sourceExtras = Bundle().apply { putString("existing", "value") }
        val draft = IntentField.Extras(sourceExtras)
        draft.bundle.remove("existing")

        val original = Intent("before").putExtras(sourceExtras)
        val edited = updateIntentField(IntentField.Extras(draft.bundle), original)
        val clearedAction = updateIntentField(IntentField.Action("", "Action"), original)
        val clearedData = updateIntentField(IntentField.Data(Uri.parse(""), "Data"), original)

        assertTrue(sourceExtras.containsKey("existing"))
        assertTrue(original.hasExtra("existing"))
        assertFalse(edited.hasExtra("existing"))
        assertEquals("before", original.action)
        assertNull(clearedAction.action)
        assertNull(clearedData.data)
    }

    @Test
    fun invalidMimeTypeAndPartialComponentAreRejected() {
        assertFalse(IntentField.MimeType("not a mime type", "MIME type").isValid)
        assertFalse(IntentField.MimeType("image/", "MIME type").isValid)
        assertFalse(IntentField.Component(ComponentName("com.example.app", "")).isValid)
        assertTrue(IntentField.Component(ComponentName("com.example.app", "com.example.app.MainActivity")).isValid)
    }

    @Test
    fun actionAndMimeEditsAcceptOnlyAllowedValuesOrAnEmptyField() {
        assertTrue(IntentField.Action(Intent.ACTION_VIEW, "Action").isValid)
        assertFalse(IntentField.Action(Intent.ACTION_ANSWER, "Action").isValid)
        assertTrue(IntentField.Action("", "Action").isValid)

        assertTrue(IntentField.MimeType("vnd.android.cursor.item/phone_v2", "MIME type").isValid)
        assertFalse(IntentField.MimeType("image/png", "MIME type").isValid)
        assertTrue(IntentField.MimeType("", "MIME type").isValid)
    }

    @Test
    fun categoryAndFlagEditorsOfferExactlyTheLauncherOptions() {
        assertEquals(ShortcutIntentPolicy.categories, editableIntentCategories.values.toSet())
        assertEquals(ShortcutIntentPolicy.flags, editableIntentFlags.values.toSet())
        assertFalse(Intent.CATEGORY_HOME in editableIntentCategories.values)
        assertFalse(Intent.FLAG_GRANT_READ_URI_PERMISSION in editableIntentFlags.values)
    }

    @Test
    fun editingAnotherFieldPreservesLegacyActionAndMimeType() {
        val original = Intent(Intent.ACTION_ANSWER).setType("image/png")
        val edited = updateIntentField(IntentField.Flags(Intent.FLAG_ACTIVITY_NEW_TASK), original)

        assertEquals(Intent.ACTION_ANSWER, edited.action)
        assertEquals("image/png", edited.type)
    }

    @Test
    fun categoryCheckboxUsesFullAndroidNameAndPreservesCustomCategories() {
        val custom = "com.example.category.CUSTOM"
        val original = setOf(Intent.CATEGORY_BROWSABLE, custom)

        val added = updateIntentCategories(original, "DEFAULT", checked = true)
        assertEquals(setOf(Intent.CATEGORY_DEFAULT, Intent.CATEGORY_BROWSABLE, custom), added)
        assertEquals(original, updateIntentCategories(added, "DEFAULT", checked = false))
    }

    @Test
    fun flagCheckboxDoesNotClearOtherActivityFlagsOrUriGrantBits() {
        val original = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

        val added = updateIntentFlags(original, "ACTIVITY_CLEAR_TOP", checked = true)
        assertEquals(original or Intent.FLAG_ACTIVITY_CLEAR_TOP, added)
        assertEquals(original, updateIntentFlags(added, "ACTIVITY_CLEAR_TOP", checked = false))
    }
}
