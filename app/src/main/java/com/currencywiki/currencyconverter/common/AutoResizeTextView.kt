package com.currencywiki.currencyconverter.common

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.max
import kotlin.math.min


class AutoResizeTextView(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    AppCompatTextView(context, attrs, defStyle) {
    // Interface for resize notifications
    interface OnTextResizeListener {
        fun onTextResize(
            textView: TextView?,
            oldSize: Float,
            newSize: Float
        )
    }

    // Registered resize listener
    private var mTextResizeListener: OnTextResizeListener? = null

    // Flag for text and/or size changes to force a resize
    private var mNeedsResize = false

    // Text size that is set from code. This acts as a starting point for resizing
    private var mTextSize: Float

    // Temporary upper bounds on the starting text size
    private var mMaxTextSize = 80f

    // Lower bounds for text size
    private var mMinTextSize = MIN_TEXT_SIZE

    // Text view line spacing multiplier
    private var mSpacingMult = 1.0f

    // Text view additional line spacing
    private var mSpacingAdd = 0.0f

    // Sibling Text view
    lateinit var siblingTextView: AppCompatTextView
    var siblingTextViewWidth = 0
    lateinit var container: View
    var containerRect = Rect()

    private var offset = 0f

    /**
     * Return flag to add ellipsis to text that overflows at the smallest text size
     * @return
     */
    /**
     * Set flag to add ellipsis to text that overflows at the smallest text size
     * @param addEllipsis
     */
    // Add ellipsis to text that overflows at the smallest text size
    var addEllipsis = true

    // Default constructor override
    constructor(context: Context?) : this(context, null) {}

    // Default constructor when inflating from XML file
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        before: Int,
        after: Int
    ) {
        mNeedsResize = true
        // Since this view may be reused, it is good to reset the text size
        resetTextSize()
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
//            mNeedsResize = true
        }
    }

    /**
     * Register listener to receive resize notifications
     * @param listener
     */
    fun setOnResizeListener(listener: OnTextResizeListener?) {
        mTextResizeListener = listener
    }

    /**
     * Override the set text size to update our internal reference values
     */
    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        mTextSize = textSize
    }

    /**
     * Override the set text size to update our internal reference values
     */
    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        mTextSize = textSize
    }

    /**
     * Override the set line spacing to update our internal reference values
     */
    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        mSpacingMult = mult
        mSpacingAdd = add
    }

    /**
     * Return upper text size limit
     * @return
     */
    /**
     * Set the upper text size limit and invalidate the view
     * @param maxTextSize
     */
    var maxTextSize: Float
        get() = mMaxTextSize
        set(maxTextSize) {
            mMaxTextSize = maxTextSize
            requestLayout()
            invalidate()
        }

    /**
     * Return lower text size limit
     * @return
     */
    /**
     * Set the lower text size limit and invalidate the view
     * @param minTextSize
     */
    var minTextSize: Float
        get() = mMinTextSize
        set(minTextSize) {
            mMinTextSize = minTextSize
            requestLayout()
            invalidate()
        }

    /**
     * Reset the text to the original size
     */
    private fun resetTextSize() {
        if (mTextSize > 0) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
//            mMaxTextSize = mTextSize
        }
    }

    /**
     * Resize text after measuring
     */
    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (changed && mNeedsResize) {
            if (!this::siblingTextView.isInitialized) {
                return
            }

            container.getGlobalVisibleRect(containerRect)
            siblingTextViewWidth = getTextViewSize(siblingTextView, siblingTextView.text.toString()).width()
            val widthLimit =
                right - left - compoundPaddingLeft - compoundPaddingRight
            val heightLimit =
                bottom - top - compoundPaddingBottom - compoundPaddingTop
            resizeText(widthLimit, heightLimit)
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    /**
     * Resize the text size with default width and height
     */
    fun resizeText() {
        val heightLimit = height - paddingBottom - paddingTop
        val widthLimit = width - paddingLeft - paddingRight
        resizeText(widthLimit, heightLimit)
    }

    private fun getTextViewSize(textView: AppCompatTextView, text: String): Rect {
        return getTextViewSize(textView, text, 0F)
    }

    private fun getTextViewSize(textView: AppCompatTextView, text: String, fontSize: Float): Rect {
        val bounds = Rect()
        val textPaint = textView.paint
        val paintCopy = TextPaint(textPaint)
        if (fontSize > 0) {
            paintCopy.textSize = fontSize
        }
        paintCopy.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    /**
     * Resize the text size with specified width and height
     * @param width
     * @param height
     */
    private fun resizeText(width: Int, height: Int) {
        var text = text
        // Do not resize if the view does not have dimensions or there is no text
        if (text == null || text.isEmpty() || height <= 0 || width <= 0 || mTextSize == 0f) {
            return
        }
        if (transformationMethod != null) {
            text = transformationMethod.getTransformation(text, this)
        }

        // Get the text view's paint object
        val textPaint = paint

        // Store the current text size
        val oldTextSize = textPaint.textSize
        // If there is a max text size set, use the lesser of that and the default text size
        var targetTextSize =
            if (mMaxTextSize > 0) min(mTextSize, mMaxTextSize) else mTextSize
        var targetSiblingTextSize =
            if (mMaxTextSize > 0) min(siblingTextView.textSize, mMaxTextSize) else siblingTextView.textSize

        var textWidth = getTextViewSize(this, text.toString(), targetTextSize).width()
        var siblingTextWidth = getTextViewSize(siblingTextView, siblingTextView.text.toString(), targetSiblingTextSize).width()
        container.getGlobalVisibleRect(containerRect)
        val containerRectWidth = containerRect.width()
        var offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24.0f, resources.displayMetrics)

        val textContentWidth = textWidth + siblingTextWidth + offset
        if (textContentWidth > containerRectWidth) {
            while (textWidth + siblingTextWidth + offset > containerRectWidth && targetTextSize > minTextSize) {
                targetTextSize = max(targetTextSize - 1, mMinTextSize)
                targetSiblingTextSize = max(targetSiblingTextSize - 1, mMinTextSize)
                textWidth = getTextViewSize(this, text.toString(), targetTextSize).width()
                siblingTextWidth = getTextViewSize(
                    siblingTextView,
                    siblingTextView.text.toString(),
                    targetSiblingTextSize
                ).width()
            }
        }
        else if (textContentWidth < containerRectWidth) {
            var fontIncreased = false
            while (textWidth + siblingTextWidth + offset < containerRectWidth && targetTextSize < maxTextSize) {
                targetTextSize = min(targetTextSize + 1, mMaxTextSize)
                targetSiblingTextSize = min(targetSiblingTextSize + 1, mMaxTextSize)
                textWidth = getTextViewSize(this, text.toString(), targetTextSize).width()
                siblingTextWidth = getTextViewSize(
                    siblingTextView,
                    siblingTextView.text.toString(),
                    targetSiblingTextSize
                ).width()
                fontIncreased = true
            }
            if (fontIncreased) {
                targetTextSize -= 1
                targetSiblingTextSize -= 1
            }
        }

        // Some devices try to auto adjust line spacing, so force default line spacing
        // and invalidate the layout as a side effect
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize)
        siblingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetSiblingTextSize)
        setLineSpacing(mSpacingAdd, mSpacingMult)

        // Notify the listener if registered
        if (mTextResizeListener != null) {
            mTextResizeListener!!.onTextResize(this, oldTextSize, targetTextSize)
        }

        // Reset force resize flag
        mNeedsResize = false
        Handler().postDelayed({
            requestLayout()
            invalidate()
        }, 5)
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private fun getTextHeight(
        source: CharSequence?,
        paint: TextPaint,
        width: Int,
        textSize: Float
    ): Int {
        // modified: make a copy of the original TextPaint object for measuring
        // (apparently the object gets modified while measuring, see also the
        // docs for TextView.getPaint() (which states to access it read-only)
        val paintCopy = TextPaint(paint)
        // Update the text paint object
        paintCopy.textSize = textSize
        // Measure using a static layout
        val layout = StaticLayout(
            source,
            paintCopy,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            mSpacingMult,
            mSpacingAdd,
            true
        )
        return layout.height
    }

    companion object {
        // Minimum text size for this text view
        const val MIN_TEXT_SIZE = 20f

        // Our ellipse string
        private const val mEllipsis = "..."
    }

    // Default constructor override
    init {
        mTextSize = textSize
    }
}