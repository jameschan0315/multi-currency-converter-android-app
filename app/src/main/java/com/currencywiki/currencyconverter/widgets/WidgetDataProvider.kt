package com.currencywiki.currencyconverter.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.decimalString
import com.currencywiki.currencyconverter.utils.getCircularBitmapFromSVGFileName
import com.currencywiki.currencyconverter.utils.getCurrentCurrencyRate
import com.currencywiki.currencyconverter.utils.getYesterdayCurrencyRate
import org.json.JSONArray
import java.math.BigDecimal

class WidgetDataProvider(context: Context?, intent: Intent?) :
    RemoteViewsService.RemoteViewsFactory {
    private var currencyItems: MutableList<Currency> = ArrayList()
    private var mContext: Context? = null
    private var mIntent: Intent? = null
    private var baseCurrency = "USD"
    private var amount = BigDecimal(0.0)

    override fun onCreate() {
        initData()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun onDataSetChanged() {
        initData()
    }

    override fun onDestroy() {}

    override fun getViewAt(position: Int): RemoteViews {
        val view = RemoteViews(
            mContext?.packageName,
            R.layout.item_widget_currency
        )

        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var symbolTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_22)
                }
                1 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_24)
                }
                else -> {
                    mContext?.resources?.getDimension(R.dimen.sp_26)
                }
            }

        view.setTextViewTextSize(R.id.txt_symbol, TypedValue.COMPLEX_UNIT_PX, symbolTextSize!!)
        var rateTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_17)
                }
                1 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_19)
                }
                else -> {
                    mContext?.resources?.getDimension(R.dimen.sp_21)
                }
            }

        view.setTextViewTextSize(R.id.txt_rate, TypedValue.COMPLEX_UNIT_PX, rateTextSize!!)
        var percentTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_13)
                }
                1 -> {
                    mContext?.resources?.getDimension(R.dimen.sp_15)
                }
                else -> {
                    mContext?.resources?.getDimension(R.dimen.sp_17)
                }
            }

        view.setTextViewTextSize(R.id.txt_percent, TypedValue.COMPLEX_UNIT_PX, percentTextSize!!)

        val currencyItem = currencyItems[position]
        view.setImageViewBitmap(R.id.img_flag, getCircularBitmapFromSVGFileName(currencyItem.flag))
        view.setTextViewText(R.id.txt_symbol, currencyItem.symbol)
        val rate = getCurrentCurrencyRate(baseCurrency, currencyItem.symbol)
        val rateYesterday = getYesterdayCurrencyRate(baseCurrency, currencyItem.symbol)

        view.setTextViewText(R.id.txt_rate, decimalString(rate.times(amount)))
        var percent = 0.0
        if (rateYesterday.toDouble() != 0.0) {
            percent = (rate.toDouble() - rateYesterday.toDouble()) /** 100 / rateYesterday.toDouble()*/
        }
        val percentLabel = decimalString(BigDecimal(percent), "0.0000")
        if (percent > 0) {
            view.setTextViewText(R.id.txt_percent, "+$percentLabel")
        }
        else {
            view.setTextViewText(R.id.txt_percent, percentLabel)
        }
        Log.d("WidgetApp", "percentLabel>> $percentLabel")
        if (percentLabel == "0.0000" || percentLabel == "-0.0000"){
            view.setViewVisibility(R.id.txt_percent, View.GONE)
        }else{
            view.setViewVisibility(R.id.txt_percent, View.VISIBLE)
        }

        val extras = Bundle()
        extras.putInt(EXTRA_ITEM, position)
        extras.putDouble(EXTRA_AMOUNT, amount.toDouble())
        extras.putString(EXTRA_SYMBOL1, baseCurrency)
        extras.putString(EXTRA_SYMBOL2, currencyItem.symbol)
        Log.i("MainActivity", "currencyItem.symbol>>> ${currencyItem.symbol}")
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        view.setOnClickFillInIntent(R.id.widget_item, fillInIntent)
        if (isDarkMode()) {
            view.setTextColor(R.id.txt_symbol, Color.DKGRAY)
            view.setTextColor(R.id.txt_rate, Color.DKGRAY)
            view.setTextColor(R.id.txt_percent, Color.DKGRAY)
        }
        else {
            view.setTextColor(R.id.txt_symbol, Color.WHITE)
            view.setTextColor(R.id.txt_rate, Color.WHITE)
            view.setTextColor(R.id.txt_percent, Color.WHITE)
        }
        return view
    }

    override fun getCount(): Int {
        return currencyItems.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun initData() {
        currencyItems.clear()
        var jsonItems = mIntent?.getStringExtra("jsonItems")

        val arr = JSONArray(jsonItems)
        for (index in 0 until arr.length()) {
            val symbol = arr.getString(index)
            currencyItems.add(Currency(symbol))
        }

        baseCurrency = mIntent?.getStringExtra("baseCurrency")!!
        amount = BigDecimal(mIntent?.getDoubleExtra("amount", 0.0)!!)
    }

    init {
        mContext = context
        mIntent = intent
    }
}