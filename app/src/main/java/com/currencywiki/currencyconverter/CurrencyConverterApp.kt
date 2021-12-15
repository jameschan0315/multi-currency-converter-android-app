package com.currencywiki.currencyconverter

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.util.Log
import android.widget.Toast
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.AppData
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.RateFetchTask
import com.currencywiki.currencyconverter.utils.RateFetchTaskInterface
import com.currencywiki.currencyconverter.utils.SettingManager
import com.currencywiki.currencyconverter.utils.saveSettingsToPreference
import com.currencywiki.currencyconverter.widgets.ConvertListWidget
import com.currencywiki.currencyconverter.widgets.SingleConvertWidget
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.google.gson.Gson
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class CurrencyConverterApp : Application(), RateFetchTaskInterface {
    var currencyList: ArrayList<Currency> = ArrayList()
    lateinit var appData: AppData
    var mainActivity: MainActivity? = null
    private val AF_DEV_KEY = "sSciSETKRuU6a8cqCETSSJ"

    override fun onCreate() {
        super.onCreate()
        instance = this
       // FacebookSdk.fullyInitialize()
       // FacebookSdk.setAdvertiserIDCollectionEnabled(true)
       // FacebookSdk.setAutoLogAppEventsEnabled(true)
       // FacebookSdk.setIsDebugEnabled(true);
       // FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                for (attrName in conversionData.keys) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData[attrName])
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d("LOG_TAG", "error getting conversion data: $errorMessage")
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                for (attrName in attributionData.keys) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData[attrName])
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d("LOG_TAG", "error onAttributionFailure : $errorMessage")
            }
        }
        AppsFlyerLib.getInstance().setOneLinkCustomDomain(
            "app.currency.wiki",
            "click.currency.wiki",
            "share.currency.wiki"
        )
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().setDebugLog(true)
        this.appData = AppData()
        this.appData.context = applicationContext
        this.appData.settingManager = loadSettingsFromPref()
        this.appData.loadData()
        var execService: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
        execService.scheduleAtFixedRate(
            {
                executeRateUpdate()
            },
            0,
            10,
            TimeUnit.MINUTES
        )
    }

    var InstallConversionData = ""
    var sessionCount = 0
    fun setInstallData(conversionData: Map<String, Any>) {
        if (sessionCount == 0) {
            val install_type = """
            Install Type: ${conversionData["af_status"]}
            
            """.trimIndent()
            val media_source = """
            Media Source: ${conversionData["media_source"]}
            
            """.trimIndent()
            val install_time = """
            Install Time(GMT): ${conversionData["install_time"]}
            
            """.trimIndent()
            val click_time = """
            Click Time(GMT): ${conversionData["click_time"]}
            
            """.trimIndent()
            val is_first_launch = """
            Is First Launch: ${conversionData["is_first_launch"]}
            
            """.trimIndent()
            InstallConversionData += install_type + media_source + install_time + click_time + is_first_launch
            sessionCount++
        }
    }


    fun executeRateUpdate() {
        val rateFetchTask = RateFetchTask(applicationContext)
        rateFetchTask.execute()
        rateFetchTask.rateFetchTaskInterface = this
    }

    private fun loadSettingsFromPref(): SettingManager {
        val prefs = applicationContext.getSharedPreferences("SETTING_PREF", 0)
        val settingSerialized =
            prefs.getString("SETTING_SERIALIZE", null) ?: return SettingManager()
        return Gson().fromJson(settingSerialized, SettingManager::class.java)
    }

    fun triggerWidgetUpdate() {
        val ids =
            AppWidgetManager.getInstance(this).getAppWidgetIds(
                ComponentName(
                    this,
                    SingleConvertWidget::class.java
                )
            )
        val myWidget = SingleConvertWidget()
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this), ids)

        val listWidgetIds =
            AppWidgetManager.getInstance(this).getAppWidgetIds(
                ComponentName(
                    this,
                    ConvertListWidget::class.java
                )
            )
        val myListWidget = ConvertListWidget()
        myListWidget.onUpdate(this, AppWidgetManager.getInstance(this), listWidgetIds)
    }

    companion object {
        var instance: CurrencyConverterApp? = null
            private set
    }

    override fun rateUpdated(success: Boolean) {
        if (!success) {
            return
        }
        triggerWidgetUpdate()
        this.appData.settingManager.sampleRateJson = appData.currencyRateJson.toString()
        saveSettingsToPreference()
        if (this.mainActivity != null) {
            this.mainActivity!!.updateRate()
        }
    }

    override fun rateError(error: String) {
        if (this.mainActivity != null) {
            this.mainActivity!!.runOnUiThread {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}