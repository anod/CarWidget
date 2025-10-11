package info.anodsplace.carwidget.chooser

import android.content.ComponentName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CompositeChooserLoaderTest {
    @Test
    fun duplicatesPreferEarlierLoaderAndSourceIndexSet() = runBlocking {
        val entry1a = ChooserEntry(componentName = ComponentName("pkg.one", "ActivityA"), title = "Activity A")
        val entry1b = ChooserEntry(componentName = ComponentName("pkg.one", "ActivityB"), title = "Activity B")
        val entry2aDuplicate = ChooserEntry(componentName = ComponentName("pkg.one", "ActivityA"), title = "Activity A (Alt)")
        val entry2c = ChooserEntry(componentName = ComponentName("pkg.two", "ActivityC"), title = "Activity C")
        val loader1 = StaticChooserLoader(listOf(entry1a, entry1b))
        val loader2 = StaticChooserLoader(listOf(entry2aDuplicate, entry2c))

        val composite = CompositeChooserLoader(listOf(loader1, loader2))
        val result = composite.load().first()

        // Expect 3 unique entries (A,B,C) with titles from first occurrence for duplicates
        assertEquals(3, result.size, "Expected 3 unique entries")
        val byComponent = result.associateBy { it.componentName!!.flattenToShortString() }
        assertTrue(byComponent.containsKey("pkg.one/ActivityA"))
        assertTrue(byComponent.containsKey("pkg.one/ActivityB"))
        assertTrue(byComponent.containsKey("pkg.two/ActivityC"))
        // Duplicate kept first title
        assertEquals("Activity A", byComponent.getValue("pkg.one/ActivityA").title)
        // Source loader indices
        assertEquals(0, byComponent.getValue("pkg.one/ActivityA").sourceLoader)
        assertEquals(0, byComponent.getValue("pkg.one/ActivityB").sourceLoader)
        assertEquals(1, byComponent.getValue("pkg.two/ActivityC").sourceLoader)
    }

    @Test
    fun duplicatesWithoutComponentResolvedByTitle() = runBlocking {
        val entry1 = ChooserEntry(componentName = null, title = "Title Only")
        val entry2Duplicate = ChooserEntry(componentName = null, title = "Title Only")
        val entry3 = ChooserEntry(componentName = null, title = "Other Title")
        val loader1 = StaticChooserLoader(listOf(entry1))
        val loader2 = StaticChooserLoader(listOf(entry2Duplicate, entry3))
        val composite = CompositeChooserLoader(listOf(loader1, loader2))
        val result = composite.load().first()
        assertEquals(2, result.size)
        val titles = result.associateBy { it.title }
        assertTrue(titles.containsKey("Title Only"))
        assertTrue(titles.containsKey("Other Title"))
        assertEquals(0, titles.getValue("Title Only").sourceLoader)
        assertEquals(1, titles.getValue("Other Title").sourceLoader)
    }
}
