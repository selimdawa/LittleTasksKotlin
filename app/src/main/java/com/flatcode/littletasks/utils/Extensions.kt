package com.flatcode.littletasks.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import com.flatcode.littletasks.R
import java.io.Serializable

inline fun <reified T : Activity> Context.openActivity(
    clear: Boolean = false, vararg extras: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java).apply {
        if (clear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { (key, value) ->
            when (value) {
                is String -> putExtra(key, value)
                is Int -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Serializable -> putExtra(key, value)
            }
        }
    }
    startActivity(intent)
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Context.startCropActivity(
    uri: Uri, aspectRatioX: Int = 1, aspectRatioY: Int = 1, isOval: Boolean = false
): Intent {
    return Intent(this, CropActivity::class.java).apply {
        putExtra("IMAGE_URI", uri)
        putExtra("ASPECT_RATIO_X", aspectRatioX)
        putExtra("ASPECT_RATIO_Y", aspectRatioY)
        putExtra("IS_OVAL", isOval)
        putExtra("MIN_WIDTH", DATA.MIX_SQUARE)
        putExtra("MIN_HEIGHT", DATA.MIX_SQUARE)
    }
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

fun ImageView.loadImage(isUser: Boolean, url: String?) {
    try {
        if (url.isNullOrEmpty() || url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(url) {
                placeholder(R.color.image_profile)
                error(R.color.image_profile)
                fallback(R.color.image_profile)
                crossfade(true)
            }
        }
    } catch (_: Exception) {
        if (isUser) {
            this.setImageResource(R.drawable.basic_user)
        } else {
            this.setImageResource(R.drawable.basic_book)
        }
    }
}

fun ImageView.loadBlurImage(isUser: Boolean, url: String?, level: Int) {
    try {
        if (url.isNullOrEmpty() || url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(url) {
                placeholder(R.color.image_profile)
                error(R.color.image_profile)
                fallback(R.color.image_profile)
                transformations(SimpleBlurTransformation(level.toFloat()))
            }
        }
    } catch (_: Exception) {
        if (isUser) {
            this.setImageResource(R.drawable.basic_user)
        } else {
            this.setImageResource(R.drawable.basic_book)
        }
    }
}

object GetTimeAgo {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

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