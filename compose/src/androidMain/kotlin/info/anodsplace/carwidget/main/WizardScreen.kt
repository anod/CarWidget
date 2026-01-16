package info.anodsplace.carwidget.main

import android.text.Html
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.compose.toAnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScreen(onEvent: (event: MainViewEvent) -> Unit = { }) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = { Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.app_name)) },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = R.drawable.ic_launcher_48),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = stringResource(id = info.anodsplace.carwidget.content.R.string.install_a_widget),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = stringResource(id = info.anodsplace.carwidget.content.R.string.install_widget).fromHtml(
                        isEditMode = LocalView.current.isInEditMode,
                        linkColor = MaterialTheme.colorScheme.primary
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp),
                    inlineContent = mapOf(
                        "Icons.Default.Widgets" to InlineTextContent(
                            placeholder = Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter),
                            children = {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        ),
                        "Icons.Default.TouchApp" to InlineTextContent(
                            placeholder = Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter),
                            children = {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        ),
                    )
                )
                Image(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    painter = painterResource(id = info.anodsplace.carwidget.skin.R.drawable.install_widget),
                    contentDescription = stringResource(id = info.anodsplace.carwidget.content.R.string.install_widget),
                    alignment = Alignment.Center
                )
                Spacer(modifier = Modifier.weight(1.0f))

                Row {
                    FilledTonalButton(
                        onClick = { onEvent(MainViewEvent.GotToHomeScreen) },
                    ) {
                        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.go_to_homescreen))
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Button(
                        onClick = { onEvent(MainViewEvent.CloseWizard) }
                    ) {
                        Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.close))
                    }
                }
            }
        }
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
fun PreviewWizard() {
    CarWidgetTheme {
        WizardScreen(onEvent = { })
    }
}
