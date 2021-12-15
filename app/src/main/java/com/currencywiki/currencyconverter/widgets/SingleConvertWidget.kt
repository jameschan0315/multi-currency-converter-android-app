package com.currencywiki.currencyconverter.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.drawableToBitmap
import com.currencywiki.currencyconverter.common.getWidgetGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.utils.*
import java.math.BigDecimal


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [SingleConvertWidgetConfigureActivity]
 */

private val MyOnClick = "myOnClickTag"
private var buttonClick = false

class SingleConvertWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateSingleConvertAppWidget(context, appWidgetManager, appWidgetId, false)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        widgetInfo: Bundle
    ) {
        if (context == null || appWidgetManager == null) {
            return
        }
        updateSingleConvertAppWidget(context, appWidgetManager, appWidgetId, false)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteSymbolsPref(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (MyOnClick == intent.action) {
            Log.e("Single Convert Widget", "Refresh Button Clicked>>>")
            val appWidgetIds =
                AppWidgetManager.getInstance(context).getAppWidgetIds(
                    context?.let {
                        ComponentName(
                            it,
                            SingleConvertWidget::class.java
                        )
                    }
                )
            val extras = intent.extras
            if (extras != null) {
                val widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (context != null) {
                    if (checkConnection(context)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //  val packageName = packageName
                            val pm: PowerManager =
                                context.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
                            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                val mIntent = Intent(context, MainActivity::class.java)
                                mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                mIntent.putExtra("permission", true)
                                context.startActivity(mIntent)
                            } else {
                                updateSingleConvertAppWidget(
                                    context,
                                    appWidgetManager,
                                    widgetId,
                                    true
                                )
                                CurrencyConverterApp.instance!!.executeRateUpdate()
                                Handler().postDelayed({
                                    Log.d("Working>>> ", "")
                                    updateSingleConvertAppWidget(
                                        context,
                                        appWidgetManager,
                                        widgetId,
                                        false
                                    )
                                }, 30000)
                            }
                        }else{
                            updateSingleConvertAppWidget(
                                context,
                                appWidgetManager,
                                widgetId,
                                true
                            )
                            CurrencyConverterApp.instance!!.executeRateUpdate()
                            Handler().postDelayed({
                                Log.d("Working>>> ", "")
                                updateSingleConvertAppWidget(
                                    context,
                                    appWidgetManager,
                                    widgetId,
                                    false
                                )
                            }, 30000)
                        }
                    }else{
                        Toast.makeText(context, "Please connect to Internet", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}

internal fun getPendingSelfIntent(
    context: Context?,
    action: String?,
    widgetId: Int
): PendingIntent? {
    val intent =
        Intent(context, SingleConvertWidget::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun updateSingleConvertAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    isProcess: Boolean
) {

    val symbol1 = loadSymbol1Pref(context, appWidgetId)
    val symbol2 = loadSymbol2Pref(context, appWidgetId)
    val currency1 = com.currencywiki.currencyconverter.model.Currency(symbol1)
    val currency2 = com.currencywiki.currencyconverter.model.Currency(symbol2)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.single_convert_widget)
    Log.d("singleWidgets", "appWidgetId>> $appWidgetId")
    views.setTextViewText(R.id.txt_symbol1, symbol1)
    views.setTextViewText(R.id.txt_symbol2, symbol2)
    views.setImageViewBitmap(R.id.img_flag1, getCircularBitmapFromSVGFileName(currency1.flag))
    views.setImageViewBitmap(R.id.img_flag2, getCircularBitmapFromSVGFileName(currency2.flag))
    views.setOnClickPendingIntent(
        R.id.btn_refresh,
        getPendingSelfIntent(context, MyOnClick, appWidgetId)
    )
    views.setOnClickPendingIntent(
        R.id.btn_refresh_dark,
        getPendingSelfIntent(context, MyOnClick, appWidgetId)
    )

    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    val updateDateTextSize =
        when (settingManager.visualSize) {
            0 -> {
                context.resources.getDimension(R.dimen.sp_12)
            }
            1 -> {
                context.resources.getDimension(R.dimen.sp_14)
            }
            else -> {
                context.resources.getDimension(R.dimen.sp_16)
            }
        }

    views.setTextViewTextSize(R.id.txt_provider, TypedValue.COMPLEX_UNIT_PX, updateDateTextSize)

    val percentTextSize =
        when (settingManager.visualSize) {
            0 -> {
                context.resources.getDimension(R.dimen.sp_10)
            }
            1 -> {
                context.resources.getDimension(R.dimen.sp_12)
            }
            else -> {
                context.resources.getDimension(R.dimen.sp_14)
            }
        }

    views.setTextViewTextSize(R.id.txt_percent, TypedValue.COMPLEX_UNIT_PX, percentTextSize)

    val rateTextSize =
        when (settingManager.visualSize) {
            0 -> {
                context.resources.getDimension(R.dimen.sp_20)
            }
            1 -> {
                context.resources.getDimension(R.dimen.sp_22)
            }
            else -> {
                context.resources.getDimension(R.dimen.sp_24)
            }
        }

    views.setTextViewTextSize(R.id.txt_rate, TypedValue.COMPLEX_UNIT_PX, rateTextSize)

    val symbolTextSize =
        when (settingManager.visualSize) {
            0 -> {
                context.resources.getDimension(R.dimen.sp_14)
            }
            1 -> {
                context.resources.getDimension(R.dimen.sp_16)
            }
            else -> {
                context.resources.getDimension(R.dimen.sp_18)
            }
        }

    views.setTextViewTextSize(R.id.txt_slash, TypedValue.COMPLEX_UNIT_PX, symbolTextSize)
    views.setTextViewTextSize(R.id.txt_symbol1, TypedValue.COMPLEX_UNIT_PX, symbolTextSize)
    views.setTextViewTextSize(R.id.txt_symbol2, TypedValue.COMPLEX_UNIT_PX, symbolTextSize)
    var listWidgetWidth = appWidgetManager.getAppWidgetOptions(appWidgetId)
        .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
    var listWidgetHeight = appWidgetManager.getAppWidgetOptions(appWidgetId)
        .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
    if (listWidgetWidth <= 0 || listWidgetHeight <= 0) {
        val providerInfo = AppWidgetManager.getInstance(
            CurrencyConverterApp.instance!!.applicationContext
        ).getAppWidgetInfo(appWidgetId)
        listWidgetWidth = providerInfo.minWidth
        listWidgetHeight = providerInfo.minHeight * 2
    }

    if (listWidgetWidth <= 0 || listWidgetHeight <= 0) {
        listWidgetWidth = 450
        listWidgetHeight = 300
    }

    views.setImageViewBitmap(
        R.id.img_container,
        drawableToBitmap(
            getWidgetGradientDrawable(settingManager.gradient, 0, 0, 16.0F),
            listWidgetWidth,
            listWidgetHeight
        )
    )
    val rate = getCurrentCurrencyRate(symbol1, symbol2)
    val rateYesterday = getYesterdayCurrencyRate(symbol1, symbol2)
    views.setTextViewText(R.id.txt_rate, decimalString(rate))
    var percent = 0.0
    if (rateYesterday.toDouble() != 0.0) {
        percent = (rate.toDouble() - rateYesterday.toDouble()) /** 100 / rateYesterday.toDouble()*/
    }
    val percentLabel = decimalString(BigDecimal(percent), "0.0000") /*+ "%"*/
    if (percent > 0) {
        views.setTextViewText(R.id.txt_percent, "+$percentLabel")
    } else {
        views.setTextViewText(R.id.txt_percent, percentLabel)
    }
    Log.d("WidgetApp", "percentLabel>> $percentLabel")
    if (percentLabel == "0.0000" || percentLabel == "-0.0000"){
        views.setViewVisibility(R.id.txt_percent, View.GONE)
    }else{
        views.setViewVisibility(R.id.txt_percent, View.VISIBLE)
    }
    if (isDarkMode()) {
        views.setTextColor(R.id.txt_symbol1, Color.DKGRAY)
        views.setTextColor(R.id.txt_slash, Color.DKGRAY)
        views.setTextColor(R.id.txt_symbol2, Color.DKGRAY)
        views.setTextColor(R.id.txt_rate, Color.DKGRAY)
        views.setTextColor(R.id.txt_percent, Color.DKGRAY)
        views.setTextColor(R.id.txt_provider, Color.DKGRAY)
        views.setViewVisibility(R.id.btn_refresh, View.GONE)
        views.setViewVisibility(R.id.btn_refresh_dark, View.VISIBLE)
    } else {
        views.setTextColor(R.id.txt_symbol1, Color.WHITE)
        views.setTextColor(R.id.txt_slash, Color.WHITE)
        views.setTextColor(R.id.txt_symbol2, Color.WHITE)
        views.setTextColor(R.id.txt_rate, Color.WHITE)
        views.setTextColor(R.id.txt_percent, Color.WHITE)
        views.setTextColor(R.id.txt_provider, Color.WHITE)
        views.setViewVisibility(R.id.btn_refresh, View.VISIBLE)
        views.setViewVisibility(R.id.btn_refresh_dark, View.GONE)
    }
    if (isProcess) {
        views.setViewVisibility(R.id.btn_refresh, View.GONE)
        views.setViewVisibility(R.id.btn_refresh_dark, View.GONE)
        if (isDarkMode())
            views.setViewVisibility(R.id.progressDark, View.VISIBLE)
        else
            views.setViewVisibility(R.id.progressWhite, View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.progressWhite, View.GONE)
        views.setViewVisibility(R.id.progressDark, View.GONE)
        if (isDarkMode())
            views.setViewVisibility(R.id.btn_refresh_dark, View.VISIBLE)
        else
            views.setViewVisibility(R.id.btn_refresh, View.VISIBLE)
    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}