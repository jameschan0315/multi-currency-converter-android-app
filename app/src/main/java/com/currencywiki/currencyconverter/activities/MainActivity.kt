package com.currencywiki.currencyconverter.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Base64.encode
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.adapter.PageAdapter
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.common.isDarkModeBasedOnColor
import com.currencywiki.currencyconverter.fragments.*
import com.currencywiki.currencyconverter.utils.*
import com.currencywiki.currencyconverter.widgets.ConvertListWidget
import com.currencywiki.currencyconverter.widgets.SingleConvertWidget
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import org.json.JSONObject
import java.math.BigDecimal
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity(), ColorPickerDialogListener, RateFetchTaskInterface {

    private var manager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    private var pageAdapter: PageAdapter? = null
    var previousTabPosition = 0
    private lateinit var appBar: AppBarLayout
    var fromWidget = false

    private var settingManager: SettingManager? = null

    companion object {
        var fromAddActivity = false
        var isSettingWidgets = false
        const val MY_IGNORE_OPTIMIZATION_REQUEST = 1234
    }

    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.getPackageManager()
                .getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(android.util.Base64.encode(md.digest(), 0))
                Log.i(
                    "TAG",
                    "printHashKey() Hash Key: $hashKey"
                )
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printHashKey(this)
        //
        // Load currency name map
        //

        CurrencyConverterApp.instance?.appData?.loadCurrencyNameMap()

        //
        // Load sample rate at activity start
        //

        val appData = CurrencyConverterApp.instance!!.appData
        val settingManager = appData.settingManager
        if (settingManager.sampleRateJson != "") {
            appData.currencyRateJson = JSONObject(settingManager.sampleRateJson)
            val rateFetchTask = RateFetchTask(this)
            rateFetchTask.rateFetchTaskInterface = this
            rateFetchTask.execute()
        } else {
            val rateFetchTask = RateFetchTask(this)
            val result = rateFetchTask.execute().get()
            if (result != null) {
                settingManager.sampleRateJson = result.toString()
                CurrencyConverterApp.instance?.appData?.currencyRateJson = result
                saveSettingsToPreference()
            }
        }
        appBar = findViewById(R.id.appbar)
        pageAdapter = PageAdapter(supportFragmentManager)

        viewPager = findViewById(R.id.view_pager)
        viewPager?.adapter = pageAdapter

        tabLayout = findViewById(R.id.tab_layout)
        //
        // Add Tabs
        //

        addTab(R.drawable.single_exchange)
        addTab(R.drawable.multi)
        addTab(R.drawable.decimal)
        addTab(R.drawable.star)
        addTab(R.drawable.info)
        addTab(R.drawable.setting)
        //  bindControls()

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 3) {
                    openRateSheet()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when {
                    tab?.position!! < 3 -> {
                        viewPager?.setCurrentItem(tab.position, true)
                        previousTabPosition = tab.position
                    }
                    tab.position == 3 -> {
                        openRateSheet()
                    }
                    tab.position > 3 -> {
                        viewPager?.setCurrentItem(tab.position - 1, true)
                        previousTabPosition = tab.position
                    }
                }
            }
        })

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                previousTabPosition = if (position < 3) {
                    tabLayout?.getTabAt(position)!!.select()
                    position
                } else {
                    tabLayout?.getTabAt(position + 1)!!.select()
                    position + 1
                }
            }
        })

        if (settingManager.isOptimize) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val mIntent = Intent()
                //  val packageName = packageName
                val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    isKill = false
                    mIntent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    mIntent.data = Uri.parse("package:$packageName")
                    startActivityForResult(mIntent, MY_IGNORE_OPTIMIZATION_REQUEST)
                }
            }
        }
        if (intent.getBooleanExtra("permission", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val mIntent = Intent()
                //  val packageName = packageName
                val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    isKill = false
                    mIntent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    mIntent.data = Uri.parse("package:$packageName")
                    startActivityForResult(mIntent, MY_IGNORE_OPTIMIZATION_REQUEST)
                }
            }
        }
    }


    private fun bindControls() {
        val extras = intent.extras
        settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        var symbol1 = settingManager!!.singleSymbol1
        var symbol2 = settingManager!!.singleSymbol2
        var amount = settingManager!!.singleValue
        if (extras?.getString("symbol1", null) != null) {
            fromWidget = true
            symbol1 = extras.getString("symbol1", settingManager!!.singleSymbol1)
            symbol2 = extras.getString("symbol2", settingManager!!.singleSymbol2)
            amount = BigDecimal(extras.getDouble("amount", 1.0))
        }
        val singleConvertFragment = SingleConvertFragment()
        settingManager!!.singleSymbol1 = symbol1
        settingManager!!.singleSymbol2 = symbol2
        settingManager!!.singleValue = amount
        saveSettingsToPreference()
        pageAdapter?.addPage(singleConvertFragment)
        val convertListFragment = ConvertListFragment()
        pageAdapter?.addPage(convertListFragment)
        val decimalSettingFragment = DecimalSettingFragment()
        pageAdapter?.addPage(decimalSettingFragment)
        val infoFragment = InfoFragment()
        pageAdapter?.addPage(infoFragment)
        val settingFragment = SettingFragment()
        pageAdapter?.addPage(settingFragment)
        pageAdapter?.notifyDataSetChanged()

        //
        // Define Tab and Pager Events
        //

        if (settingManager?.isColorChange!!) {
            val msDiff: Long =
                Calendar.getInstance().timeInMillis - settingManager?.appDayTime!!
            val daysDiff: Long = TimeUnit.MILLISECONDS.toDays(msDiff)
            if (daysDiff >= 7) {
                Handler(Looper.getMainLooper()).postDelayed({
                    submitReview()
                }, 1000)
            }

            settingManager?.appOpenTime = settingManager?.appOpenTime!! + 1

            if (settingManager?.appOpenTime == 5) {
                Handler(Looper.getMainLooper()).postDelayed({
                    submitReview()
                }, 1000)

            }
        }
        CurrencyConverterApp.instance!!.mainActivity = this
        applySetting()

        if (settingManager!!.displayMultiConverter && !fromWidget) {
            tabLayout?.getTabAt(1)?.select()
        } else {
            tabLayout?.getTabAt(0)?.select()
        }
    }

    private fun submitReview() {
        manager = ReviewManagerFactory.create(this)
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                val flow = manager?.launchReviewFlow(this, reviewInfo)
                flow?.addOnCompleteListener { _ ->
                    isKill = false
                    settingManager?.isColorChange = false
                    settingManager?.appDayTime = Calendar.getInstance().timeInMillis
                    settingManager?.appOpenTime = 0
                    // Toast.makeText(this@MainActivity, "Success>>$isKill", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Err", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!fromAddActivity) {
            if (pageAdapter != null) {
                bindControls()
            }
            if (settingManager != null) {
                if (settingManager!!.displayMultiConverter && !fromWidget) {
                    tabLayout?.getTabAt(1)?.select()
                }
            }
        } else {
            fromAddActivity = false
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        Log.e("MainActivity", "onActivityResult>>>")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST) {
                val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    // Ignoring battery optimization
                    Log.e("MainActivity", "if>>>")
                    settingManager.isOptimize = true
                } else {
                    // Not ignoring battery optimization
                    settingManager.isOptimize = false
                    Log.e("MainActivity", "else>>>")
                }
            }
        }

    }

    fun selectTab(tabIndex: Int) {
        tabLayout?.getTabAt(tabIndex)?.select()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val basePageFragment = pageAdapter?.getItem(viewPager!!.currentItem) as BasePageFragment
        if (basePageFragment.dispatchTouchEvent(ev)) {
            super.dispatchTouchEvent(ev)
        }
        return false
    }

    fun showSupport() {
        tabLayout?.getTabAt(4)?.select()
        val infoFragment = pageAdapter?.getItem(3) as InfoFragment
        infoFragment.showSupport()
    }

    fun applySetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            if (isDarkMode()) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decor.systemUiVisibility = 0
            }
        }
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        appBar.setBackgroundColor(getARGBColor(settingManager.gradient))

        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_pressed)
        )

        var colors = intArrayOf(
            ColorUtils.setAlphaComponent(Color.WHITE, 100),
            ColorUtils.setAlphaComponent(Color.WHITE, 100)
        )
        var tabIndicatorColor = Color.WHITE
        if (isDarkMode()) {
            colors = intArrayOf(
                ColorUtils.setAlphaComponent(Color.DKGRAY, 100),
                ColorUtils.setAlphaComponent(Color.DKGRAY, 100)
            )
            tabIndicatorColor = Color.BLACK
        }

        tabLayout?.tabRippleColor = ColorStateList(states, colors)
        tabLayout?.setSelectedTabIndicatorColor(tabIndicatorColor)
        for (index in 0 until 6) {
            val tabItemDrawable =
                tabLayout?.getTabAt(index)!!.customView!!.findViewById<ImageView>(R.id.icon).drawable
            if (isDarkMode()) {
                setDrawableTint(tabItemDrawable, Color.DKGRAY)
            } else {
                setDrawableTint(tabItemDrawable, Color.WHITE)
            }
        }
        for (index in 0 until 5) {
            val basePageFragment = pageAdapter?.getItem(index) as BasePageFragment
            basePageFragment.applySetting()
        }

        triggerWidgetUpdate()
    }

    private fun triggerWidgetUpdate() {
        val ids =
            AppWidgetManager.getInstance(application).getAppWidgetIds(
                ComponentName(
                    application,
                    SingleConvertWidget::class.java
                )
            )
        val myWidget = SingleConvertWidget()
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this), ids)

        val listWidgetIds =
            AppWidgetManager.getInstance(application).getAppWidgetIds(
                ComponentName(
                    application,
                    ConvertListWidget::class.java
                )
            )
        val myListWidget = ConvertListWidget()
        myListWidget.notifyUpdate(AppWidgetManager.getInstance(this), listWidgetIds)
        myListWidget.onUpdate(this, AppWidgetManager.getInstance(this), listWidgetIds)
    }

    private fun openRateSheet() {
        val view: View = layoutInflater.inflate(R.layout.sheet_rate, null)
        val dialog = BottomSheetDialog(
            this,
            R.style.BottomSheetDialog
        )
        val txtRateTheApp = view.findViewById<AppCompatTextView>(R.id.txt_rate_the_app)
        val txtRateDesc = view.findViewById<AppCompatTextView>(R.id.txt_rate_app_desc)
        val txtBtnNotNow = view.findViewById<AppCompatTextView>(R.id.btn_not_now)
        val rateBar = view.findViewById<MaterialRatingBar>(R.id.rate_star)
        val nameMap = CurrencyConverterApp.instance?.appData?.currencyNameMap
//        txtRateTheApp.text = nameMap!!["Rate the app"]
//        txtRateDesc.text = nameMap["Tap a start to rate it on the Google Play Store"]
//        txtBtnNotNow.text = nameMap["Not now"]
        val stars = rateBar.progressDrawable
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        if (isDarkMode()) {
            setDrawableTint(stars, Color.DKGRAY)
        } else {
            setDrawableTint(stars, getARGBColor(settingManager.gradient))
        }

        dialog.setContentView(view)
        dialog.setCancelable(false)
        view.findViewById<AppCompatTextView>(R.id.btn_not_now)
            .setOnClickListener { dialog.dismiss() }
        view.findViewById<AppCompatButton>(R.id.btn_rate_star)
            .setOnClickListener { gotoPlayStore() }
        dialog.show()
        tabLayout?.getTabAt(previousTabPosition)!!.select()
    }

    private fun addTab(iconResource: Int) {
        val tabView = this.layoutInflater.inflate(R.layout.custom_tab, null)
        tabView.findViewById<ImageView>(R.id.icon).setImageResource(iconResource)
        tabLayout?.addTab(tabLayout!!.newTab().setCustomView(tabView))
    }

    private fun gotoPlayStore() {
        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.currencywiki.currencyconverter")
            )
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        val baseFragment = pageAdapter?.getItem(viewPager!!.currentItem)
        if (baseFragment!!.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Log.e("MainActivity", "submitReview>>> $isKill")
        if (isKill)
            finish()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        settingManager.gradient = color
        settingManager.gradientChanged = true
        if (isDarkModeBasedOnColor()) {
            settingManager.themeType = 1
        } else {
            settingManager.themeType = 0
        }
        applySetting()
        saveSettingsToPreference()
        if (!settingManager.isColorChange) {
            settingManager.isColorChange = true
            settingManager.appDayTime = Calendar.getInstance().timeInMillis
            settingManager.appOpenTime = 0
        }
    }

    override fun pickerViewCreated(dialog: ColorPickerDialog?, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selectHandle = dialog?.hexEditText?.textSelectHandle
            if (selectHandle != null) {
                setDrawableTint(selectHandle, color)
            }
            val leftSelectHandle = dialog?.hexEditText?.textSelectHandleLeft
            if (leftSelectHandle != null) {
                setDrawableTint(leftSelectHandle, color)
            }
            val rightSelectHandle = dialog?.hexEditText?.textSelectHandleRight
            if (rightSelectHandle != null) {
                setDrawableTint(rightSelectHandle, color)
            }
            val cursorDrawable = dialog?.hexEditText?.textCursorDrawable
            if (cursorDrawable != null) {
                setDrawableTint(cursorDrawable, color)
            }
        } else {
            try {
                EditTextTint.applyColor(dialog!!.hexEditText, color)
            } catch (e: EditTextTint.EditTextTintError) {
                e.printStackTrace()
            }
        }
        val colorStateList = ColorStateList.valueOf(color)
        ViewCompat.setBackgroundTintList(dialog!!.hexEditText, colorStateList)
        setCursorPointerColor(dialog.hexEditText, color)
        setCursorDrawableColorNew(dialog.hexEditText, color)
    }

    override fun onDialogDismissed(dialogId: Int) {
    }

    fun updateRate() {
        for (index in 0 until 5) {
            val basePageFragment = pageAdapter?.getItem(index)
            if (basePageFragment is SingleConvertFragment) {
                basePageFragment.calculateRate()
            }
            if (basePageFragment is ConvertListFragment) {
                basePageFragment.reloadEntireRate()
            }
        }
    }

    override fun rateUpdated(success: Boolean) {
        if (!success) {
            return
        }

        val appData = CurrencyConverterApp.instance!!.appData
        val settingManager = appData.settingManager
        settingManager.sampleRateJson = appData.currencyRateJson.toString()
        saveSettingsToPreference()
        CurrencyConverterApp.instance!!.triggerWidgetUpdate()
        this.updateRate()
    }

    override fun rateError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

}