package de.yogaknete.app.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import de.yogaknete.app.R

/**
 * Custom icons for YogaKnete app
 */
object YogaIcons {
    // App branding
    @DrawableRes val AppLogo = R.drawable.yoga_knete_logo
    @DrawableRes val AppIcon = R.drawable.ic_launcher_foreground_yoga
    
    // Class status icons
    @DrawableRes val ClassCompleted = R.drawable.ic_class_completed
    @DrawableRes val ClassCancelled = R.drawable.ic_class_cancelled
    @DrawableRes val ClassPending = R.drawable.ic_class_pending
    
    // Navigation icons
    @DrawableRes val AddClass = R.drawable.ic_add_class
    @DrawableRes val Studio = R.drawable.ic_studio
    @DrawableRes val Calendar = R.drawable.ic_calendar
    @DrawableRes val Invoice = R.drawable.ic_invoice
    @DrawableRes val YogaPose = R.drawable.ic_yoga_pose
    
    // Material icons (for common actions)
    val Add: ImageVector = Icons.Default.Add
    val Edit: ImageVector = Icons.Default.Edit
    val Delete: ImageVector = Icons.Default.Delete
    val Share: ImageVector = Icons.Default.Share
    val Settings: ImageVector = Icons.Default.Settings
}
