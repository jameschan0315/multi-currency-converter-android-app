package com.currencywiki.currencyconverter.common

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.daasuu.bl.BubbleLayout

class BubbleLayoutTouch(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0): BubbleLayout(context, attrs, defStyleAttr) {

    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?
    ) : this(context, attrs, 0) {
    }

    constructor(context: Context?) : this(context, null, 0) {
    }

    fun showWithAnchor(anchor: View) {
        var rect = Rect()
        anchor.getGlobalVisibleRect(rect)
        this.arrowPosition = rect.left.toFloat()
        this.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.elevation = 6F
        }
    }
}