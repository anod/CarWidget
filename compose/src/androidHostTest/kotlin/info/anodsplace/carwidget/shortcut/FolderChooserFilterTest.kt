package info.anodsplace.carwidget.shortcut

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import info.anodsplace.compose.chooser.ChooserEntry
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class FolderChooserFilterTest {
    private val game = app("Game", ApplicationInfo.CATEGORY_GAME)
    private val music = app("Music", ApplicationInfo.CATEGORY_AUDIO)
    private val maps = app("Maps", ApplicationInfo.CATEGORY_MAPS)
    private val legacyShortcut = ChooserEntry(ComponentName("pkg.legacy", "Legacy"), "Legacy").apply { sourceLoader = 1 }
    private val all = listOf(game, legacyShortcut, maps, music)

    @Test
    fun selectedEntriesIncludeItemsHiddenByFilterAndSearch() {
        val selected = setOf(game.componentName!!, music.componentName!!, legacyShortcut.componentName!!)

        val visible = filterFolderEntries(all, CategoryFilterState.Category(ApplicationInfo.CATEGORY_MAPS), selected, "map")
        assertEquals(listOf(maps), visible)

        assertEquals(listOf(game, legacyShortcut, music), selectedFolderEntries(all, selected))
    }

    @Test
    fun selectedTabKeepsSnapshotEntriesVisibleAfterUncheck() {
        val snapshot = setOf(game.componentName!!, music.componentName!!)

        val visible = filterFolderEntries(all, CategoryFilterState.Selected, snapshot, "")

        assertEquals(listOf(game, legacyShortcut, music), visible)
        assertEquals(listOf(game), selectedFolderEntries(all, setOf(game.componentName!!)))
    }

    private fun app(title: String, category: Int) =
        ChooserEntry(ComponentName("pkg.$title", title), title, category).apply { sourceLoader = 0 }
}
