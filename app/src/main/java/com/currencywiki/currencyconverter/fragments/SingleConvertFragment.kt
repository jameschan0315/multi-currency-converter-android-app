package com.currencywiki.currencyconverter.fragments

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.adapter.CurrencyDropdownRecyclerViewAdapter
import com.currencywiki.currencyconverter.adapter.CurrencyDropdownRecyclerViewAdapterListener
import com.currencywiki.currencyconverter.common.*
import com.currencywiki.currencyconverter.common.autofitedittext.AutofitEdittext
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.*
import com.currencywiki.currencyconverter.utils.glide.GlideApp
import com.currencywiki.currencyconverter.utils.glide.SvgSoftwareLayerSetter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_single_convert.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class SingleConvertFragment : BasePageFragment(SingleConvertFragment::class.java.simpleName), MyKeyboardInterface,
    CurrencyDropdownRecyclerViewAdapterListener, SearchView.OnQueryTextListener {
    private lateinit var symbol1TextView: AppCompatTextView
    private lateinit var symbol2TextView: AppCompatTextView
    private lateinit var updateDateTextView: AppCompatTextView
    private lateinit var symbol1FlagImageView: CircleImageView
    private lateinit var symbol2FlagImageView: CircleImageView
    private lateinit var flagImageView: CircleImageView
    lateinit var valueEditText: AutofitEdittext
    private lateinit var targetValueTextView: AutoResizeTextView
    private lateinit var currencyNameTextView: AppCompatTextView
    lateinit var targetCurrencyNameTextView: AutoResizeTextView
    lateinit var targetTextContainer: View
    private lateinit var inputContainerLayout: LinearLayout
    private lateinit var currency1Button: LinearLayout
    private lateinit var currency2Button: LinearLayout
    private lateinit var currencySelectList: RecyclerView
    private var bubbleLayout: BubbleLayoutTouch? = null
    private lateinit var shareButton: AppCompatImageButton

    private lateinit var currencyList: ArrayList<String>
    private var selectedSpinner = 0
    private lateinit var dropdownAdapter: CurrencyDropdownRecyclerViewAdapter
    private lateinit var dropdownSearchView: SearchView
    private lateinit var balloonContainer: LinearLayout
    private var currencyDropDownItems = ArrayList<Currency>()

//    lateinit var symbol1: String
//    lateinit var symbol2: String
//    var amount = BigDecimal(0.0)
    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_convert, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindControls(view)
        applySetting()
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (bubbleLayout==null){
            return super.dispatchTouchEvent(ev)
        }
        val rect = Rect()
        bubbleLayout?.getGlobalVisibleRect(rect)
        val currencyButton1Rect = Rect()
        currency1Button.getGlobalVisibleRect(currencyButton1Rect)
        val currencyButton2Rect = Rect()
        currency2Button.getGlobalVisibleRect(currencyButton2Rect)

        if (bubbleLayout?.visibility == View.GONE) {
            return super.dispatchTouchEvent(ev)
        }

        if (!rect.contains(ev.x.toInt(), ev.y.toInt())) {
            bubbleLayout?.visibility = View.GONE
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

    override fun applySetting() {
        super.applySetting()

        val appData = CurrencyConverterApp.instance!!.appData
        val settingManager = appData.settingManager
        if (!this::valueEditText.isInitialized) {
            return
        }

        var updateDateTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_14)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_16)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_18)
                }
            }

        updateDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, updateDateTextSize)

        var currencyCodeTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_16)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_18)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_20)
                }
            }

        currencyNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currencyCodeTextSize)

        var valueTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_22)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_24)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_26)
                }
            }

        valueEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueTextSize)

        var symbolTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_24)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_26)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_28)
                }
            }

        symbol1TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, symbolTextSize)
        symbol2TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, symbolTextSize)

        var targetValueTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_32)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_34)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_36)
                }
            }
        targetCurrencyNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, symbolTextSize)
        targetValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetValueTextSize)
        targetValueTextView.maxTextSize = targetValueTextView.textSize
        updateDateTextView.text = getSettingDateTime()
        if (settingManager.showCurrencyFlags) {
            flagImageView.visibility = View.VISIBLE
        }
        else {
            flagImageView.visibility = View.GONE
        }
        if (!settingManager.showCurrencyName && !settingManager.showCurrencySymbol) {
            currencyNameTextView.visibility = View.GONE
        }
        else {
            currencyNameTextView.visibility = View.VISIBLE
        }
        if (settingManager.showCurrencyName) {
            currencyNameTextView.text = settingManager.singleSymbol1
        }
        else {
            currencyNameTextView.text = appData.currencySymbolMap[settingManager.singleSymbol2]
        }
        valueEditText.setTextColor(getARGBColor(settingManager.gradient))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selectHandle = valueEditText.textSelectHandle
            if (selectHandle != null) {
                setDrawableTint(selectHandle, getARGBColor(settingManager.gradient))
            }
            val leftSelectHandle = valueEditText.textSelectHandleLeft
            if (leftSelectHandle != null) {
                setDrawableTint(leftSelectHandle, getARGBColor(settingManager.gradient))
            }
            val rightSelectHandle = valueEditText.textSelectHandleRight
            if (rightSelectHandle != null) {
                setDrawableTint(rightSelectHandle, getARGBColor(settingManager.gradient))
            }
            val cursorDrawable = valueEditText.textCursorDrawable
            if (cursorDrawable != null) {
                setDrawableTint(cursorDrawable, getARGBColor(settingManager.gradient))
            }
        }
        else {
            try {
                EditTextTint.applyColor(valueEditText, getARGBColor(settingManager.gradient))
            } catch (e: EditTextTint.EditTextTintError) {
                e.printStackTrace()
            }
        }
        setCursorPointerColor(valueEditText, getARGBColor(settingManager.gradient))
        setCursorDrawableColorNew(valueEditText, getARGBColor(settingManager.gradient))
        valueEditText.setHintTextColor(getARGBColor(settingManager.gradient))
        currencyNameTextView.background = getGradientDrawable(settingManager.gradient, 0, 0, 8.0F)

        val shareDrawable = shareButton.drawable
        if (isDarkMode()) {
            updateDateTextView.setTextColor(Color.DKGRAY)
            inputContainerLayout.setBackgroundResource(R.drawable.bg_dark_gray_round)
            valueEditText.setBackgroundColor(Color.DKGRAY)
            currencyNameTextView.setTextColor(Color.DKGRAY)
            currency1Button.setBackgroundResource(R.drawable.bg_dark_gray_round)
            currency2Button.setBackgroundResource(R.drawable.bg_dark_gray_round)
            symbol1TextView.setTextColor(Color.WHITE)
            symbol2TextView.setTextColor(Color.WHITE)
            targetValueTextView.setTextColor(Color.DKGRAY)
            targetCurrencyNameTextView.setTextColor(Color.DKGRAY)
            setDrawableTint(shareDrawable, Color.DKGRAY)
        }
        else {
            updateDateTextView.setTextColor(Color.WHITE)
            inputContainerLayout.setBackgroundResource(R.drawable.bg_white_round)
            valueEditText.setBackgroundColor(Color.WHITE)
            currencyNameTextView.setTextColor(Color.WHITE)
            currency1Button.setBackgroundResource(R.drawable.bg_white_round)
            currency2Button.setBackgroundResource(R.drawable.bg_white_round)
            symbol1TextView.setTextColor(Color.DKGRAY)
            symbol2TextView.setTextColor(Color.DKGRAY)
            targetValueTextView.setTextColor(Color.WHITE)
            targetCurrencyNameTextView.setTextColor(Color.WHITE)
            setDrawableTint(shareDrawable, Color.WHITE)
        }
        calculateRate()
    }

    private fun reloadFavoriteItems() {
        val appData = CurrencyConverterApp.instance?.appData
        val settingManager = appData!!.settingManager
        var itemsChanged = false
        for (currencyDropDownItem in dropdownAdapter.currencyItemArray) {
            val dropDownFavorite = settingManager.favoriteItems.contains(currencyDropDownItem.symbol)
            if (currencyDropDownItem.favorite != dropDownFavorite) {
                currencyDropDownItem.favorite = dropDownFavorite
                itemsChanged = true
            }
        }

        if (itemsChanged) {
            sortByFavorite(dropdownAdapter.currencyItemArray)
            dropdownAdapter.notifyDataSetChanged()
        }
    }

    private fun copyTargetToClipboard(): Boolean {
        val x = if (valueEditText.text.toString().trim().isEmpty()){
            "0.0"
        }else{
            valueEditText.text.toString()
        }
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        val formatter: DecimalFormat = NumberFormat.getInstance(
            Locale.US
        ) as DecimalFormat


        //val newValue = NumberFormat.getNumberInstance(Locale.getDefault()).format(x.toDouble())

        val clipboardText = decimalString(BigDecimal(x)) + " " + symbol1TextView.text.toString() +
                " = " + targetValueTextView.text.toString() + " " + symbol2TextView.text.toString() +
                " by Currency.wiki"

        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = clipboardText
        } else {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText(clipboardText, clipboardText)
            clipboard.setPrimaryClip(clip)
        }
        Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_LONG).show()
        return true
    }

    override fun bindControls(view: View) {
        super.bindControls(view)

        requestBuilder = GlideApp.with(this)
            .`as`(PictureDrawable::class.java)
            .placeholder(R.drawable.bg_white_round)
            .error(R.drawable.bg_white_round)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(SvgSoftwareLayerSetter())

        updateDateTextView = view.findViewById(R.id.txt_update_date)
        inputContainerLayout = view.findViewById(R.id.layout_input_container)
        flagImageView = view.findViewById(R.id.img_flag)
        symbol1TextView = view.findViewById(R.id.txt_symbol1)
        symbol2TextView = view.findViewById(R.id.txt_symbol2)
        symbol1FlagImageView = view.findViewById(R.id.img_symbol1)
        symbol2FlagImageView = view.findViewById(R.id.img_symbol2)
        valueEditText = view.findViewById(R.id.edit_value)
        targetCurrencyNameTextView = view.findViewById(R.id.txt_target_currency_name)
        targetTextContainer = view.findViewById(R.id.container_target_text)
        targetValueTextView = view.findViewById(R.id.txt_target_value)
        targetValueTextView.siblingTextView = targetCurrencyNameTextView
        targetValueTextView.container = targetTextContainer
        currencyNameTextView = view.findViewById(R.id.txt_currency_name)
        shareButton = view.findViewById(R.id.btn_share)
        shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "https://www.currencywiki.app")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        targetValueTextView.isLongClickable = true
        targetCurrencyNameTextView.isLongClickable = true
        targetValueTextView.setOnLongClickListener {
            copyTargetToClipboard()
        }
        targetCurrencyNameTextView.setOnLongClickListener {
            copyTargetToClipboard()
        }


        //
        // Configure invert button
        //

        view.findViewById<AppCompatImageView>(R.id.btn_invert).setOnClickListener {
            val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
            val symbolSwap = settingManager.singleSymbol1
            selectCurrencySpinner(0, settingManager.singleSymbol2)
            selectCurrencySpinner(1, symbolSwap)
        }

        //
        // Configure edit text
        //

        valueEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        valueEditText.setTextIsSelectable(true)
        val keyboard = view.findViewById(R.id.keyboard) as MyKeyboard
        val inputConnection = valueEditText.onCreateInputConnection(EditorInfo())
        keyboard.setInputConnection(inputConnection)
        keyboard.keyboardInterface = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            valueEditText.showSoftInputOnFocus = false
        } else {
            try {
                val method = AppCompatEditText::class.java.getMethod(
                    "setShowSoftInputOnFocus"
                    , *arrayOf<Class<*>?>(Boolean::class.javaPrimitiveType)
                )
                method.isAccessible = true
                method.invoke(valueEditText, false)
            } catch (e: Exception) {
            }
        }
        valueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                keyboard.visibility = View.VISIBLE
            }
            else {
                keyboard.visibility = View.GONE
            }
        }
        valueEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                calculateRate()
            }
        })
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        valueEditText.setText(roundFloat(settingManager.singleValue))

        //
        // Configure spinner
        //

        bubbleLayout = view.findViewById(R.id.bubble_layout)
        val appData = CurrencyConverterApp.instance?.appData

        dropdownSearchView = view.findViewById(R.id.search_view)
        balloonContainer = view.findViewById(R.id.balloon_container)
        dropdownSearchView.setOnQueryTextListener(this)
        currencySelectList = view.findViewById(R.id.currency_list)
        dropdownAdapter = CurrencyDropdownRecyclerViewAdapter()
        dropdownAdapter.adapterListener = this
        currencySelectList.adapter = dropdownAdapter
        currencyDropDownItems = ArrayList()
        for ((key, _) in appData!!.currencyFlagMap.entries) {
            val currencyAdded = Currency(key)
            if (settingManager.favoriteItems.contains(key)) {
                currencyAdded.favorite = true
            }
            currencyDropDownItems.add(currencyAdded)
        }
//        setFavoriteSymbol(currencyDropDownItems, settingManager.singleSymbol1)
//        setFavoriteSymbol(currencyDropDownItems, settingManager.singleSymbol2)
        sortByFavorite(currencyDropDownItems)
        dropdownAdapter.currencyItemArray = currencyDropDownItems
        dropdownAdapter.notifyDataSetChanged()

        currency1Button = view.findViewById(R.id.btn_currency1)
        currency2Button = view.findViewById(R.id.btn_currency2)
        var requestBuilder = GlideApp.with(CurrencyConverterApp.instance!!.applicationContext)
            .`as`(PictureDrawable::class.java)
            .placeholder(R.drawable.bg_white_round)
            .error(R.drawable.bg_white_round)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(SvgSoftwareLayerSetter())
        dropdownAdapter.requestBuilder = requestBuilder

        currency1Button.setOnClickListener {
            selectedSpinner = 0
            dropdownSearchView.setQuery("", false)
            reloadFavoriteItems()
            bubbleLayout?.showWithAnchor(currency1Button)
        }
        currency2Button.setOnClickListener {
            selectedSpinner = 1
            dropdownSearchView.setQuery("", false)
            reloadFavoriteItems()
            bubbleLayout?.showWithAnchor(currency2Button)
        }

        //
        // Select default currency
        //

        selectCurrencySpinner(0, settingManager.singleSymbol1)
        selectCurrencySpinner(1, settingManager.singleSymbol2)
    }

    override fun onStop() {
        bubbleLayout?.visibility = View.GONE
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

       // if (!this::targetValueTextView.isInitialized) {
       //     return
       // }
        val appData = CurrencyConverterApp.instance?.appData
        val settingManager = appData!!.settingManager
        val msDiff: Long = Calendar.getInstance().timeInMillis - settingManager.appDayTime
        val daysDiff: Long = TimeUnit.MILLISECONDS.toDays(msDiff)
        if (settingManager.appOpenTime == 5){
            isKill = false
        }else isKill = daysDiff < 7
       // targetCurrencyNameTextView.textSize = settingManager.singleSymbolSize
       // targetValueTextView.textSize = settingManager.singleValueSize
    }
    override fun onPause() {
        super.onPause()
        if (!this::targetValueTextView.isInitialized) {
            return
        }
       //val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
       //settingManager.singleSymbolSize = targetCurrencyNameTextView.textSize
       //settingManager.singleValueSize = targetValueTextView.textSize
    }
    override fun onBackPressed(): Boolean {
        if (keyboard.visibility != View.VISIBLE) {
            return false
        }
        valueEditText.clearFocus()
        keyboard.visibility = View.GONE
        return true
    }

    private fun selectCurrencySpinner(spinnerIndex: Int, currencySymbol: String) {
        Log.i("MainActivity","selectCurrencySpinner>>> $currencySymbol")
        val appData = CurrencyConverterApp.instance?.appData
        val settingManager = appData!!.settingManager
        val flagResourceName = appData.currencyFlagMap[currencySymbol]

//        setFavoriteSymbol(dropdownAdapter.currencyItemArray, currencySymbol)
        if (spinnerIndex == 0) {
            settingManager.singleSymbol1 = currencySymbol
            symbol1TextView.text = currencySymbol
            if (settingManager.showCurrencyName) {
                currencyNameTextView.text = settingManager.singleSymbol1
            }
            else {
                currencyNameTextView.text = appData.currencySymbolMap[settingManager.singleSymbol1]
            }
            if (flagResourceName != null) {
                setSVGImageToImageView(requestBuilder, flagImageView, flagResourceName)
                setSVGImageToImageView(requestBuilder, symbol1FlagImageView, flagResourceName)
            }
        }
        else {
            settingManager.singleSymbol2 = currencySymbol
            symbol2TextView.text = currencySymbol
            targetCurrencyNameTextView.text = currencySymbol
            if (flagResourceName != null) {
                Log.d("MainActivity", "name>>> $flagResourceName")
                setSVGImageToImageView(requestBuilder, symbol2FlagImageView, flagResourceName)
            }
        }

        calculateRate()
    }

    fun calculateRate() {
        if (!this::valueEditText.isInitialized) {
            return
        }
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        val rate = getCurrentCurrencyRate(settingManager.singleSymbol1, settingManager.singleSymbol2)

        try {
            settingManager.singleValue = BigDecimal(valueEditText.text.toString())
            saveSettingsToPreference()
            val targetValue = rate.times(settingManager.singleValue)
            targetValueTextView.text = decimalString(targetValue)
        }
        catch (error: NumberFormatException) {
            targetValueTextView.text = "0"
        }

    }

    override fun didClickEqual() {
        valueEditText.setText(roundFloat(BigDecimal(valueEditText.text.toString())))
        valueEditText.clearFocus()
        valueEditText.requestFocus()
        valueEditText.text?.length?.let { valueEditText.setSelection(it) }
    }

    override fun didSelectCurrency(position: Int, symbol: String) {
        selectCurrencySpinner(selectedSpinner, symbol)
        sortByFavorite(dropdownAdapter.currencyItemArray)
//        swapSymbolToTop(dropdownAdapter.currencyItemArray, symbol)
        dropdownAdapter.notifyDataSetChanged()
        bubbleLayout?.visibility = View.GONE
    }

    override fun didSelectStar(position: Int, symbol: String) {
        favoriteSwapSymbol(dropdownAdapter.currencyItemArray, position)
        dropdownAdapter.notifyDataSetChanged()
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