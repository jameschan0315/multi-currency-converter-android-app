package com.currencywiki.currencyconverter.adapter

import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.glide.GlideApp
import com.currencywiki.currencyconverter.utils.glide.SvgSoftwareLayerSetter
import com.currencywiki.currencyconverter.utils.setDrawableTint
import com.currencywiki.currencyconverter.utils.setSVGImageToImageView
import de.hdodenhof.circleimageview.CircleImageView

class CurrencyRecyclerViewAdapter :
    RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder?>() {

    var currencyItemArray = ArrayList<Currency>()
    var isSelected = true
    lateinit var adapterListener: CurrencyRecyclerViewAdapterListener
    private val requestBuilder = GlideApp.with(CurrencyConverterApp.instance!!.applicationContext)
        .`as`(PictureDrawable::class.java)
        .placeholder(R.drawable.bg_white_round)
        .error(R.drawable.bg_white_round)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(SvgSoftwareLayerSetter())

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_currency_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currencyItem = currencyItemArray[position]
        holder.flagImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setSVGImageToImageView(requestBuilder, holder.flagImageView, currencyItem.flag)
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager
        holder.symbolTextView.text = currencyItem.symbol
        holder.nameTextView.text = currencyItem.name

        if (isSelected) {
            holder.softButton.setImageResource(R.drawable.ic_delete_currency)
        }
        else {
            holder.softButton.setImageResource(R.drawable.ic_add_currency)
        }
        val drawable = holder.softButton.drawable
        setDrawableTint(drawable, getARGBColor(settingManager.gradient))

        if (isDarkMode()) {
            holder.itemContainer.setBackgroundResource(R.drawable.bg_dark_gray_round)
            holder.symbolTextView.setTextColor(Color.WHITE)
            holder.nameTextView.setTextColor(Color.WHITE)
        }
        else {
            holder.itemContainer.setBackgroundResource(R.drawable.bg_white_round)
            holder.symbolTextView.setTextColor(Color.DKGRAY)
            holder.nameTextView.setTextColor(Color.DKGRAY)
        }
    }

    override fun getItemCount(): Int {
        return currencyItemArray.size
    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val flagImageView: CircleImageView = view.findViewById(R.id.img_flag)
        val softButton: AppCompatImageView = view.findViewById(R.id.btn_soft)
        val symbolTextView: AppCompatTextView = view.findViewById(R.id.txt_symbol)
        val nameTextView: AppCompatTextView = view.findViewById(R.id.txt_name)
        val itemContainer: LinearLayout = view.findViewById(R.id.item_container)
        init {
            softButton.setOnClickListener {
                adapterListener.didSelectCurrency(adapterPosition, isSelected)
            }
        }
    }
}