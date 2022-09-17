package info.anodsplace.carwidget.screens.wizard

import android.text.Html
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.screens.main.MainViewEvent
import info.anodsplace.carwidget.screens.widget.DummySkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinPreview
import info.anodsplace.compose.toAnnotatedString
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WizardScreen(initialPage: Int = 0, modifier: Modifier = Modifier, onEvent: (event: MainViewEvent) -> Unit) {
    val pagesCount = 4
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage)
    val currentPage = pagerState.currentPage

    Surface {
        Column(modifier = modifier) {
            ScrollableTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = currentPage,
                edgePadding = 0.dp
            ) {
                (0 until pagesCount).forEach { index ->
                    val tabColor: Color = when {
                        (currentPage == index) -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }

                    Box(
                        modifier = Modifier
                            .height(height = 12.dp)
                            .width(IntrinsicSize.Max)
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
                    0 -> WelcomePage(
                        skinViewModel = DummySkinPreviewViewModel(LocalContext.current.applicationContext)
                    )
                    1 -> InstallPage()
                    2 -> ConfigPage()
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
fun WelcomePage(skinViewModel: SkinPreviewViewModel) {
    WizardPage(
        title = stringResource(id = R.string.welcome),
        description = stringResource(id = R.string.welcome_text)
    ) {
        Text(
            text = stringResource(id = R.string.swipe_continue),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 32.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WidgetSkinPreview(skinViewModel.viewState.currentSkin, skinViewModel.viewState.reload, skinViewFactory = skinViewModel)
        }
    }
}

@Composable
fun InstallPage() {
    WizardPage(
        title = stringResource(id = R.string.install_a_widget),
        description = stringResource(id = R.string.install_widget)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

        }
    }
}

@Composable
fun ConfigPage() {
    WizardPage(
        title = stringResource(id = R.string.configure),
        description = stringResource(id = R.string.configure_text)
    ) {
        Image(
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            painter = painterResource(id = R.drawable.settings_preview),
            contentDescription =  stringResource(id = R.string.configure),
            alignment = Alignment.Center
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = {  }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_holo_settings),
                contentDescription = null
            )
            val buttonText = stringResource(id = R.string.open_settings_description)
            val buttonTextAnnotated = remember(buttonText) {
                Html.fromHtml(buttonText, Html.FROM_HTML_MODE_COMPACT).toAnnotatedString()
            }
            Text(text = buttonTextAnnotated, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun WizardPage(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    val descAnnotated = remember(description) {
        Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT).toAnnotatedString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = descAnnotated,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        content()
    }
}

@Preview
@Composable
fun PreviewScreen1() {
    CarWidgetTheme {
        WizardScreen(initialPage = 0, onEvent = { })
    }
}

@Preview
@Composable
fun PreviewScreen2() {
    CarWidgetTheme {
        WizardScreen(initialPage = 1, onEvent = { })
    }
}

@Preview
@Composable
fun PreviewScreen3() {
    CarWidgetTheme {
        WizardScreen(initialPage = 2, onEvent = { })
    }
}
