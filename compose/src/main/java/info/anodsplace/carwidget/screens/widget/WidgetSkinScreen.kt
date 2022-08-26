package info.anodsplace.carwidget.screens.widget

import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun WidgetSkinPreview(skinItem: SkinList.Item, reload: Int, skinViewFactory: SkinViewFactory) {
    val view: View? by produceState<View?>(initialValue = null, skinItem, reload) {
        val view = skinViewFactory.create(skinItem)
        value = view
    }
    if (view == null) {
        CircularProgressIndicator()
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
            factory = { context ->
                FrameLayout(context).apply {
                    addView(view)
                }
            },
            update = { frameLayout ->
                frameLayout.removeAllViews()
                frameLayout.addView(view)
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WidgetSkinScreen(screenState: SkinPreviewViewState, modifier: Modifier = Modifier, skinViewFactory: SkinViewFactory, onEvent: (SkinPreviewViewEvent) -> Unit = { }) {
    val pagerState = rememberPagerState(initialPage = screenState.skinList.selectedSkinPosition)
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(pagerState.currentPage) }

    if (currentPage != pagerState.currentPage) {
        SideEffect {
            onEvent(SkinPreviewViewEvent.UpdateCurrentSkin(index = currentPage))
        }
        currentPage = pagerState.currentPage
    }

    Column(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            screenState.skinList.titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.scrollToPage(page = index) }
                    }
                )
            }
        }

        HorizontalPager(count = screenState.skinList.count, state = pagerState, modifier = Modifier.padding(16.dp)) { page ->
            WidgetSkinPreview(skinItem = screenState.skinList[page], reload = screenState.reload, skinViewFactory = skinViewFactory)
        }
    }
}