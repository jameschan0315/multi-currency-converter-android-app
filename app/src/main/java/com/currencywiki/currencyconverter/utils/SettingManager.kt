package com.currencywiki.currencyconverter.utils

import android.graphics.Color
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.model.CurrencyItem
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class SettingManager {
    var gradient = Color.argb(0xff, 0x4f, 0x7c, 0xcb)
    var gradientChanged = false
    var visualSize = 0 // 0: Small, 1: Medium, 2: Large
    var isOptimize = true
    var showCurrencyName = true
    var showCurrencySymbol = false

    var showCurrencyFlags = false
    var displayMultiConverter = false
    var dateFormat = 0 // 0: 'mm/dd/yy', 1: 'dd/mm/yy'
    var monetaryFormat = 0 // 0: '1,234.56', 1: '1.234.56', 2: '1234.56', 3: '1234,56'
    var decimalFormat = 0
    var valueFixed = "1"
    var selectedCurrencyItem: ArrayList<CurrencyItem>? =
        arrayListOf(CurrencyItem(Currency("United States Dollar", "us", "USD", "$"), BigDecimal(1.0)),
                    CurrencyItem(Currency("Euro", "eu", "EUR", "€"), BigDecimal(1.0)))
    var favoriteItems = arrayListOf("USD", "EUR", "GBP", "CAD", "MXN", "INR", "BTC")
    var themeType = 0 // 0: Light, 1: Dark
    var singleSymbol1 = "USD"
    var singleSymbol2 = "EUR"
    var singleValue = BigDecimal(1.0)
    var sampleRateJson = ""
    var singleValueSize = 0f
    var singleSymbolSize = 0f

    var isFirstAppInstall = false
    var isColorChange = false
    var appDayTime = Calendar.getInstance().timeInMillis
    var appOpenTime = 0
}