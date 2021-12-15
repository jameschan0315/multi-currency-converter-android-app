package com.currencywiki.currencyconverter.utils

import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.model.Currency
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentCurrencyRate(currencySymbol1: String, currencySymbol2: String): BigDecimal {
    return getCurrencyRate(currencySymbol1, currencySymbol2, 0)
}

fun getYesterdayCurrencyRate(currencySymbol1: String, currencySymbol2: String): BigDecimal {
    return getCurrencyRate(currencySymbol1, currencySymbol2, 1)
}

fun getCurrencyRate(currencySymbol1: String, currencySymbol2: String, type: Int): BigDecimal {
    var rate = BigDecimal(1.0)
    if (currencySymbol1 == currencySymbol2) {
        return rate
    }

    val currencyRate1 = getCurrencyRateWithUSD(currencySymbol1, type)
    val currencyRate2 = getCurrencyRateWithUSD(currencySymbol2, type)

    return currencyRate1.divide(currencyRate2, MathContext.DECIMAL128)
}

fun getCurrencyRateWithUSD(currencySymbol: String, type: Int): BigDecimal {
    var rate = 1.0
    if (currencySymbol == "USD") {
        return BigDecimal(rate)
    }

    val appData = CurrencyConverterApp.instance!!.appData
    if (appData.currencyRateJson.toString() == "") {
        appData.currencyRateJson = JSONObject(appData.settingManager.sampleRateJson)
    }
   // Log.d("Hoga", "currencyRateJson>>> ${appData.currencyRateJson}")
    try {
        var rateObject: JSONObject
        rateObject = when (type) {
            1 -> {
                appData.currencyRateJson["quotes_yesterday"] as JSONObject
            }
            2 -> {
                appData.currencyRateJson["quotes_ninty"] as JSONObject
            }
            else -> {
                appData.currencyRateJson["quotes"] as JSONObject
            }
        }


        rate = rateObject[currencySymbol].toString().toDouble()
    }catch (e:JSONException){

    }
    return BigDecimal(rate)
}

fun floatFromFormat(floatString: String): String {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    var dotFormat = "."
    var groupFormat = ","
    when (settingManager.monetaryFormat) {
        1 -> {
            dotFormat = ","
            groupFormat = "."
        }
        2 -> {
            groupFormat = " "
        }
        3 -> {
            groupFormat = " "
            dotFormat = ","
        }
    }

    var resultString = floatString.replace(groupFormat, "")
    resultString = resultString.replace(dotFormat, ".")
    resultString = roundFloat(BigDecimal(resultString))
    return resultString
}

fun roundFloat(real: BigDecimal): String {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    var monetaryFormatString = "0"
    var decimalFormatString = ""

    when (settingManager.decimalFormat) {
        0 -> {
            decimalFormatString = ".##"
        }
        1 -> {
            decimalFormatString = ".###"
        }
        2 -> {
            decimalFormatString = ".####"
        }
        3 -> {
            decimalFormatString = ".#####"
        }
        4 -> {
            decimalFormatString = ".######"
        }
    }

    return decimalString(real, monetaryFormatString + decimalFormatString)
}

fun decimalString(real: BigDecimal): String {
    var monetaryFormatString = "#,##0"
    var decimalFormatString = ""
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    when (settingManager.decimalFormat) {
        0 -> {
            decimalFormatString = ".00"
        }
        1 -> {
            decimalFormatString = ".000"
        }
        2 -> {
            decimalFormatString = ".0000"
        }
        3 -> {
            decimalFormatString = ".00000"
        }
        4 -> {
            decimalFormatString = ".000000"
        }
    }

    var formattedString = decimalString(real, monetaryFormatString + decimalFormatString)

    var dotFormat = '.'
    var groupFormat = ','
    when (settingManager.monetaryFormat) {
        1 -> {
            dotFormat = ','
            groupFormat = '.'
        }
        2 -> {
            groupFormat = ' '
        }
        3 -> {
            groupFormat = ' '
            dotFormat = ','
        }
    }

    formattedString = formattedString.replace('.', '&')
    formattedString = formattedString.replace(',', groupFormat)
    formattedString = formattedString.replace('&', dotFormat)

    return formattedString
}

fun decimalStringEliminateZeros(real: BigDecimal): String {
    var monetaryFormatString = "#,##0"
    var decimalFormatString = ""
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    when (settingManager.decimalFormat) {
        0 -> {
            decimalFormatString = ".##"
        }
        1 -> {
            decimalFormatString = ".###"
        }
        2 -> {
            decimalFormatString = ".####"
        }
        3 -> {
            decimalFormatString = ".#####"
        }
        4 -> {
            decimalFormatString = ".######"
        }
    }

    var formattedString = decimalString(real, monetaryFormatString + decimalFormatString)

    var dotFormat = '.'
    var groupFormat = ','
    when (settingManager.monetaryFormat) {
        1 -> {
            dotFormat = ','
            groupFormat = '.'
        }
        2 -> {
            groupFormat = ' '
        }
        3 -> {
            groupFormat = ' '
            dotFormat = ','
        }
    }

    formattedString = formattedString.replace('.', '&')
    formattedString = formattedString.replace(',', groupFormat)
    formattedString = formattedString.replace('&', dotFormat)

    return formattedString
}

fun decimalString(real: BigDecimal, format: String): String {
    val decimalFormat = DecimalFormat(format)
    return decimalFormat.format(real)
}

fun getSettingDateTime(): String {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    var formatString =
    if (settingManager.dateFormat == 0) {
        "MM/dd/yyyy"
    }
    else {
        "dd/MM/yyyy"
    }

    val sdf = SimpleDateFormat(formatString)
    val date = Date()
    return "Updated: " + sdf.format(date)
}

fun swapSymbolToTop(currencyArray: ArrayList<Currency>, symbol: String) {
    var symbolIndex = setFavoriteSymbol(currencyArray, symbol)
    if (symbolIndex == -1) {
        return
    }
    Collections.swap(currencyArray, 0, symbolIndex)
}

fun setFavoriteSymbol(currencyArray: ArrayList<Currency>, symbol: String): Int {
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    for (index in 0 until currencyArray.size) {
        if (currencyArray[index].symbol == symbol) {
            currencyArray[index].favorite = true
            if (!settingManager.favoriteItems.contains(currencyArray[index].symbol)) {
                settingManager.favoriteItems.add(currencyArray[index].symbol)
                saveSettingsToPreference()
            }
            return index
        }
    }
    return -1
}

fun saveSettingsToPreference() {
    val context = CurrencyConverterApp.instance!!.applicationContext
    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    val prefs = context.getSharedPreferences("SETTING_PREF", 0).edit()
    prefs.putString("SETTING_SERIALIZE", Gson().toJson(settingManager, SettingManager::class.java))
    prefs.apply()
}

fun sortByFavorite(currencyArray: ArrayList<Currency>) {
    for (index1 in 0 until currencyArray.size - 1) {
        for (index2 in index1 + 1 until currencyArray.size) {
            if ((!currencyArray[index1].favorite && currencyArray[index2].favorite)
                || ((currencyArray[index1].favorite == currencyArray[index2].favorite) && (currencyArray[index1].symbol > currencyArray[index2].symbol))) {
                Collections.swap(currencyArray, index1, index2)
            }
        }
    }
}

fun favoriteSwapSymbol(currencyArray: ArrayList<Currency>, position: Int) {
    val favoriteSymbol = currencyArray[position]
    favoriteSymbol.favorite = !favoriteSymbol.favorite
    var newPosition = position
    if (!favoriteSymbol.favorite) {
        for (index in position + 1 until currencyArray.size) {
            if (currencyArray[index].favorite || currencyArray[index].symbol < favoriteSymbol.symbol) {
                Collections.swap(currencyArray, newPosition, index)
                newPosition = index
            }
            else {
                break
            }
        }
    }
    else {
        for (index in position - 1 downTo 1 step 1) {
            if (!currencyArray[index].favorite || currencyArray[index].symbol > favoriteSymbol.symbol) {
                Collections.swap(currencyArray, newPosition, index)
                newPosition = index
            }
            else {
                break
            }
        }
    }

    val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    if (settingManager.favoriteItems.contains(favoriteSymbol.symbol)) {
        settingManager.favoriteItems.remove(favoriteSymbol.symbol)
    }
    else {
        settingManager.favoriteItems.add(favoriteSymbol.symbol)
    }
//    settingManager.favoriteItems.clear()
//    for (currency in currencyArray) {
//        if (currency.favorite) {
//            settingManager.favoriteItems.add(currency.symbol)
//        }
//    }
    saveSettingsToPreference()
}