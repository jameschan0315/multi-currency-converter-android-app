package com.currencywiki.currencyconverter.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.AddCurrencyActivity
import com.currencywiki.currencyconverter.activities.BaseActivity
import com.currencywiki.currencyconverter.adapter.CurrencyValueRecyclerViewAdapter
import com.currencywiki.currencyconverter.adapter.CurrencyValueRecyclerViewAdapterListener
import com.currencywiki.currencyconverter.adapter.ItemMoveCallback
import com.currencywiki.currencyconverter.common.MyKeyboard
import com.currencywiki.currencyconverter.common.getGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.CurrencyItem
import com.currencywiki.currencyconverter.utils.*
import java.lang.Exception
import java.math.BigDecimal


class ConvertListFragment : BasePageFragment(ConvertListFragment::class.simpleName),
    CurrencyValueRecyclerViewAdapterListener, ConvertListFragmentInterface, MyKeyboardInterface {

    private lateinit var updateDateTextView: AppCompatTextView
    lateinit var currencyList: RecyclerView
    private lateinit var currencyValueItemAdapter: CurrencyValueRecyclerViewAdapter
    private lateinit var keyboard: MyKeyboard
    private lateinit var addButton: AppCompatButton
    private lateinit var shareButton: AppCompatImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_convert_list, container, false)
        bindControls(view)
        return view
    }

    override fun bindControls(view: View) {
        super.bindControls(view)

        //
        // Init keyboard
        //

        keyboard = view.findViewById(R.id.keyboard) as MyKeyboard
        keyboard.keyboardInterface = this
        addButton = view.findViewById(R.id.btn_add)
        updateDateTextView = view.findViewById(R.id.txt_update_date)
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

        //
        // Init list
        //

        currencyValueItemAdapter = CurrencyValueRecyclerViewAdapter()
        currencyList = view.findViewById(R.id.currencyList)
        currencyValueItemAdapter.keyboard = keyboard
        currencyValueItemAdapter.context = requireContext()
        currencyValueItemAdapter.adapterListener = this
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(currencyValueItemAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(currencyList)
        currencyList.adapter = currencyValueItemAdapter

        reloadEntireRate()

        addButton.setOnClickListener {
            CurrencyConverterApp.instance!!.appData.convertListFragmentInterface = this
            (activity as BaseActivity).showActivity(
                AddCurrencyActivity::class.java,
                R.anim.slide_in_right,
                false
            )
        }

        applySetting()
    }

    fun reloadEntireRate() {
        if (!this::currencyValueItemAdapter.isInitialized) {
            return
        }

        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var value = BigDecimal(1.0)
        if (settingManager.selectedCurrencyItem != null && settingManager.selectedCurrencyItem!!.size > 0) {
            value = settingManager.selectedCurrencyItem!![0].value
        }
        reloadValueChanged(0, value, true)
    }

    override fun applySetting() {
        super.applySetting()

        if (!this::currencyValueItemAdapter.isInitialized) {
            return
        }

        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
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

        updateDateTextView.text = getSettingDateTime()
        currencyValueItemAdapter.notifyDataSetChanged()
        addButton.background = getGradientDrawable(settingManager.gradient, 0, 1)
        keyboard.applySetting()

        val shareDrawable = shareButton.drawable
        if (isDarkMode()) {
            updateDateTextView.setTextColor(Color.DKGRAY)
            addButton.setTextColor(Color.DKGRAY)
            setDrawableTint(shareDrawable, Color.DKGRAY)
        } else {
            updateDateTextView.setTextColor(Color.WHITE)
            addButton.setTextColor(Color.WHITE)
            setDrawableTint(shareDrawable, Color.WHITE)
        }
    }

    override fun onBackPressed(): Boolean {
        if (keyboard.visibility != View.VISIBLE) {
            return false
        }
        currencyValueItemAdapter.focusEditText?.clearFocus()
        keyboard.visibility = View.GONE
        currencyValueItemAdapter.focusEditText = null
        reloadCurrencyList(currencyValueItemAdapter.focusedPosition)
        return true
    }

    private fun reloadCurrencyList(position: Int) {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var currencyList = settingManager.selectedCurrencyItem
        currencyValueItemAdapter.currencyItemArray = currencyList!!

        for (index in 0 until currencyList.size) {
            if (position == index) {
                continue
            }
            currencyValueItemAdapter.notifyItemChanged(index)
        }
    }

    private fun reloadValueChanged(position: Int, value: BigDecimal, force: Boolean = false) {
        if (currencyValueItemAdapter.focusedPosition == -1 && !force) {
            currencyValueItemAdapter.focusedPosition = position
        }

        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var currencyList = settingManager.selectedCurrencyItem!!
        try {
            val baselineItem = currencyList[position]
            var valueChanged = false
            var standardDecimal = floatFromFormat(decimalString(baselineItem.value))
            if (standardDecimal != value.toString()) {
                baselineItem.value = value
                valueChanged = true
            }

            if (valueChanged) {
                currencyValueItemAdapter.focusedPosition = position
            }
            for (index in 0 until currencyList.size) {
                if (index == position) {
                    continue
                }
                val currencyItem = currencyList[index]
                val rate = getCurrentCurrencyRate(
                    baselineItem.currency.symbol,
                    currencyItem.currency.symbol
                )
                currencyItem.value = baselineItem.value.times(rate)
            }

            reloadCurrencyList(position)
            saveSettingsToPreference()

        } catch (e: Exception) {

        }
    }

    override fun didValueChanged(position: Int, value: BigDecimal) {
        if (position == -1) {
            return
        }
        reloadValueChanged(position, value)
    }

    override fun didDeleteCurrency(position: Int) {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        val currencyItem = currencyValueItemAdapter.currencyItemArray[position]
        currencyItem.currency.selected = false
        settingManager.selectedCurrencyItem!!.removeAt(position)
        currencyValueItemAdapter.notifyItemRemoved(position)
        saveSettingsToPreference()
    }

    override fun selectedCurrencyItemsChanged() {
        val beforeCurrencyList = currencyValueItemAdapter.currencyItemArray
        var baselineIndex = 0
        var baseCurrency: CurrencyItem? = null
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        if (beforeCurrencyList.isNotEmpty()) {
            baseCurrency = beforeCurrencyList[0]
            for (index in 0 until settingManager.selectedCurrencyItem!!.size) {
                val currencyItem = settingManager.selectedCurrencyItem!![index]
                if (currencyItem.currency.symbol == baseCurrency.currency.symbol) {
                    baselineIndex = index
                    break
                }
            }
        }

        var value = BigDecimal(1.0)
        if (baseCurrency != null) {
            value = baseCurrency.value
        }
        reloadValueChanged(baselineIndex, value, true)
        currencyValueItemAdapter.notifyDataSetChanged()
        saveSettingsToPreference()
    }

    override fun didClickEqual() {
        currencyValueItemAdapter.focusEditText?.clearFocus()
        currencyValueItemAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        isKill = false
    }
}