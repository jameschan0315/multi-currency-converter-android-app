package com.currencywiki.currencyconverter.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.utils.isKill

class InfoFragment : BasePageFragment(InfoFragment::class.simpleName) {

    private lateinit var termsButton: AppCompatButton
    private lateinit var supportButton: AppCompatButton
    private lateinit var privacyButton: AppCompatButton
    private lateinit var supportFragment: InfoPageFragment
    private lateinit var termsFragment: InfoPageFragment
    private lateinit var privacyFragment: InfoPageFragment
    private lateinit var tabButtonsContainer: ConstraintLayout
    private lateinit var rightTabContainer: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        bindControls(view)
        applySetting()
        return view
    }

    override fun initialize() {
        this.replaceFirstFragment()
    }

    private fun replaceFirstFragment() {
        replaceFragment(2)
    }

    fun showSupport() {
        supportFragment.scrollToBottom = true
        replaceFirstFragment()
        supportFragment.scrollToBottom()
    }

    override fun applySetting() {
        super.applySetting()

        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        if (!this::termsButton.isInitialized) {
            return
        }
        supportButton.setTextColor(getARGBColor(settingManager.gradient))
        termsButton.setTextColor(getARGBColor(settingManager.gradient))
        privacyButton.setTextColor(getARGBColor(settingManager.gradient))
        if (isDarkMode()) {
            tabButtonsContainer.setBackgroundResource(R.drawable.bg_medium_gray_radius)
            supportButton.setBackgroundResource(R.drawable.bg_info_dark_tab)
            termsButton.background = null
            privacyButton.background = null
        } else {
            tabButtonsContainer.setBackgroundResource(R.drawable.bg_gray_radius)
            supportButton.setBackgroundResource(R.drawable.bg_info_tab)
            termsButton.background = null
            privacyButton.background = null
        }
        if (termsFragment != null) {
            termsFragment.applySetting()
        }
        if (privacyFragment != null) {
            privacyFragment.applySetting()
        }
        if (supportFragment != null) {
            supportFragment.applySetting()
        }
    }

    override fun bindControls(view: View) {
        super.bindControls(view)

        tabButtonsContainer = view.findViewById(R.id.container_tab_button)
        rightTabContainer = view.findViewById(R.id.container_right_tab)
        supportButton = view.findViewById(R.id.btn_support)
        termsButton = view.findViewById(R.id.btn_terms_conditions)
        privacyButton = view.findViewById(R.id.btn_privacy_policy)

        supportButton.setOnClickListener {
            if (supportButton.isSelected) {
                return@setOnClickListener
            }
            termsButton.background = null
            privacyButton.background = null
            if (isDarkMode())
                supportButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_dark_tab)
            else
                supportButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_tab)
            replaceFragment(2)
        }
        termsButton.setOnClickListener {
            if (termsButton.isSelected) {
                return@setOnClickListener
            }
            supportButton.background = null
            privacyButton.background = null
            if (isDarkMode())
                termsButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_dark_tab)
            else
                termsButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_tab)
            replaceFragment(0)
        }

        privacyButton.setOnClickListener {
            if (privacyButton.isSelected) {
                return@setOnClickListener
            }
            supportButton.background = null
            termsButton.background = null
            if (isDarkMode())
                privacyButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_dark_tab)
            else
                privacyButton.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_info_tab)
            replaceFragment(1)
        }
        supportFragment = InfoPageFragment()
        supportFragment.type = 2
        termsFragment = InfoPageFragment()
        termsFragment.type = 0
        privacyFragment = InfoPageFragment()
        privacyFragment.type = 1

        applySetting()
    }

    private fun replaceFragment(pageType: Int) {
        when (pageType) {
            0 -> {
                termsButton.isSelected = true
                privacyButton.isSelected = false
                supportButton.isSelected = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rightTabContainer.elevation = 2f
                    supportButton.elevation = 1f
                }
                rightTabContainer.bringToFront()
                termsButton.bringToFront()
            }
            1 -> {
                termsButton.isSelected = false
                privacyButton.isSelected = true
                supportButton.isSelected = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rightTabContainer.elevation = 2f
                    supportButton.elevation = 1f
                }
                rightTabContainer.bringToFront()
                privacyButton.bringToFront()
            }
            else -> {
                termsButton.isSelected = false
                privacyButton.isSelected = false
                supportButton.isSelected = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rightTabContainer.elevation = 1f
                    supportButton.elevation = 2f
                }
                supportButton.bringToFront()
            }
        }

        when (pageType) {
            0 -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, termsFragment).commit()
            }
            1 -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, privacyFragment).commit()
            }
            else -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, supportFragment).commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isKill = false
    }
}