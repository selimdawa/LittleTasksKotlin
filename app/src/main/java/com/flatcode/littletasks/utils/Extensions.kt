package com.flatcode.littletasks.utils

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.net.toUri
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.DialogAboutAppBinding
import com.flatcode.littletasks.databinding.DialogCloseAppBinding
import com.flatcode.littletasks.databinding.DialogLogoutBinding
import com.flatcode.littletasks.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.MessageFormat

fun Context.openActivity(c: Class<*>, isFinished: Boolean = false, vararg extras: Pair<String, String?>) {
    val intent = Intent(this, c)
    if (isFinished) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    extras.forEach { intent.putExtra(it.first, it.second) }
    this.startActivity(intent)
}

fun Activity.closeApp() {
    if (this.isFinishing || this.isDestroyed) return

    val binding = DialogCloseAppBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.let { window ->
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
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
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.yes.setOnClickListener {
        FirebaseAuth.getInstance().signOut()
        this@dialogLogout.openActivity(AuthActivity::class.java, true)
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
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.website.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, DATA.WEBSITE.toUri())
        this@dialogAboutApp.startActivity(intent)
    }

    binding.facebook.setOnClickListener {
        val facebookUri = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this@dialogAboutApp.packageManager.getPackageInfo(
                    "com.facebook.katana", PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION") this@dialogAboutApp.packageManager.getPackageInfo(
                    "com.facebook.katana", 0
                )
            }
            "fb://profile/${DATA.FB_ID}"
        } catch (_: Exception) {
            "https://facebook.com${DATA.FB_ID}"
        }

        val intent = Intent(Intent.ACTION_VIEW, facebookUri.toUri())
        this@dialogAboutApp.startActivity(intent)
    }

    dialog.show()
}

fun Activity.startCropImageSquare() {
    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE).setAspectRatio(1, 1)
        .setCropShape(CropImageView.CropShape.OVAL).start(this)
}

fun Activity.startCropImageWide() {
    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE).setAspectRatio(2, 1)
        .setCropShape(CropImageView.CropShape.OVAL).start(this)
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
    val marketUri = "market://details?id=$packageName".toUri()
    val webUri = "https://google.com".toUri()

    try {
        this.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
    } catch (_: ActivityNotFoundException) {
        this.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

fun Context.dialogOptionDelete(database: String?, onDelete: () -> Unit) {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)
    dialog.window?.let { window ->
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp
    }

    binding.title.setText(R.string.do_you_want_to_delete_the)
    val baseTitle: String = binding.title.text.toString()

    binding.title.text = when (database) {
        DATA.CATEGORIES -> MessageFormat.format("{0} Category?", baseTitle)
        DATA.OBJECTS -> MessageFormat.format("{0} Object?", baseTitle)
        DATA.TASKS -> MessageFormat.format("{0} Task?", baseTitle)
        DATA.PLANS -> MessageFormat.format("{0} Plan?", baseTitle)
        else -> baseTitle
    }

    binding.yes.setOnClickListener {
        onDelete()
        dialog.dismiss()
    }
    binding.no.setOnClickListener { dialog.dismiss() }
    dialog.show()
}

fun Context.showMoreOptions(options: Array<String>, onOptionSelected: (Int) -> Unit) {
    AlertDialog.Builder(this).setTitle("Choose Options")
        .setItems(options) { _: DialogInterface?, which: Int ->
            onOptionSelected(which)
        }.show()
}

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

fun Int.levelPoint(): Int {
    val initialPoint = 10
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

object GetTimeAgo {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(time: Long): String? {
        val normalizedTime = if (time < 1000000000000L) time * 1000 else time
        val now = System.currentTimeMillis()

        if (normalizedTime !in 1..now) return null

        val diff = now - normalizedTime
        return when {
            diff < MINUTE_MILLIS -> "just now"
            diff < 2 * MINUTE_MILLIS -> "a minute ago"
            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} minutes ago"
            diff < 90 * MINUTE_MILLIS -> "an hour ago"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} hours ago"
            diff < 48 * HOUR_MILLIS -> "yesterday"
            else -> "${diff / DAY_MILLIS} days ago"
        }
    }

    fun getMessageAgo(time: Long): String? {
        val normalizedTime = if (time < 1000000000000L) time * 1000 else time
        val now = System.currentTimeMillis()

        if (normalizedTime !in 1..now) return null

        val diff = now - normalizedTime
        return when {
            diff < MINUTE_MILLIS -> "1 s"
            diff < 2 * MINUTE_MILLIS -> "1 m"
            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} m"
            diff < 90 * MINUTE_MILLIS -> "1 h"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} h"
            diff < 48 * HOUR_MILLIS -> "1 d"
            else -> "${diff / DAY_MILLIS} d"
        }
    }
}

class SimpleBlurTransformation(private val radius: Float) : Transformation() {
    override val cacheKey: String = "${SimpleBlurTransformation::class.java.name}-$radius"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (input.isRecycled) return input
        val scaleFactor = 6
        val w = (input.width / scaleFactor).coerceAtLeast(1)
        val h = (input.height / scaleFactor).coerceAtLeast(1)
        val small = input.scale(w, h, true)
        val r = (radius / scaleFactor).toInt().coerceAtLeast(1)
        val pix = IntArray(w * h)
        small.getPixels(pix, 0, w, 0, 0, w, h)
        val blurred = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            var rs = 0L
            var gs = 0L
            var bs = 0L
            var c = 0
            for (i in -r..r) {
                val xi = (x + i).coerceIn(0, w - 1)
                val p = pix[y * w + xi]
                rs += (p shr 16) and 0xff
                gs += (p shr 8) and 0xff
                bs += p and 0xff
                c++
            }
            blurred[y * w + x] =
                (0xff shl 24) or ((rs / c).toInt() shl 16) or ((gs / c).toInt() shl 8) or (bs / c).toInt()
        }
        for (x in 0 until w) for (y in 0 until h) {
            var rs = 0L
            var gs = 0L
            var bs = 0L
            var c = 0
            for (i in -r..r) {
                val yi = (y + i).coerceIn(0, h - 1)
                val p = blurred[yi * w + x]
                rs += (p shr 16) and 0xff
                gs += (p shr 8) and 0xff
                bs += p and 0xff
                c++
            }
            pix[y * w + x] =
                (0xff shl 24) or ((rs / c).toInt() shl 16) or ((gs / c).toInt() shl 8) or (bs / c).toInt()
        }
        val output = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        output.setPixels(pix, 0, w, 0, 0, w, h)
        val finalOutput = output.scale(input.width, input.height, true)
        if (output != finalOutput) output.recycle()
        if (small != input) small.recycle()
        return finalOutput
    }
}