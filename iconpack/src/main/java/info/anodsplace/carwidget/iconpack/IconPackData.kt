package info.anodsplace.carwidget.iconpack

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import info.anodsplace.carwidget.iconpack.R

data class IconCategory(val labelRes: Int, val icon: ImageVector)

val IconCategories: List<IconCategory> = listOf(
    IconCategory(R.string.icon_category_travel, Icons.Default.Flight),
    IconCategory(R.string.icon_category_audio, Icons.Default.MusicNote),
    IconCategory(R.string.icon_category_books, Icons.AutoMirrored.Filled.MenuBook),
    IconCategory(R.string.icon_category_authenticator, Icons.Default.VerifiedUser),
    IconCategory(R.string.icon_category_cars, Icons.Default.DirectionsCar),
    IconCategory(R.string.icon_category_camera, Icons.Default.PhotoCamera),
    IconCategory(R.string.icon_category_children, Icons.Default.ChildCare),
    IconCategory(R.string.icon_category_sleep, Icons.Default.Bedtime),
    IconCategory(R.string.icon_category_cloud_storage, Icons.Default.Cloud),
    IconCategory(R.string.icon_category_contacts, Icons.Default.Contacts),
    IconCategory(R.string.icon_category_comics, Icons.Default.AutoStories),
    IconCategory(R.string.icon_category_crypto, Icons.Default.MonetizationOn),
    IconCategory(R.string.icon_category_dating, Icons.Default.Favorite),
    IconCategory(R.string.icon_category_download, Icons.Default.FileDownload),
    IconCategory(R.string.icon_category_education, Icons.Default.School),
    IconCategory(R.string.icon_category_email, Icons.Default.Email),
    IconCategory(R.string.icon_category_files, Icons.Default.Folder),
    IconCategory(R.string.icon_category_finance, Icons.Default.AccountBalance),
    IconCategory(R.string.icon_category_fitness, Icons.Default.FitnessCenter),
    IconCategory(R.string.icon_category_food, Icons.Default.Fastfood),
    IconCategory(R.string.icon_category_health, Icons.Default.HealthAndSafety),
    IconCategory(R.string.icon_category_home, Icons.Default.Home),
    IconCategory(R.string.icon_category_humor, Icons.Default.EmojiEmotions),
    IconCategory(R.string.icon_category_image, Icons.Default.Image),
    IconCategory(R.string.icon_category_jobs, Icons.Default.Work),
    IconCategory(R.string.icon_category_language_education, Icons.Default.Translate),
    IconCategory(R.string.icon_category_maps, Icons.Default.Map),
    IconCategory(R.string.icon_category_movies, Icons.Default.Movie),
    IconCategory(R.string.icon_category_network_connection, Icons.Default.Wifi),
    IconCategory(R.string.icon_category_news, Icons.Default.Article),
    IconCategory(R.string.icon_category_office, Icons.Default.Apartment),
    IconCategory(R.string.icon_category_painting, Icons.Default.ColorLens),
    IconCategory(R.string.icon_category_password_manager, Icons.Default.Lock),
    IconCategory(R.string.icon_category_personal_assistant, Icons.Default.SupportAgent),
    IconCategory(R.string.icon_category_privacy_security, Icons.Default.Security),
    IconCategory(R.string.icon_category_programming, Icons.Default.Code),
    IconCategory(R.string.icon_category_public_transport, Icons.Default.DirectionsBus),
    IconCategory(R.string.icon_category_radio, Icons.Default.Radio),
    IconCategory(R.string.icon_category_religious_text, Icons.AutoMirrored.Filled.MenuBook),
    IconCategory(R.string.icon_category_remote_control, Icons.Default.SettingsRemote),
    IconCategory(R.string.icon_category_scanner, Icons.Default.DocumentScanner),
    IconCategory(R.string.icon_category_settings, Icons.Default.Settings),
    IconCategory(R.string.icon_category_shopping, Icons.Default.ShoppingCart),
    IconCategory(R.string.icon_category_social_media, Icons.Default.People),
    IconCategory(R.string.icon_category_sport, Icons.Default.SportsSoccer),
    IconCategory(R.string.icon_category_stickers, Icons.Default.Collections),
    IconCategory(R.string.icon_category_taxi, Icons.Default.LocalTaxi),
    IconCategory(R.string.icon_category_text_messaging, Icons.AutoMirrored.Filled.Message),
    IconCategory(R.string.icon_category_theme, Icons.Default.Palette),
    IconCategory(R.string.icon_category_tips, Icons.Default.TipsAndUpdates),
    IconCategory(R.string.icon_category_todo_list, Icons.AutoMirrored.Filled.ListAlt),
    IconCategory(R.string.icon_category_tools, Icons.Default.Build),
    IconCategory(R.string.icon_category_trading, Icons.AutoMirrored.Filled.ShowChart),
    IconCategory(R.string.icon_category_virtual_reality, Icons.Default.ViewInAr),
    IconCategory(R.string.icon_category_vpn, Icons.Default.VpnKey),
    IconCategory(R.string.icon_category_watch, Icons.Default.WatchLater),
    IconCategory(R.string.icon_category_weather, Icons.Default.WbSunny),
    IconCategory(R.string.icon_category_web_browser, Icons.Default.Public),
)

