package com.currencywiki.currencyconverter.fragments

open class BasePageFragment(override val fragmentTag: String?) : BaseFragment() {
    fun getTitle(): String {
        return ""
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}