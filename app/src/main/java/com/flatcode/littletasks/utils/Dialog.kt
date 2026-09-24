package com.flatcode.littletasks.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.DialogAboutAppBinding
import com.flatcode.littletasks.databinding.DialogCloseAppBinding
import com.flatcode.littletasks.databinding.DialogLogoutBinding
import com.flatcode.littletasks.ui.auth.AuthActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

fun Activity.closeApp() {
    if (isFinishing || isDestroyed) return

    val dialogBinding = DialogCloseAppBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.yes.setOnClickListener {
        finish()
    }

    dialogBinding.no.setOnClickListener {
        alertDialog.dismiss()
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Activity.dialogLogout() {
    if (isFinishing || isDestroyed) return

    val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.yes.setOnClickListener {
        FirebaseAuth.getInstance().signOut()
        openActivity<AuthActivity>(true)
        alertDialog.dismiss()
    }

    dialogBinding.no.setOnClickListener {
        alertDialog.dismiss()
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Activity.dialogAboutApp() {
    if (isFinishing || isDestroyed) return

    val dialogBinding = DialogAboutAppBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.website.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, DATA.WEBSITE.toUri())
        startActivity(intent)
    }

    dialogBinding.facebook.setOnClickListener {
        val fbAppIntent = Intent(Intent.ACTION_VIEW, "fb://profile/${DATA.FB_ID}".toUri()).apply {
            setPackage("com.facebook.katana")
        }
        val fbWebIntent = Intent(Intent.ACTION_VIEW, "https://facebook.com/${DATA.FB_ID}".toUri())

        try {
            startActivity(fbAppIntent)
        } catch (_: ActivityNotFoundException) {
            startActivity(fbWebIntent)
        } catch (_: Exception) {
            startActivity(fbWebIntent)
        }
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.dialogOptionDelete(database: String?, onDelete: () -> Unit) {
    val dialogBinding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.title.setText(R.string.do_you_want_to_delete_the)
    val baseTitle: String = dialogBinding.title.text.toString()

    dialogBinding.title.text = when (database) {
        DATA.CATEGORIES -> "$baseTitle Category?"
        DATA.OBJECTS -> "$baseTitle Object?"
        DATA.TASKS -> "$baseTitle Task?"
        DATA.PLANS -> "$baseTitle Plan?"
        else -> baseTitle
    }

    dialogBinding.yes.setOnClickListener {
        onDelete()
        alertDialog.dismiss()
    }
    dialogBinding.no.setOnClickListener { alertDialog.dismiss() }
    alertDialog.show()
}

fun Context.showMoreOptions(options: Array<String>, onOptionSelected: (Int) -> Unit) {
    MaterialAlertDialogBuilder(this).setTitle("Choose Options")
        .setItems(options) { _: DialogInterface?, which: Int ->
            onOptionSelected(which)
        }.show()
}