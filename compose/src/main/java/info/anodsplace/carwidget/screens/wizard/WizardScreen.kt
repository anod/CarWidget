package info.anodsplace.carwidget.screens.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinPreview
import info.anodsplace.compose.BackgroundSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WizardScreen(modifier: Modifier = Modifier) {
    val pagesCount = 4
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0)
    val currentPage = pagerState.currentPage

    Column(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = currentPage
        ) {
            (0..pagesCount).forEach { index ->
                val tabColor: Color = when {
                    (currentPage == index) -> colorResource(R.color.step_pager_selected_last_tab_color)
                    (currentPage - 1 == index) -> colorResource(R.color.step_pager_selected_last_tab_color)
                    (currentPage > index) -> colorResource(R.color.step_pager_next_tab_color)
                    else -> colorResource(R.color.step_pager_previous_tab_color)
                }

                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 3.dp)
                        .background(color = tabColor)
                        .clickable {
                            scope.launch {
                                pagerState.scrollToPage(page = index)
                            }
                        }
                ) {

                    }
            }
        }

        HorizontalPager(
            count = pagesCount,
            state = pagerState,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WizardWelcome()
                else -> { }
            }
        }

        Row {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Skip")
            }
            Spacer(modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                scope.launch {
                    pagerState.scrollToPage(currentPage + 1)
                }
            }) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
fun WizardWelcome() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = stringResource(id = R.string.welcome), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(id = R.string.welcome_text), style = MaterialTheme.typography.bodyLarge)
        Text(text = stringResource(id = R.string.swipe_continue), style = MaterialTheme.typography.bodySmall)

        val appContext = LocalContext.current.applicationContext
        val skinViewModel: SkinPreviewViewModel = viewModel(factory = SkinPreviewViewModel.Factory(appContext, null))
        WidgetSkinPreview(skinViewModel.skinList.current, skinViewModel)
    }
}

@Preview("Wizard Screen Light")
@Composable
fun PreviewWizard() {
    CarWidgetTheme {
        BackgroundSurface {
            WizardScreen()
        }
    }
}