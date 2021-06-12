package info.anodsplace.carwidget.screens.widget

import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
fun WidgetSkinPreview(skinItem: SkinList.Item) {

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WidgetSkinScreen(skinList: SkinList, viewModel: SkinPreviewViewModel) {
    val pagerState = rememberPagerState(pageCount = skinList.count, initialPage = skinList.selectedSkinPosition)

    TabRow(
        selectedTabIndex = pagerState.currentPage,
    ) {
        skinList.titles.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = {  },
            )
        }
    }

    HorizontalPager(state = pagerState) { page ->
        WidgetSkinPreview(skinList[page])
    }
}