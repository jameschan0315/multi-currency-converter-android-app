package com.currencywiki.currencyconverter.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity.Companion.fromAddActivity
import com.currencywiki.currencyconverter.adapter.CurrencyRecyclerViewAdapter
import com.currencywiki.currencyconverter.adapter.CurrencyRecyclerViewAdapterListener
import com.currencywiki.currencyconverter.common.*
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.model.CurrencyItem
import com.currencywiki.currencyconverter.utils.setDrawableTint
import java.math.BigDecimal

class AddCurrencyActivity : BaseActivity(), CurrencyRecyclerViewAdapterListener {
    private lateinit var backButton: AppCompatImageButton
    private lateinit var selectedCurrencyList: RecyclerView
    private lateinit var selectedCurrencyItemAdapter: CurrencyRecyclerViewAdapter
    private lateinit var currencyList: RecyclerView
    private lateinit var currencyItemAdapter: CurrencyRecyclerViewAdapter
    private lateinit var titleTextView: AppCompatTextView
    private lateinit var selectedTextView: AppCompatTextView
    private lateinit var currencyListTextView: AppCompatTextView
    private lateinit var layoutActionBar: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_currency)

        //
        // Set language
        //

        layoutActionBar = findViewById(R.id.layout_action_bar)
        titleTextView = findViewById(R.id.txt_title)
        selectedTextView = findViewById(R.id.txt_selected)
        currencyListTextView = findViewById(R.id.txt_currency_list)
        val nameMap = CurrencyConverterApp.instance?.appData?.currencyNameMap
        titleTextView.text = nameMap!!["Add Currency"]
        selectedTextView.text = nameMap["Selected"]
        currencyListTextView.text = nameMap["Currency List"]
        backButton = findViewById(R.id.btn_back)

        //
        // Add events
        //

        backButton.setOnClickListener {
            //  startActivity(Intent(this, MainActivity::class.java))
            fromAddActivity = true
            finish()
        }

        //
        // Set recycler view
        //

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

        applySetting()
    }

    private fun applySetting() {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        if (!this::layoutActionBar.isInitialized) {
            return
        }

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
        layoutActionBar.setBackgroundColor(getARGBColor(settingManager.gradient))
        findViewById<View>(R.id.container).background = getGradientDrawable(settingManager.gradient)
        val backButtonDrawable = backButton.drawable
        if (isDarkMode()) {
            setDrawableTint(backButtonDrawable, Color.DKGRAY)
            titleTextView.setTextColor(Color.DKGRAY)
            selectedTextView.setTextColor(Color.DKGRAY)
            currencyListTextView.setTextColor(Color.DKGRAY)
        } else {
            setDrawableTint(backButtonDrawable, Color.WHITE)
            titleTextView.setTextColor(Color.WHITE)
            selectedTextView.setTextColor(Color.WHITE)
            currencyListTextView.setTextColor(Color.WHITE)
        }
    }

    private fun reloadSelectedCurrencyList() {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        val currencyList = ArrayList<Currency>()
        for (currencyItem in settingManager.selectedCurrencyItem!!) {
            currencyList.add(currencyItem.currency)
        }

        selectedCurrencyItemAdapter.currencyItemArray = currencyList
        selectedCurrencyItemAdapter.notifyDataSetChanged()
    }

    private fun reloadCurrencyList() {
        val unSelectedItems = CurrencyConverterApp.instance!!.appData.getCurrencyItems(false)
        val selectedItems = CurrencyConverterApp.instance!!.appData.settingManager.selectedCurrencyItem!!
        val currencyList = ArrayList<Currency>()
        for (unSelectedItem in unSelectedItems) {
                currencyList.add(unSelectedItem.currency)
        }
        Log.e("AddCurrency", "currencyItem>> ${currencyList.size} selectedItems>> ${selectedItems.size}")
        val list = mutableListOf<Currency>()
        currencyList.forEach { unselected->
            var isSelected = true
            for(selected in selectedItems) {
                if (selected.currency.name == unselected.name){
                    Log.e("AddCurrency", "removeItem>> ${selected.currency.name} currencyItem>> ${unselected.name}")
                    isSelected = false
                    break
                }
            }
            if (isSelected){
                list.add(unselected)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Log.e("AddCurrency", "list>> ${list.size} selectedItems>> ${selectedItems.size}")
            currencyItemAdapter.currencyItemArray = list as ArrayList<Currency>
            currencyItemAdapter.notifyDataSetChanged()
        }, 500)

    }

    override fun didSelectCurrency(position: Int, selected: Boolean) {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var currencyItem: Currency
        Log.e("AddCurrency", "selected>> ${selected} ")
        if (selected) {
            currencyItem = selectedCurrencyItemAdapter.currencyItemArray[position]
            currencyItem.selected = false
            settingManager.selectedCurrencyItem!!.removeAt(position)
            selectedCurrencyItemAdapter.currencyItemArray.removeAt(position)
            selectedCurrencyItemAdapter.notifyItemRemoved(position)

            currencyItemAdapter.currencyItemArray.add(0, currencyItem)
            currencyItemAdapter.notifyItemInserted(0)
            currencyItemAdapter.notifyDataSetChanged()
        } else {
            currencyItem = currencyItemAdapter.currencyItemArray[position]
            currencyItem.selected = true
            currencyItemAdapter.currencyItemArray.removeAt(position)
            currencyItemAdapter.notifyItemRemoved(position)

            settingManager.selectedCurrencyItem!!.add(
                settingManager.selectedCurrencyItem!!.size,
                CurrencyItem(currencyItem, BigDecimal(1.0))
            )
            selectedCurrencyItemAdapter.currencyItemArray.add(
                selectedCurrencyItemAdapter.currencyItemArray.size,
                currencyItem
            )
            selectedCurrencyItemAdapter.notifyItemInserted(selectedCurrencyItemAdapter.currencyItemArray.size)
        }

        val appData = CurrencyConverterApp.instance!!.appData
        appData.convertListFragmentInterface?.selectedCurrencyItemsChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // startActivity(Intent(this, MainActivity::class.java))
        // finish()
    }
}