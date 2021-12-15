package com.currencywiki.currencyconverter.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.adapter.CurrencyDropdownRecyclerViewAdapter
import com.currencywiki.currencyconverter.adapter.CurrencyDropdownRecyclerViewAdapterListener
import com.currencywiki.currencyconverter.common.BubbleLayoutTouch
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.getGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.*
import com.currencywiki.currencyconverter.utils.glide.GlideApp
import com.currencywiki.currencyconverter.utils.glide.SvgSoftwareLayerSetter
import de.hdodenhof.circleimageview.CircleImageView

/**
 * The configuration screen for the [SingleConvertWidget] AppWidget.
 */
class SingleConvertWidgetConfigureActivity : AppCompatActivity(), SearchView.OnQueryTextListener,
    CurrencyDropdownRecyclerViewAdapterListener {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var onClickListener = View.OnClickListener {
        val context = this@SingleConvertWidgetConfigureActivity

        // When the button is clicked, store the string locally
        saveSymbolsPref(context, appWidgetId, symbol1, symbol2)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateSingleConvertAppWidget(context, appWidgetManager, appWidgetId, false)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private lateinit var layoutContainer: LinearLayout
    private lateinit var configureTextView: AppCompatTextView
    private lateinit var symbol1TextView: AppCompatTextView
    private lateinit var symbol2TextView: AppCompatTextView
    private lateinit var symbol1FlagImageView: CircleImageView
    private lateinit var symbol2FlagImageView: CircleImageView
    private lateinit var currencyList: ArrayList<String>
    private lateinit var addWidgetButton: AppCompatButton
    private lateinit var currencyButton1: LinearLayout
    private lateinit var currencyButton2: LinearLayout
    private lateinit var bubbleLayout: BubbleLayoutTouch
    private lateinit var currencySelectList: RecyclerView
    private var selectedSpinner = 0
    private lateinit var dropdownAdapter: CurrencyDropdownRecyclerViewAdapter
    private lateinit var dropdownSearchView: SearchView
    private var currencyDropDownItems = ArrayList<Currency>()

    private var symbol1 = "EUR"
    private var symbol2 = "USD"
    private val requestBuilder = GlideApp.with(CurrencyConverterApp.instance!!.applicationContext)
        .`as`(PictureDrawable::class.java)
        .placeholder(R.drawable.bg_white_round)
        .error(R.drawable.bg_white_round)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(SvgSoftwareLayerSetter())

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.single_convert_widget_configure)
        addWidgetButton = findViewById(R.id.add_button)
        addWidgetButton.setOnClickListener(onClickListener)
        symbol1TextView = findViewById(R.id.txt_symbol1)
        symbol2TextView = findViewById(R.id.txt_symbol2)
        symbol1FlagImageView = findViewById(R.id.img_symbol1)
        symbol2FlagImageView = findViewById(R.id.img_symbol2)
        configureTextView = findViewById(R.id.txt_configure)
        configSpinner()

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

//        appWidgetText.setText(loadSingleConvertTitlePref(this@SingleConvertWidgetConfigureActivity, appWidgetId))

        layoutContainer = findViewById(R.id.layout_container)
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        layoutContainer.background = getGradientDrawable(settingManager.gradient)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getARGBColor(settingManager.gradient)
        }

        addWidgetButton.setBackgroundColor(getARGBColor(settingManager.gradient))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            if (isDarkMode()) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decor.systemUiVisibility = 0
            }
        }
        if (isDarkMode()) {
            configureTextView.setTextColor(Color.DKGRAY)
            addWidgetButton.setTextColor(Color.DKGRAY)
            symbol1TextView.setTextColor(Color.WHITE)
            symbol2TextView.setTextColor(Color.WHITE)
            currencyButton1.setBackgroundResource(R.drawable.bg_dark_gray_round)
            currencyButton2.setBackgroundResource(R.drawable.bg_dark_gray_round)
        }
        else {
            configureTextView.setTextColor(Color.WHITE)
            addWidgetButton.setTextColor(Color.WHITE)
            symbol1TextView.setTextColor(Color.DKGRAY)
            symbol2TextView.setTextColor(Color.DKGRAY)
            currencyButton1.setBackgroundResource(R.drawable.bg_white_round)
            currencyButton2.setBackgroundResource(R.drawable.bg_white_round)
        }
    }

    private fun configSpinner() {
        val appData = CurrencyConverterApp.instance?.appData

        bubbleLayout = findViewById(R.id.bubble_layout)
        val settingManager = appData!!.settingManager
        dropdownSearchView = findViewById(R.id.search_view)
        dropdownSearchView.setOnQueryTextListener(this)
        currencySelectList = findViewById(R.id.currency_list)
        dropdownAdapter = CurrencyDropdownRecyclerViewAdapter()
        dropdownAdapter.adapterListener = this
        currencySelectList.adapter = dropdownAdapter
        currencyDropDownItems = ArrayList()
        for ((key, _) in appData.currencyFlagMap.entries) {
            val currencyAdded = Currency(key)
            if (settingManager.favoriteItems.contains(key)) {
                currencyAdded.favorite = true
            }
            currencyDropDownItems.add(currencyAdded)
        }
//        setFavoriteSymbol(currencyDropDownItems, symbol1)
//        setFavoriteSymbol(currencyDropDownItems, symbol2)
        sortByFavorite(currencyDropDownItems)
        dropdownAdapter.currencyItemArray = currencyDropDownItems
        dropdownAdapter.requestBuilder = requestBuilder
        dropdownAdapter.notifyDataSetChanged()

        currencyButton1 = findViewById(R.id.btn_currency1)
        currencyButton1.setOnClickListener {
            selectedSpinner = 0
            dropdownSearchView.setQuery("", false)
            bubbleLayout.showWithAnchor(currencyButton1)
        }
        currencyButton2 = findViewById(R.id.btn_currency2)
        currencyButton2.setOnClickListener {
            selectedSpinner = 1
            dropdownSearchView.setQuery("", false)
            bubbleLayout.showWithAnchor(currencyButton2)
        }

        //
        // Select default currency
        //

        selectCurrencySpinner(0, symbol1)
        selectCurrencySpinner(1, symbol2)
    }

    private fun selectCurrencySpinner(spinnerIndex: Int, currencySymbol: String) {
        val appData = CurrencyConverterApp.instance?.appData
        val settingManager = appData!!.settingManager
        val flagResourceName = appData!!.currencyFlagMap[currencySymbol]

//        setFavoriteSymbol(dropdownAdapter.currencyItemArray, currencySymbol)
        if (spinnerIndex == 0) {
            symbol1 = currencySymbol
            symbol1TextView.text = currencySymbol
            if (flagResourceName != null) {
                setSVGImageToImageView(requestBuilder, symbol1FlagImageView, flagResourceName)
            }
        }
        else {
            symbol2 = currencySymbol
            symbol2TextView.text = currencySymbol
            if (flagResourceName != null) {
                setSVGImageToImageView(requestBuilder, symbol2FlagImageView, flagResourceName)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val rect = Rect()
        bubbleLayout.getGlobalVisibleRect(rect)
        val currencyButton1Rect = Rect()
        currencyButton1.getGlobalVisibleRect(currencyButton1Rect)
        val currencyButton2Rect = Rect()
        currencyButton2.getGlobalVisibleRect(currencyButton2Rect)
        if (bubbleLayout.visibility == View.GONE) {
            return super.dispatchTouchEvent(ev)
        }

        if (!rect.contains(ev.x.toInt(), ev.y.toInt())) {
            bubbleLayout.visibility = View.GONE
            if (selectedSpinner == 0 && currencyButton2Rect.contains(ev.x.toInt(), ev.y.toInt())) {
                return super.dispatchTouchEvent(ev)
            }
            if (selectedSpinner == 1 && currencyButton1Rect.contains(ev.x.toInt(), ev.y.toInt())) {
                return super.dispatchTouchEvent(ev)
            }
            return false
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val filteredModelList: ArrayList<Currency> =
            filter(currencyDropDownItems, query!!)
        sortByFavorite(filteredModelList)
        dropdownAdapter.updateCurrencyItemArray(filteredModelList)
        dropdownAdapter.notifyDataSetChanged()
        return true
    }

    override fun didSelectCurrency(position: Int, symbol: String) {
        selectCurrencySpinner(selectedSpinner, symbol)
        sortByFavorite(dropdownAdapter.currencyItemArray)
        dropdownAdapter.notifyDataSetChanged()
        bubbleLayout.visibility = View.GONE
    }

    override fun didSelectStar(position: Int, symbol: String) {
        favoriteSwapSymbol(dropdownAdapter.currencyItemArray, position)
        dropdownAdapter.notifyDataSetChanged()
    }

    private fun filter(
        currencies: List<Currency>,
        query: String
    ): ArrayList<Currency> {
        val lowerCaseQuery = query.toLowerCase()
        val filteredModelList = ArrayList<Currency>()
        for (currency in currencies) {
            val code = currency.symbol.toLowerCase()
            val name = currency.name.toLowerCase()
            if (code.contains(lowerCaseQuery) || name.contains(lowerCaseQuery)) {
                filteredModelList.add(currency)
            }
        }
        return filteredModelList
    }
}

private const val PREFS_NAME = "com.currency.android.widgets.SingleConvertWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

internal fun saveSymbolsPref(context: Context, appWidgetId: Int, symbol1: String, symbol2: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId + "symbol1", symbol1)
    prefs.putString(PREF_PREFIX_KEY + appWidgetId + "symbol2", symbol2)
    prefs.apply()
}

internal fun loadSymbol1Pref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val symbol = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "symbol1", null)
    return symbol ?: "USD"
}

internal fun loadSymbol2Pref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val symbol = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "symbol2", null)
    return symbol ?: "USD"
}

internal fun deleteSingleConvertTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}

internal fun deleteSymbolsPref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId + "symbol1")
    prefs.remove(PREF_PREFIX_KEY + appWidgetId + "symbol2")
    prefs.apply()
}