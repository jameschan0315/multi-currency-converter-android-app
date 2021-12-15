package com.currencywiki.currencyconverter.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.MyKeyboard
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.getGradientDrawable
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.CurrencyItem
import com.currencywiki.currencyconverter.utils.*
import com.currencywiki.currencyconverter.utils.glide.GlideApp
import com.currencywiki.currencyconverter.utils.glide.SvgSoftwareLayerSetter
import de.hdodenhof.circleimageview.CircleImageView
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


class CurrencyValueRecyclerViewAdapter :
    RecyclerView.Adapter<CurrencyValueRecyclerViewAdapter.ViewHolder?>(), ItemMoveCallback.ItemTouchHelperContract {

    var currencyItemArray = ArrayList<CurrencyItem>()
    lateinit var keyboard: MyKeyboard
    var focusEditText: AppCompatEditText? = null
    lateinit var adapterListener: CurrencyValueRecyclerViewAdapterListener
    lateinit var context: Context
    private val requestBuilder = GlideApp.with(CurrencyConverterApp.instance!!.applicationContext)
        .`as`(PictureDrawable::class.java)
        .placeholder(R.drawable.bg_white_round)
        .error(R.drawable.bg_white_round)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(SvgSoftwareLayerSetter())
    var focusedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_currency_list, parent, false)
        return ViewHolder(view, adapterListener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currencyItem = currencyItemArray[position]
        setSVGImageToImageView(requestBuilder, holder.flagImageView, currencyItem.currency.flag)
        holder.flagImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (position != focusedPosition) {
            holder.valueEditText.setText(decimalString(currencyItem.value))
        }
        else {
            holder.valueEditText.setText(decimalStringEliminateZeros(currencyItem.value))
        }
        val appData = CurrencyConverterApp.instance!!.appData
        val settingManager = appData.settingManager

        var codeTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    context.resources.getDimension(R.dimen.sp_16)
                }
                1 -> {
                    context.resources.getDimension(R.dimen.sp_18)
                }
                else -> {
                    context.resources.getDimension(R.dimen.sp_20)
                }
            }

        holder.currencyNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, codeTextSize)

        var valueTextSize =
            when (settingManager.visualSize) {
                0 -> {
                    context.resources.getDimension(R.dimen.sp_22)
                }
                1 -> {
                    context.resources.getDimension(R.dimen.sp_24)
                }
                else -> {
                    context.resources.getDimension(R.dimen.sp_26)
                }
            }

        holder.valueEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueTextSize)
        holder.deleteButton.setColorFilter(getARGBColor(settingManager.gradient))
       //val drawable = holder.deleteButton.drawable
       //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
       //    DrawableCompat.setTint(drawable, getARGBColor(settingManager.gradient))
       //} else {
       //    drawable.setColorFilter(getARGBColor(settingManager.gradient), PorterDuff.Mode.SRC_IN)
       //}
       // setDrawableTint(drawable, getARGBColor(settingManager.gradient))
        holder.valueEditText.setTextColor(getARGBColor(settingManager.gradient))
        holder.valueEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        holder.valueEditText.setTextIsSelectable(true)
        holder.valueEditText.tag = position
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selectHandle = holder.valueEditText.textSelectHandle
            if (selectHandle != null) {
                setDrawableTint(selectHandle, getARGBColor(settingManager.gradient))
            }
            val leftSelectHandle = holder.valueEditText.textSelectHandleLeft
            if (leftSelectHandle != null) {
                setDrawableTint(leftSelectHandle, getARGBColor(settingManager.gradient))
            }
            val rightSelectHandle = holder.valueEditText.textSelectHandleRight
            if (rightSelectHandle != null) {
                setDrawableTint(rightSelectHandle, getARGBColor(settingManager.gradient))
            }
            val cursorDrawable = holder.valueEditText.textCursorDrawable
            if (cursorDrawable != null) {
                setDrawableTint(cursorDrawable, getARGBColor(settingManager.gradient))
            }
        }
        else {
            try {
                EditTextTint.applyColor(holder.valueEditText, getARGBColor(settingManager.gradient))
            } catch (e: EditTextTint.EditTextTintError) {
                e.printStackTrace()
            }
        }
        setCursorPointerColor(holder.valueEditText, getARGBColor(settingManager.gradient))
        setCursorDrawableColorNew(holder.valueEditText, getARGBColor(settingManager.gradient))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.valueEditText.showSoftInputOnFocus = false
        } else {
            try {
                val method = AppCompatEditText::class.java.getMethod(
                    "setShowSoftInputOnFocus"
                    , *arrayOf<Class<*>?>(Boolean::class.javaPrimitiveType)
                )
                method.isAccessible = true
                method.invoke(holder.valueEditText, false)
            } catch (e: Exception) {
            }
        }
        holder.currencyNameTextView.background = getGradientDrawable(settingManager.gradient, 0, 0, 8.0F)
        if (settingManager.showCurrencyName) {
            holder.currencyNameTextView.text = currencyItem.currency.symbol
        }
        else {
            holder.currencyNameTextView.text = appData.currencySymbolMap[currencyItem.currency.symbol]
        }
        if (settingManager.showCurrencyFlags) {
            holder.flagImageView.visibility = View.VISIBLE
        }
        else {
            holder.flagImageView.visibility = View.GONE
        }
        if (settingManager.showCurrencyName || settingManager.showCurrencySymbol) {
            holder.currencyNameTextView.visibility = View.VISIBLE
        }
        else {
            holder.currencyNameTextView.visibility = View.GONE
        }

        if (!holder.textChangedListenerAdded) {
            holder.valueEditText.setTextIsSelectable(true)
        }
        holder.valueEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val inputConnection = holder.valueEditText.onCreateInputConnection(EditorInfo())
                keyboard.setInputConnection(inputConnection)
                keyboard.visibility = View.VISIBLE
                focusEditText = holder.valueEditText
                holder.addTextChangedListener()
                holder.valueEditText.setText(floatFromFormat(holder.valueEditText.text.toString()))
            }
            else {
                holder.removeTextChangedListener()
            }
        }
        if (isDarkMode()) {
            holder.itemContainer.setBackgroundResource(R.drawable.bg_dark_gray_round)
            holder.currencyNameTextView.setTextColor(Color.DKGRAY)
            holder.valueEditText.setBackgroundColor(Color.DKGRAY)
        }
        else {
            holder.itemContainer.setBackgroundResource(R.drawable.bg_white_round)
            holder.currencyNameTextView.setTextColor(Color.WHITE)
            holder.valueEditText.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int {
        return currencyItemArray.size
    }

    inner class ViewHolder(view: View, adapterListener: CurrencyValueRecyclerViewAdapterListener) :
        RecyclerView.ViewHolder(view) {

        var textChangedListenerAdded = false
        val flagImageView: CircleImageView = view.findViewById(R.id.img_flag)
        val valueEditText: AppCompatEditText = view.findViewById(R.id.txt_value)
        val deleteButton: AppCompatImageView = view.findViewById(R.id.btn_delete)
        val currencyNameTextView: AppCompatTextView = view.findViewById(R.id.txt_currency_name)
        val itemContainer: LinearLayout = view.findViewById(R.id.item_container)
        private val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                var value = BigDecimal(0.0)
                try {
                    value = BigDecimal(valueEditText.text.toString())
                }
                catch (exception: NumberFormatException) {
                }
                adapterListener.didValueChanged(adapterPosition, value)
            }
        }
        init {
            deleteButton.setOnClickListener {
                adapterListener.didDeleteCurrency(adapterPosition)
            }
        }

        fun addTextChangedListener() {
            valueEditText.addTextChangedListener(textWatcher)
            textChangedListenerAdded = true
        }

        fun removeTextChangedListener() {
            valueEditText.removeTextChangedListener(textWatcher)
            textChangedListenerAdded = false
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (index in fromPosition until toPosition) {
                Collections.swap(currencyItemArray, index, index + 1)
            }
        } else {
            for (index in fromPosition downTo toPosition + 1) {
                Collections.swap(currencyItemArray, index, index - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        saveSettingsToPreference()
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
    }
}