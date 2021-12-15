package com.currencywiki.currencyconverter.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.activities.MainActivity
import com.currencywiki.currencyconverter.common.*
import com.currencywiki.currencyconverter.utils.*
import com.currencywiki.currencyconverter.utils.glide.GlideApp
import com.currencywiki.currencyconverter.utils.glide.SvgSoftwareLayerSetter
import com.daasuu.bl.ArrowDirection
import com.daasuu.bl.BubbleLayout
import com.daasuu.bl.BubblePopupHelper
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.jaredrummler.android.colorpicker.ColorShape
import de.hdodenhof.circleimageview.CircleImageView


class SettingFragment : BasePageFragment(SettingFragment::class.simpleName) {
    private lateinit var txtColorSelection: AppCompatTextView
    private lateinit var txtSelectGradient: AppCompatTextView
    private lateinit var txtVisualSize: AppCompatTextView
    private lateinit var txtTheme: AppCompatTextView
    private lateinit var txtWhenOpeningApp: AppCompatTextView
    private lateinit var txtDisplay: AppCompatTextView
    private lateinit var txtDisplayName: AppCompatTextView
    private lateinit var txtDisplaySymbol: AppCompatTextView
    private lateinit var txtDisplayFlags: AppCompatTextView
    private lateinit var txtDateFormat: AppCompatTextView
    private lateinit var txtShowMultiConverter: AppCompatTextView
    private lateinit var btnSelectGradient: LinearLayout
    private lateinit var layoutSelectedGradient: LinearLayout
    private lateinit var layoutPaletteSelectedGradient: LinearLayout
    private lateinit var layoutGradientText: LinearLayout
    private lateinit var layoutGradientBorder: LinearLayout
    private lateinit var displayMultiConverterSwitch: SwitchMaterial
    private lateinit var widgetOpacitySeekBar: AppCompatSeekBar
    private lateinit var widgetOpacityTextView: AppCompatTextView
    private lateinit var widgetOpacityValueTextView: AppCompatTextView
    private lateinit var widgetFlagImageView1: CircleImageView
    private lateinit var widgetFlagImageView2: CircleImageView
    private lateinit var widgetPreviewContainer: LinearLayout
    private lateinit var widgetSymbol1TextView: AppCompatTextView
    private lateinit var widgetSymbol2TextView: AppCompatTextView
    private lateinit var widgetSlashTextView: AppCompatTextView
    private lateinit var widgetRateTextView: AppCompatTextView
    private lateinit var widgetPercentTextView: AppCompatTextView
    private lateinit var widgetProviderTextView: AppCompatTextView
    private lateinit var bubbleLayout: BubbleLayout
    private lateinit var bubbleGuideTextView: AppCompatTextView
    private lateinit var bubbleClickHereTextView: AppCompatTextView
    private lateinit var viewLine: View

    private var visualCheckBoxes = ArrayList<MaterialCheckBox>()
    private var themeCheckBoxes = ArrayList<MaterialCheckBox>()
    private var displaySwitches = ArrayList<SwitchMaterial>()
    private var dateFormatCheckBoxes = ArrayList<MaterialCheckBox>()
    private val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
    override val fragmentTag = SettingFragment::class.java.canonicalName
    private lateinit var infoButton: AppCompatImageButton

    private val DIALOG_ID = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        bindControls(view)
        applySetting()
        return view
    }

    override fun bindControls(view: View) {
        super.bindControls(view)
        val requestBuilder = GlideApp.with(this)
            .`as`(PictureDrawable::class.java)
            .placeholder(R.drawable.bg_white_round)
            .error(R.drawable.bg_white_round)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(SvgSoftwareLayerSetter())
        viewLine = view.findViewById(R.id.viewLine)
        widgetOpacitySeekBar = view.findViewById(R.id.seek_bar_widget_opacity)
        widgetOpacityTextView = view.findViewById(R.id.txt_widget_transparency)
        widgetOpacityValueTextView = view.findViewById(R.id.txt_widget_opacity)
        widgetOpacitySeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setTransparencySetting(seekBar.progress)
                showSetting()
                (activity as MainActivity).applySetting()
                applySetting()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                setTransparencySetting(progress)
                showSetting()
            }
        })
        widgetFlagImageView1 = view.findViewById(R.id.img_flag1)
        widgetFlagImageView2 = view.findViewById(R.id.img_flag2)
        setSVGImageToImageView(requestBuilder, widgetFlagImageView1, "us")
        setSVGImageToImageView(requestBuilder, widgetFlagImageView2, "eu")
        widgetPreviewContainer = view.findViewById(R.id.widget_preview_container)
        widgetSymbol1TextView = view.findViewById(R.id.txt_symbol1)
        widgetSymbol2TextView = view.findViewById(R.id.txt_symbol2)
        widgetSlashTextView = view.findViewById(R.id.txt_slash)
        widgetRateTextView = view.findViewById(R.id.txt_rate)
        widgetPercentTextView = view.findViewById(R.id.txt_percent)
        widgetProviderTextView = view.findViewById(R.id.txt_provider)

        visualCheckBoxes.clear()
        for (index in 1..3) {
            val resourceId = getResourceIdByString("chk_visual_$index")
            val checkBox = view.findViewById<MaterialCheckBox>(resourceId)
            visualCheckBoxes.add(checkBox)
        }

        themeCheckBoxes.clear()
        for (index in 1..2) {
            val resourceId = getResourceIdByString("chk_theme_$index")
            val checkBox = view.findViewById<MaterialCheckBox>(resourceId)
            themeCheckBoxes.add(checkBox)
        }

        displaySwitches.clear()
        for (index in 1..3) {
            val resourceId = getResourceIdByString("switch_display_$index")
            val switchMaterial = view.findViewById<SwitchMaterial>(resourceId)
            displaySwitches.add(switchMaterial)
        }

        dateFormatCheckBoxes.clear()
        for (index in 1..2) {
            val resourceId = getResourceIdByString("chk_date_$index")
            val checkBox = view.findViewById<MaterialCheckBox>(resourceId)
            dateFormatCheckBoxes.add(checkBox)
        }

        for (index in 0 until visualCheckBoxes.size) {
            val checkBox = visualCheckBoxes[index]
            checkBox.setOnClickListener {
                settingManager.visualSize = index
                showSetting()
                (activity as MainActivity).applySetting()
                applySetting()
            }
        }

        for (index in 0 until themeCheckBoxes.size) {
            val checkBox = themeCheckBoxes[index]
            checkBox.setOnClickListener {
                settingManager.themeType = index
                showSetting()
                (activity as MainActivity).applySetting()
                applySetting()
            }
        }

        displayMultiConverterSwitch = view.findViewById(R.id.switch_display_multi_converter)

        for (index in 0 until displaySwitches.size) {
            val materialSwitch = displaySwitches[index]
            materialSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (index) {
                    0 -> {
                        if (!isChecked && !settingManager.showCurrencySymbol && !settingManager.showCurrencyFlags) {
                            materialSwitch.isChecked = true
                            return@setOnCheckedChangeListener
                        }
                        settingManager.showCurrencyName = isChecked
                        if (isChecked) {
                            settingManager.showCurrencySymbol = !isChecked
                        }
                    }
                    1 -> {
                        if (!isChecked && !settingManager.showCurrencyName && !settingManager.showCurrencyFlags) {
                            materialSwitch.isChecked = true
                            return@setOnCheckedChangeListener
                        }
                        settingManager.showCurrencySymbol = isChecked
                        if (isChecked) {
                            settingManager.showCurrencyName = !isChecked
                        }
                    }
                    2 -> {
                        if (!isChecked && !settingManager.showCurrencySymbol && !settingManager.showCurrencyName) {
                            materialSwitch.isChecked = true
                            return@setOnCheckedChangeListener
                        }
                        settingManager.showCurrencyFlags = isChecked
                    }
                }
                showSetting()
            }
        }

        displayMultiConverterSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingManager.displayMultiConverter = isChecked
            saveSettingsToPreference()
        }

        for (index in 0 until dateFormatCheckBoxes.size) {
            val checkBox = dateFormatCheckBoxes[index]
            checkBox.setOnClickListener {
                settingManager.dateFormat = index
                showSetting()
            }
        }

        txtColorSelection = view.findViewById(R.id.txt_color_selection)
        txtSelectGradient = view.findViewById(R.id.txt_select_gradient)
        txtVisualSize = view.findViewById(R.id.txt_visual_size)
        txtTheme = view.findViewById(R.id.txt_theme)
        txtWhenOpeningApp = view.findViewById(R.id.txt_when_open_app)
        txtDisplay = view.findViewById(R.id.txt_display)
        txtDisplayName = view.findViewById(R.id.txt_display_name)
        txtDisplaySymbol = view.findViewById(R.id.txt_display_symbol)
        txtDisplayFlags = view.findViewById(R.id.txt_display_flags)
        txtDateFormat = view.findViewById(R.id.txt_date_format)
        val nameMap = CurrencyConverterApp.instance?.appData?.currencyNameMap
//        txtColorSelection.text = nameMap!!["Color Selection"]
        txtSelectGradient.text = nameMap!!["Select the gradient"]
        txtVisualSize.text = nameMap["Visual Size"]
        visualCheckBoxes[0].text = nameMap["Small"]
        visualCheckBoxes[1].text = nameMap["Medium"]
        visualCheckBoxes[2].text = nameMap["Large"]
        txtDisplay.text = nameMap["Display"]
        txtDisplayName.text = nameMap["Display Currency Code"]
        txtDisplaySymbol.text = nameMap["Display Currency Symbol"]
        txtDisplayFlags.text = nameMap["Display Currency Flags"]
        txtDateFormat.text = nameMap["Date Format"]
        btnSelectGradient = view.findViewById(R.id.btn_select_gradient)
        layoutSelectedGradient = view.findViewById(R.id.layout_selected_gradient)
        layoutPaletteSelectedGradient = view.findViewById(R.id.layout_palette_selected_gradient)
        layoutGradientText = view.findViewById(R.id.layout_gradient_text)
        layoutGradientBorder = view.findViewById(R.id.layout_border)
        txtShowMultiConverter = view.findViewById(R.id.txt_display_multi_converter)

        //
        // Set decimal check
        //

        showLanguageText()

        //
        // Select gradient
        //

        btnSelectGradient.setOnClickListener {
            val colorPickerDialog =
                com.jaredrummler.android.colorpicker.ColorPickerDialog.newBuilder()
                    .setDialogType(com.jaredrummler.android.colorpicker.ColorPickerDialog.TYPE_PRESETS)
                    .setAllowPresets(false)
                    .setColorShape(ColorShape.SQUARE)
                    .setDialogId(DIALOG_ID)
                    .setColor(getARGBColor(settingManager.gradient))
                    .setShowAlphaSlider(false)
                    .show(activity)
        }
        infoButton = view.findViewById(R.id.btn_info)
        bubbleLayout =
            LayoutInflater.from(requireContext()).inflate(R.layout.layout_info_bubble, null) as BubbleLayout
        bubbleGuideTextView = bubbleLayout.findViewById(R.id.txt_guide)
        bubbleClickHereTextView = bubbleLayout.findViewById(R.id.txt_click_here)

        applySetting()
        val popupWindow: PopupWindow = BubblePopupHelper.create(requireContext(), bubbleLayout)
        bubbleClickHereTextView.setOnClickListener {
            popupWindow.dismiss()
            MainActivity.isSettingWidgets = true
            (activity as MainActivity).showSupport()
        }
        infoButton.setOnClickListener { it
            val location = IntArray(2)
            it.getLocationInWindow(location)
            val windowHeight = Resources.getSystem().displayMetrics.heightPixels
            bubbleLayout.arrowDirection = ArrowDirection.BOTTOM
            bubbleLayout.strokeWidth = 1f
            bubbleLayout.cornersRadius = 4f
            popupWindow.showAtLocation(it,
                Gravity.LEFT or Gravity.BOTTOM, location[0],
                (windowHeight - location[1] + it.height - resources.getDimension(R.dimen.dp_16)).toInt()
            )
        }
    }

    @SuppressLint("RestrictedApi", "ResourceType")
    override fun applySetting() {
        super.applySetting()
        if (!this::txtColorSelection.isInitialized) {
            return
        }
        showSetting()
        var titleTextSize =
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

        var contentTextSize =
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

        var checkBoxSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_24)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_28)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_32)
                }
            }

        txtColorSelection.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        widgetOpacityTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtVisualSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtTheme.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtWhenOpeningApp.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtDateFormat.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        txtSelectGradient.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        for (visualCheckBox in visualCheckBoxes) {
            visualCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        }
        for (themeCheckBox in themeCheckBoxes) {
            themeCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        }
        txtShowMultiConverter.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        widgetOpacityValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        txtDisplayName.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        txtDisplaySymbol.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        txtDisplayFlags.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        for (dateCheckBox in dateFormatCheckBoxes) {
            dateCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
        }

        //
        // Widget Transparency
        //

        var widgetPreviewSymbolSize =
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

        widgetOpacitySeekBar.progress = getTransparencySetting()
        widgetSymbol1TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewSymbolSize)
        widgetSymbol2TextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewSymbolSize)
        widgetSlashTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewSymbolSize)

        var widgetPreviewValueSize =
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
        widgetRateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewValueSize)

        var widgetPreviewPercentSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_10)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_12)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_14)
                }
            }
        widgetPercentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewPercentSize)

        var widgetPreviewProviderSize =
            when (settingManager.visualSize) {
                0 -> {
                    resources.getDimension(R.dimen.sp_12)
                }
                1 -> {
                    resources.getDimension(R.dimen.sp_14)
                }
                else -> {
                    resources.getDimension(R.dimen.sp_16)
                }
            }
        widgetProviderTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, widgetPreviewProviderSize)


        if (isDarkMode()) {
            val infoDrawable = infoButton.drawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                widgetOpacitySeekBar.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.dark_gray)
                widgetOpacitySeekBar.progressTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.dark_gray)
            }else{
                val drawable = widgetOpacitySeekBar.thumb
                setDrawableTint(drawable, Color.DKGRAY)
                val progressDrawable = widgetOpacitySeekBar.progressDrawable
                setDrawableTint(progressDrawable, Color.DKGRAY)
            }

            setDrawableTint(infoDrawable, Color.DKGRAY)
            viewLine.setBackgroundColor(Color.DKGRAY)
            widgetSymbol1TextView.setTextColor(Color.DKGRAY)
            widgetSymbol2TextView.setTextColor(Color.DKGRAY)
            widgetSlashTextView.setTextColor(Color.DKGRAY)
            widgetRateTextView.setTextColor(Color.DKGRAY)
            widgetPercentTextView.setTextColor(Color.DKGRAY)
            widgetProviderTextView.setTextColor(Color.DKGRAY)
            txtColorSelection.setTextColor(Color.DKGRAY)
            widgetOpacityTextView.setTextColor(Color.DKGRAY)
            txtVisualSize.setTextColor(Color.DKGRAY)
            txtTheme.setTextColor(Color.DKGRAY)
            for (visualCheckBox in visualCheckBoxes) {
                visualCheckBox.setTextColor(Color.DKGRAY)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.DKGRAY, Color.DKGRAY)
                CompoundButtonCompat.setButtonTintList(visualCheckBox, ColorStateList(states, colors))
            }
            for (themeCheckBox in themeCheckBoxes) {
                themeCheckBox.setTextColor(Color.DKGRAY)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.DKGRAY, Color.DKGRAY)
                CompoundButtonCompat.setButtonTintList(themeCheckBox, ColorStateList(states, colors))
            }
            txtWhenOpeningApp.setTextColor(Color.DKGRAY)
            txtShowMultiConverter.setTextColor(Color.DKGRAY)
            widgetOpacityValueTextView.setTextColor(Color.DKGRAY)
            txtDisplay.setTextColor(Color.DKGRAY)
            txtDisplayName.setTextColor(Color.DKGRAY)
            txtDisplayFlags.setTextColor(Color.DKGRAY)
            txtDisplaySymbol.setTextColor(Color.DKGRAY)
            txtDateFormat.setTextColor(Color.DKGRAY)
            for (dateCheckBox in dateFormatCheckBoxes) {
                dateCheckBox.setTextColor(Color.DKGRAY)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.DKGRAY, Color.DKGRAY)
                CompoundButtonCompat.setButtonTintList(dateCheckBox, ColorStateList(states, colors))
            }
            layoutGradientBorder.setBackgroundColor(Color.DKGRAY)
            infoButton.supportImageTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_gray)
        }
        else {
            val infoDrawable = infoButton.drawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                widgetOpacitySeekBar.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                widgetOpacitySeekBar.progressTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
            }else{
                val drawable = widgetOpacitySeekBar.thumb
                setDrawableTint(drawable, Color.WHITE)
                val progressDrawable = widgetOpacitySeekBar.progressDrawable
                setDrawableTint(progressDrawable, Color.WHITE)
            }


            setDrawableTint(infoDrawable, Color.WHITE)
            viewLine.setBackgroundColor(Color.WHITE)
            widgetSymbol1TextView.setTextColor(Color.WHITE)
            widgetSymbol2TextView.setTextColor(Color.WHITE)
            widgetSlashTextView.setTextColor(Color.WHITE)
            widgetRateTextView.setTextColor(Color.WHITE)
            widgetPercentTextView.setTextColor(Color.WHITE)
            widgetProviderTextView.setTextColor(Color.WHITE)
            txtColorSelection.setTextColor(Color.WHITE)
            widgetOpacityTextView.setTextColor(Color.WHITE)
            txtColorSelection.setTextColor(Color.WHITE)
            widgetOpacityTextView.setTextColor(Color.WHITE)
            txtVisualSize.setTextColor(Color.WHITE)
            txtTheme.setTextColor(Color.WHITE)
            for (visualCheckBox in visualCheckBoxes) {
                visualCheckBox.setTextColor(Color.WHITE)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.WHITE, Color.WHITE)
                CompoundButtonCompat.setButtonTintList(visualCheckBox, ColorStateList(states, colors))
            }
            for (themeCheckBox in themeCheckBoxes) {
                themeCheckBox.setTextColor(Color.WHITE)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.WHITE, Color.WHITE)
                CompoundButtonCompat.setButtonTintList(themeCheckBox, ColorStateList(states, colors))
            }
            txtWhenOpeningApp.setTextColor(Color.WHITE)
            txtShowMultiConverter.setTextColor(Color.WHITE)
            widgetOpacityValueTextView.setTextColor(Color.WHITE)
            txtDisplay.setTextColor(Color.WHITE)
            txtDisplayName.setTextColor(Color.WHITE)
            txtDisplayFlags.setTextColor(Color.WHITE)
            txtDisplaySymbol.setTextColor(Color.WHITE)
            txtDisplaySymbol.setTextColor(Color.WHITE)
            txtDateFormat.setTextColor(Color.WHITE)
            for (dateCheckBox in dateFormatCheckBoxes) {
                dateCheckBox.setTextColor(Color.WHITE)
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
                val colors = intArrayOf(Color.WHITE, Color.WHITE)
                CompoundButtonCompat.setButtonTintList(dateCheckBox, ColorStateList(states, colors))
            }
            layoutGradientBorder.setBackgroundColor(Color.WHITE)
            infoButton.supportImageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        }

        //
        // Bubble
        //

        var bubbleContextTextSize =
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

        bubbleGuideTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleContextTextSize)
        bubbleClickHereTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleContextTextSize)

        bubbleLayout.bubbleColor = getARGBColor(settingManager.gradient)
        if (isDarkMode()) {
            bubbleLayout.strokeColor = Color.DKGRAY
            bubbleGuideTextView.setTextColor(Color.DKGRAY)
            bubbleClickHereTextView.setTextColor(Color.DKGRAY)
        }
        else {
            bubbleLayout.strokeColor = Color.WHITE
            bubbleGuideTextView.setTextColor(Color.WHITE)
            bubbleClickHereTextView.setTextColor(Color.WHITE)
        }

        saveSettingsToPreference()
    }

    private fun showLanguageText() {

    }

    private fun showSetting() {
        val visualSize = settingManager.visualSize
        val themeType = settingManager.themeType
        val dateFormat = settingManager.dateFormat

        for (visualSizeCheckBox in visualCheckBoxes) {
            visualSizeCheckBox.isChecked = false
        }
        visualCheckBoxes[visualSize].isChecked = true

        for (themeCheckBox in themeCheckBoxes) {
            themeCheckBox.isChecked = false
        }
        themeCheckBoxes[themeType].isChecked = true

        displayMultiConverterSwitch.isChecked = settingManager.displayMultiConverter

        displaySwitches[0].isChecked = settingManager.showCurrencyName
        displaySwitches[1].isChecked = settingManager.showCurrencySymbol
        displaySwitches[2].isChecked = settingManager.showCurrencyFlags

        for (dateFormatCheckBox in dateFormatCheckBoxes) {
            dateFormatCheckBox.isChecked = false
        }
        dateFormatCheckBoxes[dateFormat].isChecked = true

        val gradientColor = settingManager.gradient
        if (!settingManager.gradientChanged) {
            layoutGradientText.visibility = View.VISIBLE
            layoutGradientBorder.visibility = View.INVISIBLE
            layoutSelectedGradient.visibility = View.INVISIBLE
            layoutPaletteSelectedGradient.visibility = View.INVISIBLE
        }
        else {
            layoutGradientText.visibility = View.INVISIBLE
            layoutGradientBorder.visibility = View.VISIBLE
            layoutPaletteSelectedGradient.visibility = View.VISIBLE
            layoutSelectedGradient.visibility = View.VISIBLE
            layoutSelectedGradient.background = getGradientDrawable(gradientColor, 1)
        }

        //
        // Widget Transparency
        //

        widgetOpacityValueTextView.text = getTransparencySetting().toString()
        widgetPreviewContainer.background = getWidgetGradientDrawable(settingManager.gradient, 0, 0, 16.0F)
    }

    override fun onResume() {
        super.onResume()
        isKill = false
    }
}