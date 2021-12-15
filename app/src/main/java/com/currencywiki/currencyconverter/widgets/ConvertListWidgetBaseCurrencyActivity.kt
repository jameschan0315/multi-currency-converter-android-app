package com.currencywiki.currencyconverter.widgets

import android.app.Activity
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
import androidx.appcompat.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.BaseActivity
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

class ConvertListWidgetBaseCurrencyActivity : BaseActivity(), SearchView.OnQueryTextListener,
    CurrencyDropdownRecyclerViewAdapterListener {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var titleTextView: AppCompatTextView
    private lateinit var selectBaseCurrencyTextView: AppCompatTextView
    private lateinit var symbolTextView: AppCompatTextView
    private lateinit var symbolFlagImageView: CircleImageView
    private lateinit var amountTextView: AppCompatTextView
    private lateinit var amountEditText: AppCompatEditText
    private lateinit var layoutContainer: LinearLayout
    private lateinit var layoutActionBar: LinearLayout
    private lateinit var currencyButton: LinearLayout
    private lateinit var nextButton: AppCompatButton
    private lateinit var bubbleLayout: BubbleLayoutTouch
    private lateinit var currencyList: ArrayList<String>
    private var symbol = "USD"
    private lateinit var dropdownAdapter: CurrencyDropdownRecyclerViewAdapter
    private lateinit var dropdownSearchView: SearchView
    private var currencyDropDownItems = ArrayList<Currency>()
    private lateinit var currencySelectList: RecyclerView
    private val requestBuilder = GlideApp.with(CurrencyConverterApp.instance!!.applicationContext)
        .`as`(PictureDrawable::class.java)
        .placeholder(R.drawable.bg_white_round)
        .error(R.drawable.bg_white_round)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(SvgSoftwareLayerSetter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_convert_list_widget_base_currency)
        bindControls()
    }

    private fun bindControls() {

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

        titleTextView = findViewById(R.id.txt_title)
        selectBaseCurrencyTextView = findViewById(R.id.txt_select_base_currency)
        layoutContainer = findViewById(R.id.container)
        layoutActionBar = findViewById(R.id.layout_action_bar)
        symbolTextView = findViewById(R.id.txt_symbol)
        amountTextView = findViewById(R.id.txt_amount)
        amountEditText = findViewById(R.id.edit_amount)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selectHandle = amountEditText.textSelectHandle
            if (selectHandle != null) {
                setDrawableTint(selectHandle, getARGBColor(settingManager.gradient))
            }
            val leftSelectHandle = amountEditText.textSelectHandleLeft
            if (leftSelectHandle != null) {
                setDrawableTint(leftSelectHandle, getARGBColor(settingManager.gradient))
            }
            val rightSelectHandle = amountEditText.textSelectHandleRight
            if (rightSelectHandle != null) {
                setDrawableTint(rightSelectHandle, getARGBColor(settingManager.gradient))
            }
            val cursorDrawable = amountEditText.textCursorDrawable
            if (cursorDrawable != null) {
                setDrawableTint(cursorDrawable, getARGBColor(settingManager.gradient))
            }
        }
        else {
            try {
                EditTextTint.applyColor(amountEditText, getARGBColor(settingManager.gradient))
            } catch (e: EditTextTint.EditTextTintError) {
                e.printStackTrace()
            }
        }
        setCursorPointerColor(amountEditText, getARGBColor(settingManager.gradient))
        setCursorDrawableColorNew(amountEditText, getARGBColor(settingManager.gradient))

        symbolFlagImageView = findViewById(R.id.img_flag)
        findViewById<AppCompatImageButton>(R.id.btn_back).setOnClickListener {
            setResult(Activity.RESULT_CANCELED, null)
            finish()
        }

        nextButton = findViewById(R.id.btn_next)
        nextButton.setOnClickListener {
            val context = this@ConvertListWidgetBaseCurrencyActivity

            val amount = getAmount()
            if (amount == null) {
                showError("Please input amount correctly.")
                return@setOnClickListener
            }

            // When the button is clicked, store the string locally
            val baseCurrency = symbolTextView.text.toString()

            saveBaseCurrencyToPref(context, appWidgetId, baseCurrency, amount)

            val args = Bundle()
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            showActivity(ConvertListWidgetConfigureActivity::class.java, args, 2)
        }

        //
        // Configure spinner
        //

        bubbleLayout = findViewById(R.id.bubble_layout)
        val appData = CurrencyConverterApp.instance?.appData
        val configManager = appData!!.settingManager

        dropdownSearchView = findViewById(R.id.search_view)
        dropdownSearchView.setOnQueryTextListener(this)
        currencySelectList = findViewById(R.id.currency_list)
        dropdownAdapter = CurrencyDropdownRecyclerViewAdapter()
        dropdownAdapter.adapterListener = this
        currencySelectList.adapter = dropdownAdapter
        currencyDropDownItems = ArrayList()
        for ((key, _) in appData.currencyFlagMap.entries) {
            val currencyAdded = Currency(key)
            if (configManager.favoriteItems.contains(key)) {
                currencyAdded.favorite = true
            }
            currencyDropDownItems.add(currencyAdded)
        }
//        setFavoriteSymbol(currencyDropDownItems, symbol)
        sortByFavorite(currencyDropDownItems)
        dropdownAdapter.currencyItemArray = currencyDropDownItems
        dropdownAdapter.requestBuilder = requestBuilder
        dropdownAdapter.notifyDataSetChanged()

        currencyButton = findViewById(R.id.btn_currency)
        currencyButton.setOnClickListener {
            dropdownSearchView.setQuery("", false)
            bubbleLayout.showWithAnchor(currencyButton)
        }
        selectCurrencySpinner(symbol)

        layoutContainer.background = getGradientDrawable(settingManager.gradient)
        layoutActionBar.setBackgroundColor(getARGBColor(settingManager.gradient))
        nextButton.setBackgroundColor(getARGBColor(settingManager.gradient))
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
            titleTextView.setTextColor(Color.DKGRAY)
            selectBaseCurrencyTextView.setTextColor(Color.DKGRAY)
            amountTextView.setTextColor(Color.DKGRAY)
            symbolTextView.setTextColor(Color.WHITE)
            amountEditText.setTextColor(Color.WHITE)
            currencyButton.setBackgroundResource(R.drawable.bg_dark_gray_round)
            amountEditText.setBackgroundResource(R.drawable.bg_dark_gray_round)
            nextButton.setTextColor(Color.DKGRAY)
        }
        else {
            titleTextView.setTextColor(Color.WHITE)
            selectBaseCurrencyTextView.setTextColor(Color.WHITE)
            amountTextView.setTextColor(Color.WHITE)
            symbolTextView.setTextColor(Color.DKGRAY)
            amountEditText.setTextColor(Color.DKGRAY)
            currencyButton.setBackgroundResource(R.drawable.bg_white_round)
            amountEditText.setBackgroundResource(R.drawable.bg_white_round)
            nextButton.setTextColor(Color.WHITE)
        }
    }

    private fun selectCurrencySpinner(currencySymbol: String) {
        val appData = CurrencyConverterApp.instance?.appData
        val flagResourceName = appData!!.currencyFlagMap[currencySymbol]

//        setFavoriteSymbol(dropdownAdapter.currencyItemArray, currencySymbol)
        symbol = currencySymbol
        symbolTextView.text = currencySymbol
        if (flagResourceName != null) {
            setSVGImageToImageView(requestBuilder, symbolFlagImageView, flagResourceName)
        }
    }

    private fun getAmount(): Float? {
        return try {
            amountEditText.text.toString().toFloat()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                // It is the responsibility of the configuration activity to update the app widget
                val appWidgetManager = AppWidgetManager.getInstance(this)
                updateAppWidget(this, appWidgetManager, appWidgetId, false)

                // Make sure we pass back the original appWidgetId
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val rect = Rect()
        bubbleLayout.getGlobalVisibleRect(rect)
        if (bubbleLayout.visibility == View.GONE) {
            return super.dispatchTouchEvent(ev)
        }

        if (!rect.contains(ev.x.toInt(), ev.y.toInt())) {
            bubbleLayout.visibility = View.GONE
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
        selectCurrencySpinner(symbol)
        sortByFavorite(dropdownAdapter.currencyItemArray)
//        swapSymbolToTop(dropdownAdapter.currencyItemArray, symbol)
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

private const val PREFS_NAME = "com.currency.android.widgets.ConvertListWidget"
private const val PREF_BASE_CURRENCY = "BASE_CURRENCY_"
private const val PREF_AMOUNT = "BASE_AMOUNT_"

internal fun saveBaseCurrencyToPref(context: Context, appWidgetId: Int, baseCurrency: String, amount: Float) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_BASE_CURRENCY + appWidgetId, baseCurrency)
    prefs.putFloat(PREF_AMOUNT + appWidgetId, amount)
    prefs.apply()
}

internal fun loadBaseCurrency(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val baseCurrency = prefs.getString(PREF_BASE_CURRENCY + appWidgetId, null)
    if (baseCurrency != null) {
        return baseCurrency
    }
    return ""
}

internal fun loadAmount(context: Context, appWidgetId: Int): Double {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val baseCurrency = prefs.getFloat(PREF_AMOUNT + appWidgetId, 0.0F).toDouble()
    if (baseCurrency != null) {
        return baseCurrency
    }
    return 0.0
}