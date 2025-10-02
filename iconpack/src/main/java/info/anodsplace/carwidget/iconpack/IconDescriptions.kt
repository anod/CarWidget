package info.anodsplace.carwidget.iconpack

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class IconDescription(
    val labelRes: Int,
    val icon: ImageVector,
    @DrawableRes val iconRes: Int
)

val IconDescriptions: List<IconDescription> = listOf(
    IconDescription(R.string.icon_category_travel, Icons.Default.Flight, R.drawable.baseline_flight_24),
    IconDescription(R.string.icon_category_audio, Icons.Default.MusicNote, R.drawable.baseline_music_note_24),
    IconDescription(R.string.icon_category_books, Icons.AutoMirrored.Filled.MenuBook, R.drawable.baseline_menu_book_24),
    IconDescription(R.string.icon_category_authenticator, Icons.Default.VerifiedUser, R.drawable.baseline_verified_user_24),
    IconDescription(R.string.icon_category_cars, Icons.Default.DirectionsCar, R.drawable.baseline_directions_car_24),
    IconDescription(R.string.icon_category_camera, Icons.Default.PhotoCamera, R.drawable.baseline_photo_camera_24),
    IconDescription(R.string.icon_category_children, Icons.Default.ChildCare, R.drawable.baseline_child_care_24),
    IconDescription(R.string.icon_category_sleep, Icons.Default.Bedtime, R.drawable.baseline_bedtime_24),
    IconDescription(R.string.icon_category_cloud_storage, Icons.Default.Cloud, R.drawable.baseline_cloud_24),
    IconDescription(R.string.icon_category_contacts, Icons.Default.Contacts, R.drawable.baseline_contacts_24),
    IconDescription(R.string.icon_category_comics, Icons.Default.AutoStories, R.drawable.baseline_auto_stories_24),
    IconDescription(R.string.icon_category_crypto, Icons.Default.MonetizationOn, R.drawable.baseline_monetization_on_24),
    IconDescription(R.string.icon_category_dating, Icons.Default.Favorite, R.drawable.baseline_favorite_24),
    IconDescription(R.string.icon_category_download, Icons.Default.FileDownload, R.drawable.baseline_file_download_24),
    IconDescription(R.string.icon_category_education, Icons.Default.School, R.drawable.baseline_school_24),
    IconDescription(R.string.icon_category_email, Icons.Default.Email, R.drawable.baseline_email_24),
    IconDescription(R.string.icon_category_files, Icons.Default.Folder, R.drawable.baseline_folder_24),
    IconDescription(R.string.icon_category_finance, Icons.Default.AccountBalance, R.drawable.baseline_account_balance_24),
    IconDescription(R.string.icon_category_fitness, Icons.Default.FitnessCenter, R.drawable.baseline_fitness_center_24),
    IconDescription(R.string.icon_category_food, Icons.Default.Fastfood, R.drawable.baseline_fastfood_24),
    IconDescription(R.string.icon_category_health, Icons.Default.HealthAndSafety, R.drawable.baseline_health_and_safety_24),
    IconDescription(R.string.icon_category_home, Icons.Default.Home, R.drawable.baseline_home_24),
    IconDescription(R.string.icon_category_humor, Icons.Default.EmojiEmotions, R.drawable.baseline_emoji_emotions_24),
    IconDescription(R.string.icon_category_image, Icons.Default.Image, R.drawable.baseline_image_24),
    IconDescription(R.string.icon_category_jobs, Icons.Default.Work, R.drawable.baseline_work_24),
    IconDescription(R.string.icon_category_language_education, Icons.Default.Translate, R.drawable.baseline_translate_24),
    IconDescription(R.string.icon_category_maps, Icons.Default.Map, R.drawable.baseline_map_24),
    IconDescription(R.string.icon_category_movies, Icons.Default.Movie, R.drawable.baseline_movie_24),
    IconDescription(R.string.icon_category_network_connection, Icons.Default.Wifi, R.drawable.baseline_wifi_24),
    IconDescription(R.string.icon_category_news, Icons.Default.Article, R.drawable.baseline_article_24),
    IconDescription(R.string.icon_category_office, Icons.Default.Apartment, R.drawable.baseline_apartment_24),
    IconDescription(R.string.icon_category_painting, Icons.Default.ColorLens, R.drawable.baseline_color_lens_24),
    IconDescription(R.string.icon_category_password_manager, Icons.Default.Lock, R.drawable.baseline_lock_24),
    IconDescription(R.string.icon_category_personal_assistant, Icons.Default.SupportAgent, R.drawable.baseline_support_agent_24),
    IconDescription(R.string.icon_category_privacy_security, Icons.Default.Security, R.drawable.baseline_security_24),
    IconDescription(R.string.icon_category_programming, Icons.Default.Code, R.drawable.baseline_code_24),
    IconDescription(R.string.icon_category_public_transport, Icons.Default.DirectionsBus, R.drawable.baseline_directions_bus_24),
    IconDescription(R.string.icon_category_radio, Icons.Default.Radio, R.drawable.baseline_radio_24),
    IconDescription(R.string.icon_category_religious_text, Icons.AutoMirrored.Filled.MenuBook, R.drawable.baseline_menu_book_24),
    IconDescription(R.string.icon_category_remote_control, Icons.Default.SettingsRemote, R.drawable.baseline_settings_remote_24),
    IconDescription(R.string.icon_category_scanner, Icons.Default.DocumentScanner, R.drawable.baseline_document_scanner_24),
    IconDescription(R.string.icon_category_settings, Icons.Default.Settings, R.drawable.baseline_settings_24),
    IconDescription(R.string.icon_category_shopping, Icons.Default.ShoppingCart, R.drawable.baseline_shopping_cart_24),
    IconDescription(R.string.icon_category_social_media, Icons.Default.People, R.drawable.baseline_people_24),
    IconDescription(R.string.icon_category_sport, Icons.Default.SportsSoccer, R.drawable.baseline_sports_soccer_24),
    IconDescription(R.string.icon_category_stickers, Icons.Default.Collections, R.drawable.baseline_collections_24),
    IconDescription(R.string.icon_category_taxi, Icons.Default.LocalTaxi, R.drawable.baseline_local_taxi_24),
    IconDescription(R.string.icon_category_text_messaging, Icons.AutoMirrored.Filled.Message, R.drawable.baseline_message_24),
    IconDescription(R.string.icon_category_theme, Icons.Default.Palette, R.drawable.baseline_palette_24),
    IconDescription(R.string.icon_category_tips, Icons.Default.TipsAndUpdates, R.drawable.baseline_tips_and_updates_24),
    IconDescription(R.string.icon_category_todo_list, Icons.AutoMirrored.Filled.ListAlt, R.drawable.baseline_list_alt_24),
    IconDescription(R.string.icon_category_tools, Icons.Default.Build, R.drawable.baseline_build_24),
    IconDescription(R.string.icon_category_trading, Icons.AutoMirrored.Filled.ShowChart, R.drawable.baseline_show_chart_24),
    IconDescription(R.string.icon_category_virtual_reality, Icons.Default.ViewInAr, R.drawable.baseline_view_in_ar_24),
    IconDescription(R.string.icon_category_vpn, Icons.Default.VpnKey, R.drawable.baseline_vpn_key_24),
    IconDescription(R.string.icon_category_watch, Icons.Default.WatchLater, R.drawable.baseline_watch_later_24),
    IconDescription(R.string.icon_category_weather, Icons.Default.WbSunny, R.drawable.baseline_wb_sunny_24),
    IconDescription(R.string.icon_category_web_browser, Icons.Default.Public, R.drawable.baseline_public_24),
)
