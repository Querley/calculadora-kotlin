package com.ramyres.calculadora

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.FrameLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val engine = CalculatorEngine()
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getStringArrayList("calculator")?.let { engine.restoreState(it) }
        window.statusBarColor = Color.rgb(11, 18, 32)
        window.navigationBarColor = Color.rgb(11, 18, 32)
        setContentView(buildInterface())
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("calculator", ArrayList(engine.saveState()))
        super.onSaveInstanceState(outState)
    }

    private fun buildInterface(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(20))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(
                Color.rgb(11, 18, 32), Color.rgb(20, 31, 49)
            ))
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(11, 18, 32))
            addView(root, FrameLayout.LayoutParams(-1, -2))
            setOnApplyWindowInsetsListener { view, insets ->
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                insets.consumeSystemWindowInsets()
            }
        }

        val brand = TextView(this).apply {
            text = "CALCULADORA"
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = .18f
            setTextColor(Color.rgb(255, 159, 10))
            gravity = Gravity.START
        }
        root.addView(brand, LinearLayout.LayoutParams(-1, dp(44)))

        val displayPanel = LinearLayout(this).apply {
            minimumHeight = dp(180)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(20), dp(20), dp(20), dp(22))
            background = rounded(Color.rgb(26, 38, 57), 28f, Color.rgb(47, 63, 84))
        }
        expressionView = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.rgb(148, 163, 184))
            gravity = Gravity.END
            maxLines = 1
        }
        resultView = TextView(this).apply {
            textSize = 48f
            setTextColor(Color.WHITE)
            gravity = Gravity.END
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            maxLines = 1
            isSingleLine = true
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        displayPanel.addView(HorizontalScrollView(this).apply {
            isFillViewport = true
            addView(expressionView, FrameLayout.LayoutParams(-2, -1))
        }, LinearLayout.LayoutParams(-1, dp(44)))
        displayPanel.addView(HorizontalScrollView(this).apply {
            isFillViewport = true
            addView(resultView, FrameLayout.LayoutParams(-2, -1))
        }, LinearLayout.LayoutParams(-1, dp(80)))
        root.addView(displayPanel, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            bottomMargin = dp(20)
        })

        val grid = GridLayout(this).apply {
            columnCount = 4
            rowCount = 5
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = false
        }

        val keys = listOf(
            Key("AC", KeyType.ACTION), Key("+/−", KeyType.ACTION), Key("%", KeyType.ACTION), Key("÷", KeyType.OPERATOR),
            Key("7"), Key("8"), Key("9"), Key("×", KeyType.OPERATOR),
            Key("4"), Key("5"), Key("6"), Key("−", KeyType.OPERATOR),
            Key("1"), Key("2"), Key("3"), Key("+", KeyType.OPERATOR),
            Key("0", span = 2), Key(","), Key("=", KeyType.OPERATOR)
        )

        var row = 0
        var col = 0
        keys.forEach { key ->
            if (col + key.span > 4) { row++; col = 0 }
            val button = makeButton(key)
            val spec = GridLayout.LayoutParams(
                GridLayout.spec(row, 1f), GridLayout.spec(col, key.span, key.span.toFloat())
            ).apply {
                width = 0
                height = dp(72)
                setMargins(dp(5), dp(5), dp(5), dp(5))
            }
            grid.addView(button, spec)
            col += key.span
            if (col == 4) { row++; col = 0 }
        }
        root.addView(grid, LinearLayout.LayoutParams(-1, dp(410)))
        return scroll
    }

    private fun makeButton(key: Key): TextView = TextView(this).apply {
        text = key.label
        textSize = if (key.type == KeyType.OPERATOR) 29f else 23f
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        setTextColor(if (key.type == KeyType.ACTION) Color.rgb(17, 24, 39) else Color.WHITE)
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        background = when (key.type) {
            KeyType.OPERATOR -> rounded(Color.rgb(255, 159, 10), 22f)
            KeyType.ACTION -> rounded(Color.rgb(203, 213, 225), 22f)
            KeyType.NUMBER -> rounded(Color.rgb(52, 64, 84), 22f)
        }
        contentDescription = when (key.label) {
            "AC" -> "Limpar"; "+/−" -> "Alterar sinal"; "%" -> "Porcentagem"
            "÷" -> "Dividir"; "×" -> "Multiplicar"; "−" -> "Subtrair"
            "+" -> "Somar"; "=" -> "Resolver"; "," -> "Vírgula decimal"
            else -> key.label
        }
        setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            when (key.label) {
                "AC" -> engine.clear()
                "+/−" -> engine.toggleSign()
                "%" -> engine.percent()
                "+", "−", "×", "÷" -> engine.operation(key.label)
                "=" -> engine.equals()
                "," -> engine.digit(".")
                else -> engine.digit(key.label)
            }
            refresh()
        }
    }

    private fun refresh() {
        expressionView.text = engine.expression
        resultView.text = engine.display
        resultView.textSize = when {
            engine.display.length > 15 -> 28f
            engine.display.length > 11 -> 36f
            else -> 48f
        }
    }

    private fun rounded(color: Int, radius: Float, stroke: Int? = null) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radius.toInt()).toFloat()
        stroke?.let { setStroke(dp(1), it) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private data class Key(val label: String, val type: KeyType = KeyType.NUMBER, val span: Int = 1)
    private enum class KeyType { NUMBER, ACTION, OPERATOR }
}
