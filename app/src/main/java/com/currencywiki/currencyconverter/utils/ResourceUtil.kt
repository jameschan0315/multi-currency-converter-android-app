package com.currencywiki.currencyconverter.utils

import android.content.ContentResolver
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestBuilder
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.larvalabs.svgandroid.SVGParser


var isKill = true

fun getDrawableByString(resourceName: String): Int {
    val context = CurrencyConverterApp.instance!!.applicationContext
    return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
}

fun getResourceIdByString(resourceName: String): Int {
    val context = CurrencyConverterApp.instance!!.applicationContext
    return context.resources.getIdentifier(resourceName, "id", context.packageName)
}

fun setDrawableTint(drawable: Drawable, @ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(drawable, color)
    } else {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun getFlagDrawable(flagName: String): Int {
    return getDrawableByString("ic_$flagName")
}

fun getFlagRaw(flagName: String): Int {
    val context = CurrencyConverterApp.instance!!.applicationContext
    return context.resources.getIdentifier("ic_$flagName", "raw", context.packageName)
}

fun setSVGImageToImageView(
    requestBuilder: RequestBuilder<PictureDrawable>?,
    imageView: ImageView,
    flagName: String
) {
    val context = CurrencyConverterApp.instance!!.applicationContext
    if (flagName == "mm" || flagName == "ss" || flagName == "ve" || flagName == "bc" || flagName == "ca") {
        val imageResource = context.resources.getIdentifier(
            "ic_$flagName",
            "drawable",
            context.packageName
        )
        imageView.setImageResource(imageResource)
        return
    }

    val uri = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://"
                + context.packageName
                + "/"
                + getFlagRaw(flagName)
    )
    requestBuilder?.load(uri)?.into(imageView)
}

fun bitmapFromSVGFileName(fileName: String): Bitmap? {
    val context = CurrencyConverterApp.instance!!.applicationContext
    if (fileName == "mm" || fileName == "ss" || fileName == "ve" || fileName == "bc" || fileName == "ca") {
        val imageResource = context.resources.getIdentifier(
            "ic_$fileName",
            "drawable",
            context.packageName
        )
        val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(imageResource) as BitmapDrawable
        } else {
            context.resources.getDrawable(imageResource) as BitmapDrawable
        }
        return drawable.bitmap
    }
    val pictureDrawable = pictureDrawableFromSVGFileName(fileName)
    return pictureDrawable2Bitmap(pictureDrawable)
}

fun getCircularBitmapFromSVGFileName(fileName: String): Bitmap? {
    val bitmap = bitmapFromSVGFileName(fileName)
    return getCircularBitmapFrom(bitmap)
}

fun getCircularBitmapFrom(bitmap: Bitmap?): Bitmap? {
    if (bitmap == null || bitmap.isRecycled) {
        return null
    }
    val radius = if (bitmap.width > bitmap.height) bitmap
        .height.toFloat() / 2f else bitmap.width.toFloat() / 2f
    val canvasBitmap = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888
    )
    val shader = BitmapShader(
        bitmap, Shader.TileMode.CLAMP,
        Shader.TileMode.CLAMP
    )
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = shader
    val canvas = Canvas(canvasBitmap)
    canvas.drawCircle(
        bitmap.width / 2.toFloat(), bitmap.height / 2.toFloat(),
        radius, paint
    )
    return canvasBitmap
}

fun pictureDrawableFromSVGFileName(fileName: String): PictureDrawable {
    val context = CurrencyConverterApp.instance!!.applicationContext
    val resourceId = getFlagRaw(fileName)
    val svg = SVGParser.getSVGFromResource(context.resources, resourceId)
    return svg.createPictureDrawable()
}

fun pictureDrawable2Bitmap(pictureDrawable: PictureDrawable): Bitmap? {
    val bitmap: Bitmap =
        Bitmap.createBitmap(
            pictureDrawable.intrinsicWidth,
            pictureDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    val canvas = Canvas(bitmap)
    canvas.drawPicture(pictureDrawable.picture)
    return bitmap
}

fun getResourceUri(resourceId: Int): Uri {
    val context = CurrencyConverterApp.instance!!.applicationContext
    val resources = context.resources
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(resourceId))
        .appendPath(resources.getResourceTypeName(resourceId))
        .appendPath(resources.getResourceEntryName(resourceId))
        .build()
}

fun setCursorPointerColor(view: EditText, @ColorInt color: Int) {
    try {
        //get the pointer resource id
        var field = TextView::class.java.getDeclaredField("mTextSelectHandleRes")
        field.isAccessible = true
        val drawableResId = field.getInt(view)

        //get the editor
        field = TextView::class.java.getDeclaredField("mEditor")
        field.isAccessible = true
        val editor = field.get(view)

        //tint drawable
        val drawable = ContextCompat.getDrawable(view.context, drawableResId)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(drawable, color)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

        //set the drawable
        field = editor.javaClass.getDeclaredField("mSelectHandleCenter")
        field.isAccessible = true
        field.set(editor, drawable)

    } catch (ex: Exception) {
    }
}

fun setCursorDrawableColorNew(editText: TextView, @ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
//        gradientDrawable.setSize(2.spToPx(editText.context).toInt(), editText.textSize.toInt())
//        editText.textCursorDrawable = gradientDrawable
        return
    }

    try {
        val editorField = try {
            TextView::class.java.getDeclaredField("mEditor").apply { isAccessible = true }
        } catch (t: Throwable) {
            null
        }
        val editor = editorField?.get(editText) ?: editText
        val editorClass: Class<*> =
            if (editorField == null) TextView::class.java else editor.javaClass

        val tintedCursorDrawable = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            .apply { isAccessible = true }
            .getInt(editText)
            .let { ContextCompat.getDrawable(editText.context, it) ?: return }
            .let { tintDrawable(it, color) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            editorClass
                .getDeclaredField("mDrawableForCursor")
                .apply { isAccessible = true }
                .run { set(editor, tintedCursorDrawable) }
        } else {
            editorClass
                .getDeclaredField("mCursorDrawable")
                .apply { isAccessible = true }
                .run { set(editor, arrayOf(tintedCursorDrawable, tintedCursorDrawable)) }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}

fun Number.spToPx(context: Context? = null): Float {
    val res = context?.resources ?: android.content.res.Resources.getSystem()
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), res.displayMetrics)
}

fun tintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
    (drawable as? VectorDrawableCompat)
        ?.apply { setTintList(ColorStateList.valueOf(color)) }
        ?.let { return it }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        (drawable as? VectorDrawable)
            ?.apply { setTintList(ColorStateList.valueOf(color)) }
            ?.let { return it }
    }

    val wrappedDrawable = DrawableCompat.wrap(drawable)
    DrawableCompat.setTint(wrappedDrawable, color)
    return DrawableCompat.unwrap(wrappedDrawable)
}

fun checkConnection(context: Context): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connMgr.activeNetworkInfo
    if (activeNetworkInfo != null) { // connected to the internet
        // connected to the mobile provider's data plan
        return if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
            // connected to wifi
            true
        } else activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
    }

    return false
}