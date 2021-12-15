package com.currencywiki.currencyconverter.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.drawableToBitmap
import com.currencywiki.currencyconverter.common.getWidgetGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.utils.checkConnection
import com.currencywiki.currencyconverter.utils.decimalStringEliminateZeros
import com.currencywiki.currencyconverter.utils.isKill
import java.math.BigDecimal


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ConvertListWidgetConfigureActivity]
 */
private val MyOnClick = "myOnClickTag"
private var buttonClick = false

class ConvertListWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, false)
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
        updateAppWidget(context, appWidgetManager, appWidgetId, false)
    }

    fun notifyUpdate(
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_currency);
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    override fun onReceive(
        context: Context?,
        intent: Intent
    ) {
        val mgr =
            AppWidgetManager.getInstance(context)
        if (intent.action == TOAST_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
            val symbol1 = intent.getStringExtra(EXTRA_SYMBOL1)
            val symbol2 = intent.getStringExtra(EXTRA_SYMBOL2)
            val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 1.0)
//            if (CurrencyConverterApp.instance?.mainActivity != null) {
//                CurrencyConverterApp.instance?.mainActivity!!.selectTab(0)
//                CurrencyConverterApp.instance?.mainActivity!!.finish()
//            }
            Log.i("MainActivity", "viewIndex>>> $viewIndex")
            CurrencyConverterApp
            val startActivityIntent =
                Intent(context, MainActivity::class.java)
            startActivityIntent.putExtra("id", java.lang.String.valueOf(viewIndex))
            startActivityIntent.putExtra("symbol1", symbol1)
            startActivityIntent.putExtra("symbol2", symbol2)
            startActivityIntent.putExtra("amount", amount)
            startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(startActivityIntent)
        } else if (intent.action == MyOnClick) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds =
                AppWidgetManager.getInstance(context).getAppWidgetIds(
                    context?.let {
                        ComponentName(
                            it,
                            ConvertListWidget::class.java
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
                    if (checkConnection(context)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //  val packageName = packageName
                            val pm: PowerManager = context.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
                            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                val mIntent = Intent(context, MainActivity::class.java)
                                mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                mIntent.putExtra("permission", true)
                                context.startActivity(mIntent)
                            }else{
                                updateAppWidget(context, appWidgetManager, widgetId, true)
                                CurrencyConverterApp.instance!!.executeRateUpdate()
                                Handler().postDelayed({
                                    updateAppWidget(context, appWidgetManager, widgetId, false)
                                }, 30000)
                            }
                        }else{
                            updateAppWidget(context, appWidgetManager, widgetId, true)
                            CurrencyConverterApp.instance!!.executeRateUpdate()
                            Handler().postDelayed({
                                updateAppWidget(context, appWidgetManager, widgetId, false)
                            }, 30000)
                        }
                    }else{
                        Toast.makeText(context, "Please connect to Internet", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
        super.onReceive(context, intent)
    }
}

internal fun getPendingSelfIntentForConvertList(
    context: Context?,
    action: String?,
    widgetId: Int
): PendingIntent? {
    val intent =
        Intent(context, ConvertListWidget::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    isProcess: Boolean
) {
    val views = RemoteViews(context.packageName, R.layout.convert_list_widget)
    Log.d("convertList", "appWidgetId>> $appWidgetId")
    val jsonString = loadItemsPref(context, appWidgetId)
    val baseCurrency = loadBaseCurrency(context, appWidgetId)
    val amount = loadAmount(context, appWidgetId)
    setRemoteAdapter(context, views, jsonString, baseCurrency, amount, appWidgetId)
    views.setOnClickPendingIntent(
        R.id.btn_refresh,
        getPendingSelfIntentForConvertList(context, MyOnClick, appWidgetId)
    )
    views.setOnClickPendingIntent(
        R.id.btn_refresh_dark,
        getPendingSelfIntentForConvertList(context, MyOnClick, appWidgetId)
    )

    // Instruct the widget manager to update the widget
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    var symbolTextSize =
        when (settingManager.visualSize) {
            0 -> {
                context.resources.getDimension(R.dimen.sp_17)
            }
            1 -> {
                context.resources.getDimension(R.dimen.sp_19)
            }
            else -> {
                context.resources.getDimension(R.dimen.sp_21)
            }
        }

    views.setTextViewTextSize(R.id.usd_symbol, TypedValue.COMPLEX_UNIT_PX, symbolTextSize)

    var updateDateTextSize =
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

    var listWidgetWidth = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(
        OPTION_APPWIDGET_MIN_WIDTH,
        0
    )
    var listWidgetHeight = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(
        OPTION_APPWIDGET_MAX_HEIGHT,
        0
    )
    if (listWidgetWidth <= 0 || listWidgetHeight <= 0) {
        val providerInfo = AppWidgetManager.getInstance(
            CurrencyConverterApp.instance!!.applicationContext
        ).getAppWidgetInfo(appWidgetId)
        listWidgetWidth = providerInfo.minWidth
        listWidgetHeight = providerInfo.minHeight
    }

    if (listWidgetWidth <= 0 || listWidgetHeight <= 0) {
        listWidgetWidth = 400
        listWidgetHeight = 300
    }

    views.setImageViewBitmap(
        R.id.img_container, drawableToBitmap(
            getWidgetGradientDrawable(
                settingManager.gradient,
                0,
                0,
                16.0F
            ), listWidgetWidth, listWidgetHeight
        )
    )
    var amountText =
        decimalStringEliminateZeros(BigDecimal(amount))
    views.setTextViewText(R.id.usd_symbol, "$amountText $baseCurrency = ")
    if (isDarkMode()) {
        views.setTextColor(R.id.usd_symbol, Color.DKGRAY)
        views.setTextColor(R.id.txt_provider, Color.DKGRAY)
        views.setViewVisibility(R.id.view_separator_white, View.GONE)
        views.setViewVisibility(R.id.view_separator_dark, View.VISIBLE)
        views.setViewVisibility(R.id.btn_refresh, View.GONE)
        views.setViewVisibility(R.id.btn_refresh_dark, View.VISIBLE)
    } else {
        views.setTextColor(R.id.usd_symbol, Color.WHITE)
        views.setTextColor(R.id.txt_provider, Color.WHITE)
        views.setViewVisibility(R.id.view_separator_white, View.VISIBLE)
        views.setViewVisibility(R.id.view_separator_dark, View.GONE)
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
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_currency)
    }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

const val TOAST_ACTION = "com.currency.android.listWidget.TOAST_ACTION"
const val EXTRA_ITEM = "com.currency.android.listWidget.EXTRA_ITEM"
const val EXTRA_AMOUNT = "com.currency.android.listWidget.EXTRA_AMOUNT"
const val EXTRA_SYMBOL1 = "com.currency.android.listWidget.EXTRA_SYMBOL1"
const val EXTRA_SYMBOL2 = "com.currency.android.listWidget.EXTRA_SYMBOL2"

internal fun setRemoteAdapter(
    context: Context,
    @NonNull views: RemoteViews,
    jsonItems: String,
    baseCurrency: String,
    amount: Double,
    appWidgetId: Int
) {
    val intent = Intent(context, WidgetService::class.java)
    intent.putExtra("jsonItems", jsonItems)
    intent.putExtra("baseCurrency", baseCurrency)
    intent.putExtra("amount", amount)

    views.setRemoteAdapter(
        R.id.list_currency,
        intent
    )

    val toastIntent = Intent(
        context,
        ConvertListWidget::class.java
    )
    toastIntent.action = TOAST_ACTION
    toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
    val toastPendingIntent = PendingIntent.getBroadcast(
        context, 0, toastIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setPendingIntentTemplate(R.id.list_currency, toastPendingIntent)
}
