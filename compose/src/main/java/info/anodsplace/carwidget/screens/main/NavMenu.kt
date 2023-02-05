package info.anodsplace.carwidget.screens.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import info.anodsplace.carwidget.screens.NavItem

fun NavHostController.navigate(item: NavItem.Tab) {
    navigate(item.route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = false
    }
}

@Composable
fun BottomTabsMenu(items: List<NavItem.Tab>, currentRoute: String?, onClick: (NavItem.Tab) -> Unit) {
    NavigationBar {
        items.forEachIndexed { _, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(id = item.resourceId),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute?.startsWith(item.route) == true,
                onClick = {
                    onClick(item)
                }
            )
        }
    }
}

@Composable
fun NavRailMenu(
    items: List<NavItem.Tab>,
    showApply: Boolean,
    currentRoute: String?,
    onClick: (NavItem.Tab) -> Unit,
    onApply: () -> Unit,
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 3.0.dp
    ) {
        NavigationRail(
            header = {
                if (showApply) {
                    AppBarButton(image = Icons.Filled.Check, descRes = android.R.string.ok, onClick = onApply)
                }
            },
            windowInsets = windowInsets
        ) {

            Spacer(Modifier.weight(1f))
            items.forEachIndexed { _, item ->
                NavigationRailItem(
                    selected = currentRoute?.startsWith(item.route) == true,
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = {
                        Text(
                            text = stringResource(id = item.resourceId),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    },
                    alwaysShowLabel = false,
                    onClick = {
                        onClick(item)
                    }
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }

}