package com.ramyres.calculadora

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {
    @Test fun soma() = assertCalculation("12", "+", "8", "20")
    @Test fun subtracao() = assertCalculation("50", "−", "17", "33")
    @Test fun multiplicacao() = assertCalculation("7", "×", "9", "63")
    @Test fun divisao() = assertCalculation("100", "÷", "4", "25")

    @Test fun porcentagem() {
        val calculator = CalculatorEngine()
        enter(calculator, "15")
        calculator.percent()
        assertEquals("0,15", calculator.display)
    }

    @Test fun alteraSinal() {
        val calculator = CalculatorEngine()
        enter(calculator, "42")
        calculator.toggleSign()
        assertEquals("−42", calculator.display)
    }

    @Test fun divisaoPorZeroExibeErro() {
        val calculator = CalculatorEngine()
        enter(calculator, "8")
        calculator.operation("÷")
        enter(calculator, "0")
        calculator.equals()
        assertEquals("Não é possível dividir por zero", calculator.expression)
    }

    private fun assertCalculation(left: String, operation: String, right: String, expected: String) {
        val calculator = CalculatorEngine()
        enter(calculator, left)
        calculator.operation(operation)
        enter(calculator, right)
        calculator.equals()
        assertEquals(expected, calculator.display)
    }

    private fun enter(calculator: CalculatorEngine, value: String) {
        value.forEach { calculator.digit(it.toString()) }
    }
}
