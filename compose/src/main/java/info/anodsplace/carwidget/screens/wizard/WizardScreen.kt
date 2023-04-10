package info.anodsplace.carwidget.screens.wizard

import android.text.Html
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.SettingsIcon
import info.anodsplace.carwidget.screens.main.MainViewEvent
import info.anodsplace.carwidget.screens.main.MainViewState
import info.anodsplace.carwidget.screens.widget.DummySkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.SkinList
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinPreview
import info.anodsplace.compose.toAnnotatedString
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WizardScreen(screenState: MainViewState, modifier: Modifier = Modifier, initialPage: Int = 0, onEvent: (event: MainViewEvent) -> Unit = { }) {
    val pagesCount = 4
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage)
    val currentPage = pagerState.currentPage
    val isLast = currentPage == 3

    Surface {
        Column(modifier = modifier.windowInsetsPadding(WindowInsets.systemBars)) {
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
                pageCount = pagesCount,
                state = pagerState,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.8f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(skinViewModel = DummySkinPreviewViewModel(LocalContext.current.applicationContext))
                    1 -> InstallPage()
                    2 -> ConfigPage()
                    3 -> InCarModePage(screenState)
                    else -> { }
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                if (!isLast) {
                    OutlinedButton(
                        onClick = { onEvent(MainViewEvent.CloseWizard) },
                    ) {
                        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.skip))
                    }
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(onClick = {
                    if (isLast) {
                        onEvent(MainViewEvent.CloseWizard)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                    }
                }) {
                    Text(text = if (isLast) stringResource(id = info.anodsplace.carwidget.content.R.string.finish) else stringResource(id = info.anodsplace.carwidget.content.R.string.next))
                }
            }
        }
    }
}

@Composable
fun WelcomePage(
    skinViewModel: SkinPreviewViewModel
) {
    val skinList = SkinList(skinViewModel.viewState.widgetSettings.skin, LocalContext.current)
    WizardPage(
        title = stringResource(id = info.anodsplace.carwidget.content.R.string.welcome),
        description = stringResource(id = info.anodsplace.carwidget.content.R.string.welcome_text).fromHtml(
            isEditMode = LocalView.current.isInEditMode,
            linkColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Text(
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.swipe_continue),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 32.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WidgetSkinPreview(skinList.current, skinViewFactory = skinViewModel)
        }
    }
}

@Composable
fun InstallPage() {
    WizardPage(
        title = stringResource(id = info.anodsplace.carwidget.content.R.string.install_a_widget),
        description = stringResource(id = info.anodsplace.carwidget.content.R.string.install_widget).fromHtml(
            isEditMode = LocalView.current.isInEditMode,
            linkColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Image(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            painter = painterResource(id = info.anodsplace.carwidget.skin.R.drawable.install_widget),
            contentDescription =  stringResource(id = info.anodsplace.carwidget.content.R.string.install_widget),
            alignment = Alignment.Center
        )
    }
}

@Composable
fun ConfigPage() {
    WizardPage(
        title = stringResource(id = info.anodsplace.carwidget.content.R.string.configure),
        description = stringResource(id = info.anodsplace.carwidget.content.R.string.configure_text).fromHtml(
            isEditMode = LocalView.current.isInEditMode,
            linkColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Image(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            painter = painterResource(id = info.anodsplace.carwidget.skin.R.drawable.settings_preview),
            contentDescription =  stringResource(id = info.anodsplace.carwidget.content.R.string.configure),
            alignment = Alignment.Center
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = {  }
        ) {
            SettingsIcon()
            Text(
                text = stringResource(id = info.anodsplace.carwidget.content.R.string.open_settings_description).fromHtml(
                    isEditMode = LocalView.current.isInEditMode,
                    linkColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun InCarModePage(
    screenState: MainViewState,
) {
    WizardPage(
        title = stringResource(id = info.anodsplace.carwidget.content.R.string.incar_mode),
        description = stringResource(id = info.anodsplace.carwidget.content.R.string.detect_incar_description).fromHtml(
            isEditMode = LocalView.current.isInEditMode,
            linkColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Text(
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.enable_incar_description).fromHtml(
                isEditMode = LocalView.current.isInEditMode,
                linkColor = MaterialTheme.colorScheme.primary
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 32.dp)
        )
        Row(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionIcon(
                iconRes = R.drawable.ic_action_bluetooth_connected,
                iconDescRes = info.anodsplace.carwidget.content.R.string.bluetooth
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_usb,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_power_connected_title
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_headphones,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_headset_connected_title
            )
        }
        Text(
            text = stringResource(id = info.anodsplace.carwidget.content.R.string.adjust_incar_description).fromHtml(
                isEditMode = LocalView.current.isInEditMode,
                linkColor = MaterialTheme.colorScheme.primary
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionIcon(
                iconRes = R.drawable.ic_action_brightness_medium,
                iconDescRes = info.anodsplace.carwidget.content.R.string.adjust_brightness
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_bulb,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_screen_timeout
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_ring_volume,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_auto_answer
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_volume_up,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_volume_level_summary
            )
            ActionIcon(
                iconRes = R.drawable.ic_action_bluetooth,
                iconDescRes = info.anodsplace.carwidget.content.R.string.pref_blutooth_device_title
            )
        }
    }
}

@Composable
private fun ActionIcon(@DrawableRes iconRes: Int, @StringRes iconDescRes: Int) {
    Icon(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(4.dp),
        painter = painterResource(id = iconRes),
        contentDescription = stringResource(id = iconDescRes),
        tint = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun WizardPage(
    title: String,
    description: AnnotatedString,
    content: @Composable () -> Unit
) {
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
            text = description,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        content()
    }
}

private fun String.fromHtml(isEditMode: Boolean, linkColor: Color): AnnotatedString {
    if (isEditMode) {
        return AnnotatedString(this)
    }
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toAnnotatedString(linkColor = linkColor)
}

@Preview
@Composable
fun PreviewScreen1() {
    CarWidgetTheme {
        WizardScreen(
            screenState = MainViewState(),
            initialPage = 0,
            onEvent = { })
    }
}

@Preview
@Composable
fun PreviewScreen2() {
    CarWidgetTheme {
        WizardScreen(
            screenState = MainViewState(),
            initialPage = 1,
            onEvent = { })
    }
}

@Preview
@Composable
fun PreviewScreen3() {
    CarWidgetTheme {
        WizardScreen(
            screenState = MainViewState(),
            initialPage = 2,
            onEvent = { })
    }
}


@Preview
@Composable
fun PreviewScreen4() {
    CarWidgetTheme {
        WizardScreen(
            screenState = MainViewState(),
            initialPage = 3,
            onEvent = { })
    }
}
