package com.currencywiki.currencyconverter.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.*
import androidx.core.widget.CompoundButtonCompat
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.utils.decimalString
import com.currencywiki.currencyconverter.utils.getResourceIdByString
import com.currencywiki.currencyconverter.utils.isKill
import com.currencywiki.currencyconverter.utils.setDrawableTint
import java.math.BigDecimal

class DecimalSettingFragment : BasePageFragment(DecimalSettingFragment::class.simpleName) {

    private lateinit var monetaryTextView: AppCompatTextView
    private lateinit var decimalTextView: AppCompatTextView
    private lateinit var previewTextView: AppCompatTextView
    private lateinit var shareButton: AppCompatImageButton

    private var monetaryCheckBoxes = ArrayList<AppCompatCheckBox>()
    private var decimalCheckBoxes = ArrayList<AppCompatCheckBox>()
    private val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_decimal_setting, container, false)
        bindControls(view)
        return view
    }

    override fun bindControls(view: View) {
        super.bindControls(view)

        monetaryCheckBoxes.clear()
        for (index in 1..4) {
            val resourceId = getResourceIdByString("chk_monetary_$index")
            val checkBox = view.findViewById<AppCompatCheckBox>(resourceId)
            monetaryCheckBoxes.add(checkBox)
        }

        decimalCheckBoxes.clear()
        for (index in 1..6) {
            val resourceId = getResourceIdByString("chk_decimal_$index")
            val checkBox = view.findViewById<AppCompatCheckBox>(resourceId)
            decimalCheckBoxes.add(checkBox)
        }

        for (index in 0 until monetaryCheckBoxes.size) {
            val checkBox = monetaryCheckBoxes[index]
            checkBox.setOnClickListener {
                settingManager.monetaryFormat = index
                showSetting()
                (activity as MainActivity).applySetting()
            }
        }

        for (index in 0 until decimalCheckBoxes.size) {
            val checkBox = decimalCheckBoxes[index]
            checkBox.setOnClickListener {
                settingManager.decimalFormat = index
                showSetting()
                (activity as MainActivity).applySetting()
            }
        }

        previewTextView = view.findViewById(R.id.txt_preview)
        monetaryTextView = view.findViewById(R.id.txt_monetary_format)
        decimalTextView = view.findViewById(R.id.txt_decimal_format)
        val nameMap = CurrencyConverterApp.instance?.appData?.currencyNameMap
        monetaryTextView.text = nameMap!!["Monetary format"]
        decimalTextView.text = nameMap["Decimal format"]
        decimalCheckBoxes[5].text = nameMap["Don't show"]
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
        // Set decimal check
        //

        showSetting()
        applySetting()
    }

    override fun applySetting() {
        super.applySetting()

        if (!this::previewTextView.isInitialized) {
            return
        }

        var previewTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_36)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_38)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_40)
                }
            }

        previewTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, previewTextSize)

        var decimalTitleTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_18)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_20)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_22)
                }
            }

        monetaryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, decimalTitleTextSize)
        decimalTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, decimalTitleTextSize)

        var decimalSampleTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_20)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_22)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_24)
                }
            }

        for (monetaryCheckBox in monetaryCheckBoxes) {
            monetaryCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, decimalSampleTextSize)
        }
        for (decimalCheckBox in decimalCheckBoxes) {
            decimalCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, decimalSampleTextSize)
        }

        previewTextView.text = getPreview()

        val shareDrawable = shareButton.drawable
        if (isDarkMode()) {
            previewTextView.setTextColor(Color.DKGRAY)
            monetaryTextView.setTextColor(Color.DKGRAY)
            decimalTextView.setTextColor(Color.DKGRAY)
            for (monetaryCheckBox in monetaryCheckBoxes) {
                monetaryCheckBox.setTextColor(Color.DKGRAY)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.DKGRAY, Color.DKGRAY)
                CompoundButtonCompat.setButtonTintList(monetaryCheckBox, ColorStateList(states, colors))
            }
            for (decimalCheckBox in decimalCheckBoxes) {
                decimalCheckBox.setTextColor(Color.DKGRAY)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.DKGRAY, Color.DKGRAY)
                CompoundButtonCompat.setButtonTintList(decimalCheckBox, ColorStateList(states, colors))
            }
            setDrawableTint(shareDrawable, Color.DKGRAY)
        }
        else {
            previewTextView.setTextColor(Color.WHITE)
            monetaryTextView.setTextColor(Color.WHITE)
            decimalTextView.setTextColor(Color.WHITE)
            for (monetaryCheckBox in monetaryCheckBoxes) {
                monetaryCheckBox.setTextColor(Color.WHITE)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.WHITE, Color.WHITE)
                CompoundButtonCompat.setButtonTintList(monetaryCheckBox, ColorStateList(states, colors))
            }
            for (decimalCheckBox in decimalCheckBoxes) {
                decimalCheckBox.setTextColor(Color.WHITE)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.WHITE, Color.WHITE)
                CompoundButtonCompat.setButtonTintList(decimalCheckBox, ColorStateList(states, colors))
            }
            setDrawableTint(shareDrawable, Color.WHITE)
        }
    }

    private fun getPreview(): String {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        when (settingManager.decimalFormat) {
            0 -> {
                return decimalString(BigDecimal(123456.02))
            }
            1 -> {
                return decimalString(BigDecimal(123456.003))
            }
            2 -> {
                return decimalString(BigDecimal(123456.0004))
            }
            3 -> {
                return decimalString(BigDecimal(123456.00005))
            }
            4 -> {
                return decimalString(BigDecimal(123456.000006))
            }
            else -> {
                return decimalString(BigDecimal(123456))
            }
        }
    }

    private fun showSetting() {
        val monetary = settingManager.monetaryFormat
        val decimal = settingManager.decimalFormat

        for (monetaryCheckBox in monetaryCheckBoxes) {
            monetaryCheckBox.isChecked = false
        }
        monetaryCheckBoxes[monetary].isChecked = true

        for (decimalCheckBox in decimalCheckBoxes) {
            decimalCheckBox.isChecked = false
        }
        decimalCheckBoxes[decimal].isChecked = true
    }

    override fun onResume() {
        super.onResume()
        isKill = false
    }
}