package com.flatcode.littletasks.core.utils

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.DialogAboutAppBinding
import com.flatcode.littletasks.databinding.DialogCloseAppBinding
import com.flatcode.littletasks.databinding.DialogLogoutBinding
import com.flatcode.littletasks.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.MessageFormat

// --- Activity Extensions ---

fun Activity.closeApp() {
    if (this.isFinishing || this.isDestroyed) return

    val binding = DialogCloseAppBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.let { window ->
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.yes.setOnClickListener {
        this.finish()
    }

    binding.no.setOnClickListener {
        dialog.cancel()
    }

    dialog.show()
}

fun Activity.dialogLogout() {
    if (this.isFinishing || this.isDestroyed) return

    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.let { window ->
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.yes.setOnClickListener {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this@dialogLogout, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        this@dialogLogout.startActivity(intent)

        dialog.dismiss()
    }

    binding.no.setOnClickListener {
        dialog.cancel()
    }

    dialog.show()
}

fun Activity.dialogAboutApp() {
    if (this.isFinishing || this.isDestroyed) return

    val binding = DialogAboutAppBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.let { window ->
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.website.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEBSITE))
        this@dialogAboutApp.startActivity(intent)
    }

    binding.facebook.setOnClickListener {
        val facebookUri = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this@dialogAboutApp.packageManager.getPackageInfo(
                    "com.facebook.katana",
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                this@dialogAboutApp.packageManager.getPackageInfo("com.facebook.katana", 0)
            }
            "fb://profile/${DATA.FB_ID}"
        } catch (_: Exception) {
            "https://facebook.com${DATA.FB_ID}"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUri))
        this@dialogAboutApp.startActivity(intent)
    }

    dialog.show()
}

fun Activity.startCropImageSquare() {
    CropImage.activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE)
        .setAspectRatio(1, 1)
        .setCropShape(CropImageView.CropShape.OVAL)
        .start(this)
}

fun Activity.startCropImageWide() {
    CropImage.activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE)
        .setAspectRatio(2, 1)
        .setCropShape(CropImageView.CropShape.OVAL)
        .start(this)
}

// --- Context Extensions ---

fun Context.openActivityAndClear(c: Class<*>) {
    val intent = Intent(this, c)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    this.startActivity(intent)
}

fun Context.openActivity(c: Class<*>, vararg extras: Pair<String, String?>) {
    val intent = Intent(this, c)
    extras.forEach { intent.putExtra(it.first, it.second) }
    this.startActivity(intent)
}

fun Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "share app")
        putExtra(
            Intent.EXTRA_TEXT,
            "Download the app now from Google Play: https://google.com${this@shareApp.packageName}"
        )
    }
    this.startActivity(Intent.createChooser(shareIntent, "Choose how to share"))
}

fun Context.rateApp() {
    val packageName = this.packageName
    val marketUri = Uri.parse("market://details?id=$packageName")
    val webUri = Uri.parse("https://google.com")

    try {
        this.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
    } catch (_: ActivityNotFoundException) {
        this.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

fun Context.dialogOptionDelete(database: String?, id: String?, name: String, onDelete: () -> Unit) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_logout)
    dialog.setCancelable(true)
    dialog.window?.let { window ->
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    val title: TextView = dialog.findViewById(R.id.title)
    title.setText(R.string.do_you_want_to_delete_the)
    val baseTitle: String = title.text.toString()

    title.text = when (database) {
        DATA.CATEGORIES -> MessageFormat.format("{0} Category?", baseTitle)
        DATA.OBJECTS -> MessageFormat.format("{0} Object?", baseTitle)
        DATA.TASKS -> MessageFormat.format("{0} Task?", baseTitle)
        DATA.PLANS -> MessageFormat.format("{0} Plan?", baseTitle)
        else -> baseTitle
    }

    dialog.findViewById<View>(R.id.yes).setOnClickListener {
        onDelete()
        dialog.dismiss()
    }
    dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
    dialog.show()
}

fun Context.showMoreOptions(options: Array<String>, onOptionSelected: (Int) -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Choose Options")
        .setItems(options) { _: DialogInterface?, which: Int ->
            onOptionSelected(which)
        }.show()
}

// --- ImageView Extensions ---

fun ImageView.loadImage(isUser: Boolean, url: String?) {
    try {
        if (url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(url) {
                placeholder(R.color.image_profile)
                crossfade(true)
            }
        }
    } catch (_: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.loadBlurImage(isUser: Boolean, url: String, level: Int) {
    try {
        if (url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(url) {
                placeholder(R.color.image_profile)
                transformations(SimpleBlurTransformation(level.toFloat()))
            }
        }
    } catch (_: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

// --- Other Extensions ---

fun Int.levelPoint(initialPoint: Int): Int {
    var mutablePoint = initialPoint
    val half = mutablePoint / 2

    val thresholds = IntArray(21)
    thresholds[1] = mutablePoint * 5
    for (i in 2..20) {
        thresholds[i] = thresholds[i - 1] + half * (i + 1) * half
    }

    return when {
        this <= thresholds[1] -> {
            this / mutablePoint
        }

        this <= thresholds[20] -> {
            var stepIndex = 1
            while (stepIndex < 19 && this > thresholds[stepIndex + 1]) {
                stepIndex++
            }

            val baseLevel = 5 * stepIndex
            val remainderPoints = this - thresholds[stepIndex]
            mutablePoint += half * (stepIndex - 1)

            baseLevel + (remainderPoints / mutablePoint)
        }

        else -> 100
    }
}

fun Uri.getFileExtension(context: Context): String {
    val cR: ContentResolver = context.contentResolver
    val mime: MimeTypeMap = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(this))!!
}
