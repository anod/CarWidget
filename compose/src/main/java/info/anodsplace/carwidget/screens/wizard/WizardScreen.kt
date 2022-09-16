package info.anodsplace.carwidget.screens.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import info.anodsplace.carwidget.screens.main.MainViewEvent
import info.anodsplace.carwidget.screens.widget.DummySkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinPreview
import info.anodsplace.carwidget.skin.YouProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WizardScreen(modifier: Modifier = Modifier, onEvent: (event: MainViewEvent) -> Unit) {
    val pagesCount = 4
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0)
    val currentPage = pagerState.currentPage

    Surface {
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
                            },
                        content = { }
                    )
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
                    0 -> WizardWelcome(
                        skinViewModel = DummySkinPreviewViewModel(LocalContext.current.applicationContext, YouProperties())
                    )
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
}

@Composable
fun WizardWelcome(skinViewModel: SkinPreviewViewModel) {

    Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(id = R.string.welcome),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = stringResource(id = R.string.welcome_text),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.swipe_continue),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 32.dp)
            )

            WidgetSkinPreview(skinViewModel.viewState.currentSkin, skinViewModel.viewState.reload, skinViewFactory = skinViewModel)
        }
}

@Preview("Wizard Screen Light")
@Composable
fun PreviewWizard() {
    CarWidgetTheme {
        WizardScreen(onEvent = { })
    }
}