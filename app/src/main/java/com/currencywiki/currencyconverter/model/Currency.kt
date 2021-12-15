package com.currencywiki.currencyconverter.model

import com.currencywiki.currencyconverter.CurrencyConverterApp

class Currency {
    var name = ""
    var flag = ""
    var symbol = ""
    var currencySymbol = ""
    var selected = false
    var favorite = false

    constructor(symbol: String) {
        val appData = CurrencyConverterApp.instance?.appData
        this.symbol = symbol
        this.flag = appData?.currencyFlagMap?.get(symbol)!!
        this.currencySymbol = appData.currencySymbolMap?.get(symbol)!!
        this.name = appData.currencyNameMap[symbol].toString()
        this.selected = false
        this.favorite = false
    }

    constructor(name:String, flag: String, symbol: String, currencySymbol: String) {
        this.name = name
        this.flag = flag
        this.symbol = symbol
        this.currencySymbol = currencySymbol
        this.selected = false
        this.favorite = false
    }

    constructor(symbol: String, lateInit: Boolean) {
        this.symbol = symbol
    }
}