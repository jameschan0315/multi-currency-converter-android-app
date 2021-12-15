package com.currencywiki.currencyconverter.common

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import com.currencywiki.currencyconverter.CurrencyConverterApp
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.utils.MyKeyboardInterface
import com.currencywiki.currencyconverter.utils.setDrawableTint

class MyKeyboard(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {
    // constructors
    constructor(context: Context?) : this(context, null, 0) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}

    // keyboard keys (buttons)
    private lateinit var button1: AppCompatButton
    private lateinit var button2: AppCompatButton
    private lateinit var button3: AppCompatButton
    private lateinit var button4: AppCompatButton
    private lateinit var button5: AppCompatButton
    private lateinit var button6: AppCompatButton
    private lateinit var button7: AppCompatButton
    private lateinit var button8: AppCompatButton
    private lateinit var button9: AppCompatButton
    private lateinit var button0: AppCompatButton
    private lateinit var clearButton: AppCompatButton
    private lateinit var deleteButton: AppCompatImageButton
    private lateinit var divideButton: AppCompatImageButton
    private lateinit var percentButton: AppCompatButton
    private lateinit var multiplyButton: AppCompatImageButton
    private lateinit var minusButton: AppCompatButton
    private lateinit var plusButton: AppCompatButton
    private lateinit var equalButton: AppCompatButton
    private lateinit var dotButton: AppCompatButton
    private lateinit var layoutOperator: LinearLayout
    private lateinit var layoutNumberPane: LinearLayout
    private lateinit var layoutOperatorPane: LinearLayout

    var keyboardInterface: MyKeyboardInterface? = null

    private var number1: Float? = null
    private var operator: String? = null
    private var number2: Float? = null
    private var currentInput = 0 // 0: First Number, 1: Operator, 2: 2nd Number

    var keyValues: SparseArray<String> = SparseArray()

    private var inputConnection: InputConnection? = null
    private fun init(context: Context?, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.keyboard, this, true)
        button1 = findViewById(R.id.button_1)
        button2 = findViewById(R.id.button_2)
        button3 = findViewById(R.id.button_3)
        button4 = findViewById(R.id.button_4)
        button5 = findViewById(R.id.button_5)
        button6 = findViewById(R.id.button_6)
        button7 = findViewById(R.id.button_7)
        button8 = findViewById(R.id.button_8)
        button9 = findViewById(R.id.button_9)
        button0 = findViewById(R.id.button_0)
        clearButton = findViewById(R.id.btn_clear)
        deleteButton = findViewById(R.id.btn_backspace)
        divideButton = findViewById(R.id.btn_divide)
        percentButton = findViewById(R.id.btn_percent)
        multiplyButton = findViewById(R.id.btn_multiply)
        minusButton = findViewById(R.id.btn_minus)
        plusButton = findViewById(R.id.btn_plus)
        equalButton = findViewById(R.id.btn_equal)
        dotButton = findViewById(R.id.button_dot)
        layoutOperator = findViewById(R.id.layout_operator)
        layoutNumberPane = findViewById(R.id.layout_number_pane)
        layoutOperatorPane = findViewById(R.id.layout_operator_pane)

        // set button click listeners
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
        button9.setOnClickListener(this)
        button0.setOnClickListener(this)
        clearButton.setOnClickListener(this)
        deleteButton.setOnClickListener(this)
        divideButton.setOnClickListener(this)
        percentButton.setOnClickListener(this)
        multiplyButton.setOnClickListener(this)
        minusButton.setOnClickListener(this)
        plusButton.setOnClickListener(this)
        equalButton.setOnClickListener(this)
        dotButton.setOnClickListener(this)

        // map buttons IDs to input strings
        keyValues.put(R.id.button_1, "1")
        keyValues.put(R.id.button_2, "2")
        keyValues.put(R.id.button_3, "3")
        keyValues.put(R.id.button_4, "4")
        keyValues.put(R.id.button_5, "5")
        keyValues.put(R.id.button_6, "6")
        keyValues.put(R.id.button_7, "7")
        keyValues.put(R.id.button_8, "8")
        keyValues.put(R.id.button_9, "9")
        keyValues.put(R.id.button_0, "0")

        applySetting()
    }

    fun applySetting() {
        val settingManager = CurrencyConverterApp.instance!!.appData.settingManager

        layoutOperator.background = getGradientDrawable(settingManager.gradient)
        layoutNumberPane.background = getGradientDrawable(settingManager.gradient)
        layoutOperatorPane.background = getGradientDrawable(settingManager.gradient)

        val multiplyDrawable = multiplyButton.drawable
        val divideDrawable = divideButton.drawable
        val deleteDrawable = deleteButton.drawable
        if (isDarkMode()) {
            button0.setTextColor(Color.DKGRAY)
            button1.setTextColor(Color.DKGRAY)
            button2.setTextColor(Color.DKGRAY)
            button3.setTextColor(Color.DKGRAY)
            button4.setTextColor(Color.DKGRAY)
            button5.setTextColor(Color.DKGRAY)
            button6.setTextColor(Color.DKGRAY)
            button7.setTextColor(Color.DKGRAY)
            button8.setTextColor(Color.DKGRAY)
            button9.setTextColor(Color.DKGRAY)
            clearButton.setTextColor(Color.DKGRAY)
            dotButton.setTextColor(Color.DKGRAY)
            minusButton.setTextColor(Color.DKGRAY)
            plusButton.setTextColor(Color.DKGRAY)
            equalButton.setTextColor(Color.DKGRAY)
            percentButton.setTextColor(Color.DKGRAY)
            setDrawableTint(multiplyDrawable, Color.DKGRAY)
            setDrawableTint(divideDrawable, Color.DKGRAY)
            setDrawableTint(deleteDrawable, Color.DKGRAY)
        }
        else {
            button0.setTextColor(Color.WHITE)
            button1.setTextColor(Color.WHITE)
            button2.setTextColor(Color.WHITE)
            button3.setTextColor(Color.WHITE)
            button4.setTextColor(Color.WHITE)
            button5.setTextColor(Color.WHITE)
            button6.setTextColor(Color.WHITE)
            button7.setTextColor(Color.WHITE)
            button8.setTextColor(Color.WHITE)
            button9.setTextColor(Color.WHITE)
            clearButton.setTextColor(Color.WHITE)
            dotButton.setTextColor(Color.WHITE)
            minusButton.setTextColor(Color.WHITE)
            plusButton.setTextColor(Color.WHITE)
            equalButton.setTextColor(Color.WHITE)
            percentButton.setTextColor(Color.WHITE)
            setDrawableTint(multiplyDrawable, Color.WHITE)
            setDrawableTint(divideDrawable, Color.WHITE)
            setDrawableTint(deleteDrawable, Color.WHITE)
        }
    }

    private fun clearText(currentText: CharSequence) {
        if (inputConnection == null) {
            return
        }

        val beforeCursorText =
            inputConnection!!.getTextBeforeCursor(currentText.length, 0)
        val afterCursorText =
            inputConnection!!.getTextAfterCursor(currentText.length, 0)
        inputConnection!!.deleteSurroundingText(beforeCursorText.length, afterCursorText.length)
    }

    private fun commitText(text: String, currentText: CharSequence) {
        if (currentText.length > 25) {
            return
        }
        inputConnection!!.commitText(text, 1)
    }

    override fun onClick(v: View) {

        // do nothing if the InputConnection has not been set yet
        if (inputConnection == null) return

        // Delete text or input key value
        // All communication goes through the InputConnection
        val currentText =
            inputConnection!!.getExtractedText(ExtractedTextRequest(), 0).text
        when {
            v.id === R.id.btn_clear -> {
                clearText(currentText)
                number1 = null
                operator = null
                number2 = null
            }
            v.id == R.id.button_dot -> {
                if (!canDotInput(currentText)) {
                    return
                }
                commitText(".", currentText)
            }
            v.id == R.id.btn_divide -> {
                if (!isLastCharacterNumberOrPercent(currentText)) {
                    return
                }

                commitText("÷", currentText)
            }
            v.id == R.id.btn_percent -> {
                if (!isLastCharacterNumber(currentText)) {
                    return
                }

                commitText("%", currentText)
            }
            v.id == R.id.btn_multiply -> {
                if (!isLastCharacterNumberOrPercent(currentText)) {
                    return
                }

                commitText("✕", currentText)
            }
            v.id == R.id.btn_minus -> {
                if (!isLastCharacterNumberOrPercent(currentText)) {
                    return
                }

                commitText("-", currentText)
            }
            v.id == R.id.btn_plus -> {
                if (!isLastCharacterNumberOrPercent(currentText)) {
                    return
                }

                commitText("+", currentText)
            }
            v.id == R.id.btn_equal -> {
                if (!isLastCharacterNumberOrPercent(currentText)) {
                    return
                }
                val result = calculateFormula(currentText)
                clearText(currentText)
                inputConnection!!.commitText(result.toString(), 1)
                keyboardInterface?.didClickEqual()

            }
            v.id == R.id.btn_backspace -> {
                val selectedText = inputConnection!!.getSelectedText(0)
                if (TextUtils.isEmpty(selectedText)) {
                    inputConnection!!.deleteSurroundingText(1, 0)
                } else {
                    inputConnection!!.commitText("", 1)
                }
            }
            else -> {
                if (currentInput == 1) {
                    clearText(currentText)
                    currentInput = 2
                }

                val value: String = keyValues.get(v.id)
                commitText(value, currentText)
            }
        }
    }

    // The activity (or some parent or controller) must give us
    // a reference to the current EditText's InputConnection
    fun setInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }

    private fun isLastCharacterNumber(currentText: CharSequence): Boolean {
        if (currentText.isEmpty()) {
            return false
        }

        val lastCharacter = currentText[currentText.length - 1]
        if (lastCharacter in '0'..'9') {
            return true
        }
        return false
    }

    private fun isLastCharacterNumberOrPercent(currentText: CharSequence): Boolean {
        if (currentText.isEmpty()) {
            return false
        }

        val lastCharacter = currentText[currentText.length - 1]
        if (lastCharacter in '0'..'9') {
            return true
        }
        if (lastCharacter == '%') {
            return true
        }
        return false
    }

    init {
        init(context, attrs)
    }

    private fun canDotInput(currentText: CharSequence): Boolean {
        if (currentText.isEmpty()) {
            return false
        }

        var index = currentText.length - 1
        val lastChar = currentText[index]
        if (lastChar == '.' || lastChar == '+' || lastChar == '-'
            || lastChar == '✕' || lastChar == '÷' || lastChar == '%') {

            return false
        }

        while (index >= 0) {
            val charAt = currentText[index]
            if (charAt in '0'..'9' || charAt == '.') {
                index -= 1
                continue
            }
            val lastTerm = currentText.subSequence(index, currentText.length)
            return !lastTerm.toString().contains('.')
        }
        return true
    }

    private fun calculateFormula(currentText: CharSequence): Float {
        var result = 0.0F
        if (currentText.isEmpty()) {
            return result
        }

        var index = 0
        var startIndex = 0
        var operator = '+'
        var previousTerm = result
        while (index < currentText.length) {
            val charAt = currentText[index]
            index += 1
            if (charAt != '+' && charAt != '-' && index < currentText.length) {
                continue
            }
            var termStr: CharSequence = if (index == currentText.length) {
                currentText.subSequence(startIndex, index)
            } else {
                currentText.subSequence(startIndex, index - 1)
            }

            val termResult = calculateTerm(termStr, previousTerm)
            previousTerm = termResult
            if (operator == '+') {
                result += termResult
            }
            if (operator == '-') {
                result -= termResult
            }
            operator = charAt
            startIndex = index
        }
        return result
    }

    private fun calculateTerm(term: CharSequence, previousTerm: Float = 1f): Float {
        var result = 1F
        var operator = '✕'
        var index = 0
        var startIndex = 0
        var previousNumber = previousTerm
        while (index < term.length) {
            val charAt = term[index]
            index += 1
            if (charAt != '✕' && charAt != '÷' && index < term.length) {
                continue
            }
            var percentTermStr: CharSequence = if (index == term.length) {
                term.subSequence(startIndex, index)
            } else {
                term.subSequence(startIndex, index - 1)
            }

            val percentTermResult = calculatePercentTerm(percentTermStr, previousNumber)
            previousNumber = percentTermResult
            if (operator == '✕') {
                result *= percentTermResult
            }
            if (operator == '÷') {
                result /= percentTermResult
            }
            operator = charAt
            startIndex = index
        }
        return result
    }

    private fun calculatePercentTerm(percentTerm: CharSequence, previousPercentTerm: Float = 1.0f): Float {
        var result = 1f
        var index = 0
        var startIndex = 0
        var previousNumber = previousPercentTerm
        while (index < percentTerm.length) {
            val charAt = percentTerm[index]
            index += 1
            if (charAt != '%' && index < percentTerm.length) {
                continue
            }
            var numberStr: CharSequence = if (index == percentTerm.length && percentTerm[percentTerm.length - 1] in '0'..'9') {
                percentTerm.subSequence(startIndex, index)
            } else {
                percentTerm.subSequence(startIndex, index - 1)
            }
            val number = numberStr.toString().toFloat()
            if (startIndex == 0) {
                result = number
            }
            else {
                result = result * number / 100f
                previousNumber = result
            }
            if (index == percentTerm.length && charAt == '%') {
                result = previousNumber * number / 100f
            }
            startIndex = index
        }
        return result
    }

//    private fun calculateTerm(term: CharSequence, previousTerm: Float = 1f): Float {
//        var result = 1F
//        var operator = '✕'
//        var index = 0
//        var startIndex = 0
//        var previousNumber = previousTerm
//        while (index < term.length) {
//            val charAt = term[index]
//            index += 1
//            if (charAt != '✕' && charAt != '÷' && charAt != '%' && index < term.length) {
//                continue
//            }
//            var numberStr: CharSequence = if (index == term.length && term[term.length - 1] in '0'..'9') {
//                term.subSequence(startIndex, index)
//            } else {
//                term.subSequence(startIndex, index - 1)
//            }
//            val number = numberStr.toString().toFloat()
//            if (operator == '✕') {
//                result *= number
//            }
//            if (operator == '÷') {
//                result /= number
//            }
//            if (charAt == '%' && index == term.length) {
//                result = previousNumber * number / 100.0F
//            }
//            else if (charAt == '%' && term[index] !in '0'..'9') {
//                result = previousNumber * number / 100.0F
//            }
//            if (operator == '%') {
//                result = result * number / 100.0F
//            }
//            operator = charAt
//            startIndex = index
//        }
//        return result
//    }
}