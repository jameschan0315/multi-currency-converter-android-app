package com.currencywiki.currencyconverter.model

import java.math.BigDecimal

class CurrencyItem {
    var currency: Currency
    var value: BigDecimal

    constructor(currency: Currency, value: BigDecimal) {
        this.currency = currency
        this.value = value
    }
}