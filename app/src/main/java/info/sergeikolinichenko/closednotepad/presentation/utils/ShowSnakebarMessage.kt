package info.sergeikolinichenko.closednotepad.presentation.utils

import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R

/** Created by Sergei Kolinichenko on 09.12.2022 at 12:50 (GMT+3) **/

fun showSnakebar(
    viewContainer: View,
    anchorObject: View,
    isNight: Boolean,
    message: String
) {
    val icon = if (isNight) R.drawable.ic_information_variant_black_48dp
    else R.drawable.ic_information_variant_white_48dp

    val snackBar = Snackbar.make(
        viewContainer,
        message,
        Snackbar.LENGTH_LONG
    )

    val snackBarView = snackBar.view
    val snackBarText = snackBarView.findViewById<TextView>(
        com.google.android.material.R.id.snackbar_text
    )
    snackBarText.setCompoundDrawablesWithIntrinsicBounds(
        icon, 0, 0, 0
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        snackBarText.setAutoSizeTextTypeWithDefaults(
            TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
    }
    snackBarText.compoundDrawablePadding = 5
    snackBarText.gravity = Gravity.CENTER
    snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
    snackBar.anchorView = anchorObject
    snackBar.show()
}

fun showSnakebar(
    viewContainer: View,
    anchorObject: View,
    isNight: Boolean,
    message: String,
    textButton: Int,
    action: () -> Unit,
    cancel: () -> Unit
) {
    val icon = if (isNight) R.drawable.ic_map_marker_question_outline_black_48dp
    else R.drawable.ic_map_marker_question_outline_white_48dp

    val snackBar = Snackbar.make(
        viewContainer,
        message,
        Snackbar.LENGTH_LONG
    )
        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                cancel()
            }
        })
        .setAction(textButton) {
            action()
        }
    val snackBarView = snackBar.view
    val snackBarText = snackBarView.findViewById<TextView>(
        com.google.android.material.R.id.snackbar_text
    )
    snackBarText.setCompoundDrawablesWithIntrinsicBounds(
        icon, 0, 0, 0
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        snackBarText.setAutoSizeTextTypeWithDefaults(
            TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
    }
    snackBarText.compoundDrawablePadding = 5
    snackBarText.gravity = Gravity.CENTER
    snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
    snackBar.anchorView = anchorObject
    snackBar.show()
}