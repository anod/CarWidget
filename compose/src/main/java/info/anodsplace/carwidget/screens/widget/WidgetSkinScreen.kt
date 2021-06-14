package info.anodsplace.carwidget.screens.widget

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun WidgetSkinPreview(skinItem: SkinList.Item, viewModel: SkinPreviewViewModel) {
    val view: View? by viewModel.load(skinItem).collectAsState(initial = null)
    if (view == null) {
        CircularProgressIndicator()
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
            factory = { view!! },
            update = {
                // View's been inflated or state read in this block has been updated
                // Add logic here if necessary

                // As selectedItem is read here, AndroidView will recompose
                // whenever the state changes
                // Example of Compose -> View communication
                // view.coordinator.selectedItem = selectedItem.value
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WidgetSkinScreen(skinList: SkinList, viewModel: SkinPreviewViewModel, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = skinList.count, initialPage = skinList.selectedSkinPosition)
    val scope = rememberCoroutineScope()

    pagerState.targetPage

    Column(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            skinList.titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.scrollToPage(page = index) }
                    },
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.padding(16.dp)) { page ->
            WidgetSkinPreview(skinList[page], viewModel = viewModel)
        }
    }
}