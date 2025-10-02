package info.anodsplace.carwidget.appwidget

import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.main.MainViewEvent
import kotlinx.coroutines.launch

@Composable
fun WidgetSkinPreview(skinItem: SkinList.Item, skinViewFactory: SkinViewFactory, reload: Int = 0) {
    val view: View? by produceState<View?>(initialValue = null, skinItem, reload) {
        val view = skinViewFactory.create(skinItem)
        value = view
    }
    if (view == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetSkinScreen(
    screenState: SkinPreviewViewState,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    skinViewFactory: SkinViewFactory,
    onMainEvent: (MainViewEvent) -> Unit = { },
    windowSizeClass: WindowSizeClass
) {
    val skinList = screenState.skinList
    val pagerState = rememberPagerState(initialPage = skinList.selectedSkinPosition, pageCount = { skinList.count })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        // Collect from the pager state a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            if (currentPage != skinList.selectedSkinPosition) {
                onMainEvent(MainViewEvent.WidgetUpdateSkin(skinIdx = currentPage))
            }
        }
    }

    LaunchedEffect(skinList) {
        if (pagerState.currentPage != skinList.selectedSkinPosition) {
            pagerState.scrollToPage(skinList.selectedSkinPosition)
        }
    }

    val isCompact = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column {
            if (!isCompact) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage
                ) {
                    skinList.titles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.scrollToPage(page = index) }
                            }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(16.dp)
            ) { page ->
                WidgetSkinPreview(
                    skinItem = skinList[page],
                    reload = screenState.previewVersion,
                    skinViewFactory = skinViewFactory
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun PreviewWidgetSkinScreen() {
    val context = LocalContext.current
    CarWidgetTheme {
        Surface {
            WidgetSkinScreen(
                screenState = SkinPreviewViewState(

                ),
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp)),
                skinViewFactory = DummySkinPreviewViewModel(context),
                onMainEvent = { }
            )
        }
    }
}