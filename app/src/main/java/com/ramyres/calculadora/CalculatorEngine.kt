package com.ramyres.calculadora

import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorEngine {
    private var accumulator: BigDecimal? = null
    private var pendingOperation: String? = null
    private var input = "0"
    private var waitingForOperand = false
    private var justSolved = false

    var expression: String = "Pronto para calcular"
        private set

    val display: String get() = if (input == "Erro") input else formatInput()

    fun digit(value: String) {
        require(value == "." || value.matches(Regex("[0-9]")))
        if (waitingForOperand || justSolved || input == "Erro") {
            input = "0"
            waitingForOperand = false
            justSolved = false
        }
        if (value == ".") {
            if (!input.contains(".")) input += "."
        } else {
            if (input.count { it.isDigit() } >= 15) return
            input = when (input) {
                "0" -> value
                "-0" -> "-$value"
                else -> input + value
            }
        }
        updateExpression()
    }

    fun operation(symbol: String) {
        require(symbol in listOf("+", "−", "×", "÷"))
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
        if (input == "Erro" || waitingForOperand) return
        val original = parseInput()
        var value = original.movePointLeft(2)
        if (pendingOperation == "+" || pendingOperation == "−") {
            value = (accumulator ?: BigDecimal.ZERO).multiply(value)
        }
        expression = if (pendingOperation == null) "${format(original)}% ="
            else "${format(accumulator ?: BigDecimal.ZERO)} $pendingOperation ${format(original)}%"
        input = plain(value)
        justSolved = true
    }

    fun toggleSign() {
        if (input == "Erro") return
        if (waitingForOperand) {
            input = "0"
            waitingForOperand = false
        }
        input = if (input.startsWith("-")) input.removePrefix("-") else "-$input"
        justSolved = false
        updateExpression()
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
        justSolved = true
    }

    private fun parseInput(): BigDecimal = input.toBigDecimalOrNull() ?: BigDecimal.ZERO
    private fun formatInput(): String {
        val parts = input.split(".", limit = 2)
        val integer = parts[0].removePrefix("-").reversed().chunked(3).joinToString(".").reversed()
        return (if (input.startsWith("-")) "−" else "") + integer +
            (if (parts.size == 2) ",${parts[1]}" else "")
    }

    private fun updateExpression() {
        expression = if (pendingOperation == null) formatInput()
            else "${format(accumulator ?: BigDecimal.ZERO)} $pendingOperation ${formatInput()}"
    }

    fun saveState(): List<String> = listOf(input, accumulator?.toPlainString().orEmpty(),
        pendingOperation.orEmpty(), waitingForOperand.toString(), justSolved.toString(), expression)

    fun restoreState(state: List<String>) {
        if (state.size != 6) return
        input = state[0]
        accumulator = state[1].toBigDecimalOrNull()
        pendingOperation = state[2].ifEmpty { null }
        waitingForOperand = state[3].toBoolean()
        justSolved = state[4].toBoolean()
        expression = state[5]
    }

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
