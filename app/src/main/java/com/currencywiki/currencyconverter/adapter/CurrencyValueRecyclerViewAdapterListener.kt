package com.currencywiki.currencyconverter.adapter

import java.math.BigDecimal

interface CurrencyValueRecyclerViewAdapterListener {
    fun didValueChanged(position: Int, value: BigDecimal)
    fun didDeleteCurrency(position: Int)
}