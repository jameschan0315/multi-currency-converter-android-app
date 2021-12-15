package com.currencywiki.currencyconverter.adapter

interface CurrencyRecyclerViewAdapterListener {
    fun didSelectCurrency(position: Int, selected: Boolean)
}