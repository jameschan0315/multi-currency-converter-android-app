package com.currencywiki.currencyconverter.adapter

interface CurrencyDropdownRecyclerViewAdapterListener {
    fun didSelectCurrency(position: Int, symbol: String)
    fun didSelectStar(position: Int, symbol: String)
}