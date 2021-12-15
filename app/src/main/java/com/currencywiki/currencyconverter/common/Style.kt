package com.currencywiki.currencyconverter.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import com.currencywiki.currencyconverter.CurrencyConverterApp

val visualSize = arrayOf("Small", "Medium", "Large")

fun getARGBColor(color: Int): Int {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hsv = FloatArray(3)
    Color.RGBToHSV(r, g, b, hsv)
    return Color.HSVToColor(hsv)
}

fun getARGBWidgetColor(color: Int): Int {
    var a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hsv = FloatArray(3)
    Color.RGBToHSV(r, g, b, hsv)
    return Color.HSVToColor(a, hsv)
}

fun getTransparencySetting(): Int {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    val color = settingManager.gradient
    val alpha = Color.alpha(color)
    val transparency = (255 - alpha).toFloat() * 100.0f / 255.0f
    return transparency.toInt()
}

fun setTransparencySetting(transparency: Int) {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    val color = settingManager.gradient
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val alpha = (100 - transparency).toFloat() * 255.0f / 100.0f
    settingManager.gradient = Color.argb(alpha.toInt(), r, g, b)
}

fun isDarkMode(): Boolean {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    return settingManager.themeType == 1
}

fun isDarkModeBasedOnColor(): Boolean {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    val color = settingManager.gradient
    var a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hsv = FloatArray(3)
    Color.RGBToHSV(r, g, b, hsv)
    if (a < 30) {
        return true
    }
    if (hsv[1] < 0.1 && hsv[2] > 0.9) {
        return true
    }
    return false
}

fun getWidgetGradientDrawable(color: Int, orientation: Int = 0, shape: Int = 0, radius: Float = 0.0F): GradientDrawable {
    var a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hsv = FloatArray(3)
    val orgHsv = FloatArray(3)
    Color.RGBToHSV(r, g, b, hsv)
    Color.RGBToHSV(r, g, b, orgHsv)
    var startSaturation = hsv[1] - 0.37F
    if (startSaturation < 0.2) {
        if (hsv[2] < 0.25) {
            hsv[1] = 0.2F
        }
        else {
            hsv[2] = hsv[2] - 0.2F
        }
    }
    else {
        hsv[1] = startSaturation
    }
    var startColor = Color.HSVToColor(a, hsv)
    var endColor = Color.HSVToColor(a, orgHsv)
    val gradientColors = intArrayOf(
        startColor, endColor)

    var gradientOrientation = if (orientation == 0) {
        GradientDrawable.Orientation.TOP_BOTTOM
    }
    else {
        GradientDrawable.Orientation.LEFT_RIGHT
    }

    var gradientDrawable: GradientDrawable
    return if (shape == 0) {
        gradientDrawable = GradientDrawable(
            gradientOrientation,
            gradientColors
        )
        gradientDrawable.cornerRadius = radius
        gradientDrawable
    }
    else {
        val gradientDrawable = GradientDrawable(
            gradientOrientation,
            gradientColors
        )
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable
    }
}

fun getGradientDrawable(color: Int, orientation: Int = 0, shape: Int = 0, radius: Float = 0.0F): GradientDrawable {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hsv = FloatArray(3)
    val orgHsv = FloatArray(3)
    Color.RGBToHSV(r, g, b, hsv)
    Color.RGBToHSV(r, g, b, orgHsv)
    var startSaturation = hsv[1] - 0.37F
    if (startSaturation < 0.2) {
        if (hsv[2] < 0.25) {
            hsv[1] = 0.2F
        }
        else {
            hsv[2] = hsv[2] - 0.2F
        }
    }
    else {
        hsv[1] = startSaturation
    }
    var startColor = Color.HSVToColor(hsv)
    var endColor = Color.HSVToColor(orgHsv)
    val gradientColors = intArrayOf(
        startColor, endColor)

    var gradientOrientation = if (orientation == 0) {
        GradientDrawable.Orientation.TOP_BOTTOM
    }
    else {
        GradientDrawable.Orientation.LEFT_RIGHT
    }

    var gradientDrawable: GradientDrawable
    return if (shape == 0) {
        gradientDrawable = GradientDrawable(
            gradientOrientation,
            gradientColors
        )
        gradientDrawable.cornerRadius = radius
        gradientDrawable
    }
    else {
        val gradientDrawable = GradientDrawable(
            gradientOrientation,
            gradientColors
        )
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable
    }
}

fun drawableToBitmap(drawable: GradientDrawable, width: Int = 300, height: Int = 300): Bitmap? {
    var bitmap: Bitmap? = null
    if (drawable is BitmapDrawable) {
        val bitmapDrawable: BitmapDrawable = drawable as BitmapDrawable
        if (bitmapDrawable.bitmap != null) {
            return bitmapDrawable.bitmap
        }
    }
    bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
