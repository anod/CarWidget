package info.anodsplace.carwidget.chooser

import android.content.Context
import android.content.Intent
import info.anodsplace.compose.chooser.QueryIntentChooserLoader
import info.anodsplace.framework.content.forLauncher

class AllAppsIntentChooserLoader(context: Context) : QueryIntentChooserLoader(context, Intent().forLauncher())
