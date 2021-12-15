package com.currencywiki.currencyconverter.model

class CurrencyRate {
    var currency: String
    var rate: Float

    constructor(currency: String, rate: Float) {
        this.currency = currency
        this.rate = rate
    }
}