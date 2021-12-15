package com.currencywiki.currencyconverter.fragments

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.getGradientDrawable


abstract class BaseFragment : Fragment() {
    private lateinit var containerView: LinearLayout

    fun showMessage(message: String?) {
        if (message == null || message.length == 0) {
            return
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessage(message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLongMessage(message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun showLongMessage(message: String?) {
        if (message == null || message.length == 0) {
            return
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    fun showError(message: Int) {
        showLongMessage(message)
    }

    fun showError(message: String?) {
        showLongMessage(message)
    }

    fun hideKeyboard() {
        val ctx: Context? = activity
        val inputManager =
            ctx!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val v = (ctx as Activity?)!!.currentFocus ?: return
        try {
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    open fun bindControls(view: View) {
        containerView = view.findViewById(R.id.container_view)
    }

    open fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    open fun applySetting() {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        if (!this::containerView.isInitialized) {
            return
        }
        containerView.background = getGradientDrawable(settingManager.gradient)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.window.statusBarColor = getARGBColor(settingManager.gradient)
        }

        applyDateSetting()
    }

    open fun applyDateSetting() {
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    abstract val fragmentTag: String?

    open fun initialize() {
    }
    fun loadDisplay() {}
}