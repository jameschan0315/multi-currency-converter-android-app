package com.currencywiki.currencyconverter.common

import android.content.Context
import com.currencywiki.currencyconverter.fragments.ConvertListFragmentInterface
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.model.CurrencyItem
import com.currencywiki.currencyconverter.utils.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class AppData {
    lateinit var databaseManager: DatabaseManager
    lateinit var context: Context
    lateinit var settingManager: SettingManager
    var currencyFlagMap = TreeMap<String, String>()
    var currencyNameMap = TreeMap<String, String>()
    var currencySymbolMap = TreeMap<String, String>()

    var currencyItems = ArrayList<CurrencyItem>()
    var convertListFragmentInterface: ConvertListFragmentInterface? = null
    val reentrantLock = ReentrantLock()
    var currencyRateJson = JSONObject()

    fun loadCurrencyNameMap() {

        //
        // Load asset by language
        //

        val locale = Locale.getDefault()
        var language = locale.language
        val country = locale.country
        var fileName = language
        if (country.isNotEmpty()) {
            fileName = "$fileName-$country"
        }
        fileName = "$fileName.json"
        val jsonUtil = JSONUtil()
        var mapString = jsonUtil.loadJSONFromAssets(context, fileName)
        if (mapString == null) {
            mapString = jsonUtil.loadJSONFromAssets(context, "$language.json")
        }
        if (mapString == null) {
            mapString = jsonUtil.loadJSONFromAssets(context, "en-US.json")
        }

        //
        // Parse language
        //

        val jsonObj = JSONObject(mapString!!)
        val keys: Iterator<String> = jsonObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value = jsonObj[key].toString()
            currencyNameMap[key] = value
        }
    }

    fun loadData() {
        val jsonUtil = JSONUtil()
        if (currencyFlagMap.isEmpty()) {
            val mapString = jsonUtil.loadJSONFromAssets(context, "codes-map.json")
            val mapArray = JSONArray(mapString)
            for (index in 0 until mapArray.length()) {
                val mapItem = mapArray.getJSONObject(index)
                var flagName = mapItem.keys().next()
                val currencyCode = mapItem[flagName].toString()
                when (currencyCode) {
                    "USD" -> { flagName = "us" }
                    "EUR" -> { flagName = "eu" }
                    "GBP" -> { flagName = "gb" }
                    "ANG" -> { flagName = "sx" }
                    "AUD" -> { flagName = "au" }
                    "CHF" -> { flagName = "li" }
                    "DKK" -> { flagName = "dk" }
                    "ILS" -> { flagName = "il" }
                    "MAD" -> { flagName = "ma" }
                    "NOK" -> { flagName = "no" }
                    "NZD" -> { flagName = "nz" }
                    "XAF" -> { flagName = "ro" }
                    "XCD" -> { flagName = "ai" }
                    "XOF" -> { flagName = "tg" }
                    else -> { }
                }
                currencyFlagMap[currencyCode] = flagName.toLowerCase()
            }
        }

        if (currencyNameMap.isEmpty()) {
            loadCurrencyNameMap()
        }

        //
        // Load currency items
        //


        for ((key, _) in currencyFlagMap.entries) {
            var currency = Currency(key, true)
            currency.flag = currencyFlagMap[key]!!
            currency.name = currencyNameMap[key].toString()
            if (key == "USD" || key == "EUR") {
                currency.selected = true
            }
            currencyItems.add(CurrencyItem(currency, BigDecimal(1.0)))
        }

        //
        // Load Currency Symbol
        //

        if (currencySymbolMap.isEmpty()) {
            val mapString = jsonUtil.loadJSONFromAssets(context, "currency-symbols.json")
            val jsonObj = JSONObject(mapString!!)
            val keys: Iterator<String> = jsonObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                var value = jsonObj[key].toString()
                currencySymbolMap[key] = value
            }
        }

        //
        // Load sample rate json
        //

        val rateFetchTask = RateFetchTask(context)
        val result = rateFetchTask.execute().get()
        if (result != null) {
            settingManager.sampleRateJson = result.toString()
            saveSettingsToPreference()
        }
        else {
            currencyRateJson = JSONObject(settingManager.sampleRateJson)
        }
    }

    fun getCurrencyItems(selected: Boolean): ArrayList<CurrencyItem> {
        var selectedCurrencyItem = ArrayList<CurrencyItem>()
        for (currencyItem in currencyItems) {
            if (currencyItem.currency.selected == selected) {
                selectedCurrencyItem.add(currencyItem)
            }
        }
        return selectedCurrencyItem
    }
}
