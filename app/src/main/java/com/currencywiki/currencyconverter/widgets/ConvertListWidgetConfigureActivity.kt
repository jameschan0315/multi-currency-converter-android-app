package com.currencywiki.currencyconverter.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.BaseActivity
import com.currencywiki.currencyconverter.adapter.CurrencyRecyclerViewAdapter
import com.currencywiki.currencyconverter.adapter.CurrencyRecyclerViewAdapterListener
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.getGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.Currency
import org.json.JSONArray

/**
 * The configuration screen for the [ConvertListWidget] AppWidget.
 */
open class ConvertListWidgetConfigureActivity : BaseActivity(),
    CurrencyRecyclerViewAdapterListener {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var onClickListener = View.OnClickListener {
        val context = this@ConvertListWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val selectedItems = selectedCurrencyItemAdapter.currencyItemArray
        saveItemsToPref(context, appWidgetId, selectedItems)

        setResult(RESULT_OK, null)
        finish()
    }

    private lateinit var selectedTextView: AppCompatTextView
    private lateinit var currencyListTextView: AppCompatTextView
    private lateinit var layoutContainer: LinearLayout
    private lateinit var selectedCurrencyList: RecyclerView
    lateinit var selectedCurrencyItemAdapter: CurrencyRecyclerViewAdapter
    lateinit var currencyList: RecyclerView
    lateinit var currencyItemAdapter: CurrencyRecyclerViewAdapter
    private lateinit var addWidgetButton: AppCompatButton

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        setContentView(R.layout.convert_list_widget_configure)
        addWidgetButton = findViewById(R.id.add_button)
        addWidgetButton.setOnClickListener(onClickListener)
        selectedTextView = findViewById(R.id.txt_selected)
        currencyListTextView = findViewById(R.id.txt_currency_list)

        configRecyclerView()

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        layoutContainer = findViewById(R.id.layout_container)
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        layoutContainer.background = getGradientDrawable(settingManager.gradient)
        addWidgetButton.setBackgroundColor(getARGBColor(settingManager.gradient))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getARGBColor(settingManager.gradient)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            if (isDarkMode()) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decor.systemUiVisibility = 0
            }
        }
        if (isDarkMode()) {
            selectedTextView.setTextColor(Color.DKGRAY)
            currencyListTextView.setTextColor(Color.DKGRAY)
            addWidgetButton.setTextColor(Color.DKGRAY)
        }
        else {
            selectedTextView.setTextColor(Color.WHITE)
            currencyListTextView.setTextColor(Color.WHITE)
            addWidgetButton.setTextColor(Color.WHITE)
        }
    }

    private fun configRecyclerView() {
        selectedCurrencyItemAdapter = CurrencyRecyclerViewAdapter()
        selectedCurrencyItemAdapter.isSelected = true
        selectedCurrencyItemAdapter.adapterListener = this
        selectedCurrencyList = findViewById(R.id.selectedCurrencyList)
        selectedCurrencyList.adapter = selectedCurrencyItemAdapter
        reloadSelectedCurrencyList()

        currencyItemAdapter = CurrencyRecyclerViewAdapter()
        currencyItemAdapter.isSelected = false
        currencyItemAdapter.adapterListener = this
        currencyList = findViewById(R.id.currencyList)
        currencyList.adapter = currencyItemAdapter
        reloadCurrencyList()
    }

    private fun reloadSelectedCurrencyList() {
        val selectedItems = CurrencyConverterApp.instance!!.appData.getCurrencyItems(true)
        val currencyList = ArrayList<Currency>()
        for (selectedItem in selectedItems) {
            currencyList.add(selectedItem.currency)
        }
        selectedCurrencyItemAdapter.currencyItemArray = currencyList
        selectedCurrencyItemAdapter.notifyDataSetChanged()
    }

    private fun reloadCurrencyList() {
        val unSelectedItems = CurrencyConverterApp.instance!!.appData.getCurrencyItems(false)
        val currencyList = ArrayList<Currency>()
        for (unSelectedItem in unSelectedItems) {
            currencyList.add(unSelectedItem.currency)
        }
        currencyItemAdapter.currencyItemArray = currencyList
        currencyItemAdapter.notifyDataSetChanged()
    }

    override fun didSelectCurrency(position: Int, selected: Boolean) {
        var currencyItem: Currency
        if (selected) {
            currencyItem = selectedCurrencyItemAdapter.currencyItemArray[position]
            selectedCurrencyItemAdapter.currencyItemArray.removeAt(position)
            selectedCurrencyItemAdapter.notifyItemRemoved(position)

            currencyItemAdapter.currencyItemArray.add(0, currencyItem)
            currencyItemAdapter.notifyItemInserted(0)
            currencyItemAdapter.notifyDataSetChanged()
        }
        else {
            currencyItem = currencyItemAdapter.currencyItemArray[position]
            currencyItemAdapter.currencyItemArray.removeAt(position)
            currencyItemAdapter.notifyItemRemoved(position)

            selectedCurrencyItemAdapter.currencyItemArray.add(selectedCurrencyItemAdapter.currencyItemArray.size, currencyItem)
            selectedCurrencyItemAdapter.notifyItemInserted(selectedCurrencyItemAdapter.currencyItemArray.size)
        }
    }
}

private const val PREFS_NAME = "com.currency.android.widgets.ConvertListWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

internal fun saveItemsToPref(context: Context, appWidgetId: Int, selectedItems: ArrayList<Currency>) {
    var symbolList = ArrayList<String>()
    for (selectedItem in selectedItems) {
        symbolList.add(selectedItem.symbol)
    }
    val jsArray = JSONArray(symbolList)
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, jsArray.toString())
    prefs.apply()
}

internal fun loadItemsPref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val jsonString = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    if (jsonString != null) {
        return jsonString
    }
    return ""
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}