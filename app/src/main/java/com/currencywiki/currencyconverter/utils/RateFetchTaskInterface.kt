package com.currencywiki.currencyconverter.utils

interface RateFetchTaskInterface {
    fun rateUpdated(success: Boolean)
    fun rateError(error: String)
}