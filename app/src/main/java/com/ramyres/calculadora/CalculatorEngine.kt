package com.ramyres.calculadora

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class CalculatorEngine {
    private var accumulator: BigDecimal? = null
    private var pendingOperation: String? = null
    private var input = "0"
    private var waitingForOperand = false
    private var justSolved = false

    var expression: String = "Pronto para calcular"
        private set

    val display: String get() = format(parseInput())

    fun digit(value: String) {
        if (waitingForOperand || justSolved || input == "Erro") {
            input = "0"
            waitingForOperand = false
            justSolved = false
        }
        if (value == ".") {
            if (!input.contains(".")) input += "."
        } else {
            input = if (input == "0") value else input + value
        }
        expression = if (pendingOperation == null) "Digitando" else expression.substringBeforeLast(" ") + " " + formatInput()
    }

    fun operation(symbol: String) {
        if (input == "Erro") clear()
        val current = parseInput()
        if (pendingOperation != null && !waitingForOperand) {
            val result = calculate(accumulator ?: BigDecimal.ZERO, current, pendingOperation!!)
            if (result == null) return showError()
            accumulator = result
            input = plain(result)
        } else {
            accumulator = current
        }
        pendingOperation = symbol
        expression = "${format(accumulator!!)} $symbol"
        waitingForOperand = true
        justSolved = false
    }

    fun equals() {
        val op = pendingOperation ?: return
        val right = parseInput()
        val left = accumulator ?: BigDecimal.ZERO
        val result = calculate(left, right, op) ?: return showError()
        expression = "${format(left)} $op ${format(right)} ="
        input = plain(result)
        accumulator = result
        pendingOperation = null
        waitingForOperand = false
        justSolved = true
    }

    fun percent() {
        if (input == "Erro") return
        val value = parseInput().divide(BigDecimal(100), MathContext.DECIMAL64)
        expression = "${format(parseInput())}% ="
        input = plain(value)
        justSolved = true
    }

    fun toggleSign() {
        if (input == "Erro" || parseInput().compareTo(BigDecimal.ZERO) == 0) return
        input = plain(parseInput().negate())
        justSolved = false
    }

    fun clear() {
        accumulator = null
        pendingOperation = null
        input = "0"
        waitingForOperand = false
        justSolved = false
        expression = "Pronto para calcular"
    }

    private fun calculate(a: BigDecimal, b: BigDecimal, op: String): BigDecimal? = when (op) {
        "+" -> a.add(b)
        "−" -> a.subtract(b)
        "×" -> a.multiply(b)
        "÷" -> if (b.compareTo(BigDecimal.ZERO) == 0) null else a.divide(b, 12, RoundingMode.HALF_UP).stripTrailingZeros()
        else -> b
    }

    private fun showError() {
        input = "Erro"
        expression = "Não é possível dividir por zero"
        accumulator = null
        pendingOperation = null
        waitingForOperand = false
    }

    private fun parseInput(): BigDecimal = input.toBigDecimalOrNull() ?: BigDecimal.ZERO
    private fun formatInput(): String = if (input == "Erro") input else format(parseInput())
    private fun plain(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()

    private fun format(value: BigDecimal): String {
        val normalized = value.stripTrailingZeros()
        val plain = normalized.toPlainString()
        val parts = plain.split(".")
        val negative = parts[0].startsWith("-")
        val digits = parts[0].removePrefix("-")
        val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
        val decimal = parts.getOrNull(1)?.let { ",$it" } ?: ""
        return (if (negative) "−" else "") + grouped + decimal
    }
}
