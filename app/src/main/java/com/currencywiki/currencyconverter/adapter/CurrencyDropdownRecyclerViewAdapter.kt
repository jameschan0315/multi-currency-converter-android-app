package com.currencywiki.currencyconverter.adapter

import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.getARGBColor
import com.currencywiki.currencyconverter.common.isDarkMode
import com.currencywiki.currencyconverter.model.Currency
import com.currencywiki.currencyconverter.utils.setDrawableTint
import com.currencywiki.currencyconverter.utils.setSVGImageToImageView
import de.hdodenhof.circleimageview.CircleImageView


class CurrencyDropdownRecyclerViewAdapter :
    RecyclerView.Adapter<CurrencyDropdownRecyclerViewAdapter.ViewHolder?>() {

    var currencyItemArray = ArrayList<Currency>()
    var selectedSymbol = ""
    lateinit var adapterListener: CurrencyDropdownRecyclerViewAdapterListener
    var requestBuilder: RequestBuilder<PictureDrawable>? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dropdown_currency_list, parent, false)
        return ViewHolder(view)
    }

    fun updateCurrencyItemArray(newCurrencyItemArray: ArrayList<Currency>) {
        var replaceCurrencyItemArray = ArrayList<Currency>()
        for (newCurrencyItem in newCurrencyItemArray) {
            replaceCurrencyItemArray.add(newCurrencyItem)
        }
        currencyItemArray = replaceCurrencyItemArray
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

        if (!currencyItem.favorite) {
            holder.starImageView.setImageResource(R.drawable.ic_star_blank)
        }
        else {
            holder.starImageView.setImageResource(R.drawable.ic_star_filled)
        }
        val drawable = holder.starImageView.drawable
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
        val starImageView: AppCompatImageView = view.findViewById(R.id.img_star)
        val symbolTextView: AppCompatTextView = view.findViewById(R.id.txt_symbol)
        val nameTextView: AppCompatTextView = view.findViewById(R.id.txt_name)
        val itemContainer: LinearLayout = view.findViewById(R.id.item_container)
        init {
            itemContainer.setOnClickListener {
                adapterListener.didSelectCurrency(adapterPosition, currencyItemArray[adapterPosition].symbol)
            }
            starImageView.setOnClickListener {
                adapterListener.didSelectStar(adapterPosition, currencyItemArray[adapterPosition].symbol)
            }
        }
    }
}