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
        assertEquals("Erro", calculator.display)
        assertEquals("Não é possível dividir por zero", calculator.expression)
    }

    @Test fun expressaoMantemOperadorDuranteDigitacao() {
        val calculator = CalculatorEngine()
        enter(calculator, "12")
        calculator.operation("+")
        enter(calculator, "85")
        assertEquals("12 + 85", calculator.expression)
    }

    @Test fun decimaisAparecemDuranteDigitacaoESomamComPrecisao() {
        val calculator = CalculatorEngine()
        enter(calculator, "0.")
        assertEquals("0,", calculator.display)
        enter(calculator, "10")
        assertEquals("0,10", calculator.display)
        calculator.operation("+")
        enter(calculator, "0.2")
        calculator.equals()
        assertEquals("0,3", calculator.display)
    }

    @Test fun trocaOperadorSemPerderPrimeiroNumero() {
        val calculator = CalculatorEngine()
        enter(calculator, "12")
        calculator.operation("+")
        calculator.operation("×")
        enter(calculator, "3")
        calculator.equals()
        assertEquals("36", calculator.display)
    }

    @Test fun permiteDigitarSegundoNumeroNegativo() {
        val calculator = CalculatorEngine()
        enter(calculator, "5")
        calculator.operation("×")
        calculator.toggleSign()
        enter(calculator, "2")
        calculator.equals()
        assertEquals("−10", calculator.display)
        assertEquals("5 × −2 =", calculator.expression)
    }

    @Test fun porcentagemEmAdicaoEDesconto() {
        for ((operacao, esperado) in listOf("+" to "220", "−" to "180")) {
            val calculator = CalculatorEngine()
            enter(calculator, "200")
            calculator.operation(operacao)
            enter(calculator, "10")
            calculator.percent()
            calculator.equals()
            assertEquals(esperado, calculator.display)
        }
    }

    @Test fun multiplicacaoPorPorcentagem() {
        val calculator = CalculatorEngine()
        enter(calculator, "200")
        calculator.operation("×")
        enter(calculator, "10")
        calculator.percent()
        calculator.equals()
        assertEquals("20", calculator.display)
    }

    @Test fun encadeiaOperacoesEIniciaNovoCalculo() {
        val calculator = CalculatorEngine()
        enter(calculator, "2")
        calculator.operation("+")
        enter(calculator, "3")
        calculator.operation("×")
        enter(calculator, "4")
        calculator.equals()
        assertEquals("20", calculator.display)
        enter(calculator, "8")
        assertEquals("8", calculator.display)
        calculator.clear()
        assertEquals("0", calculator.display)
    }

    @Test fun recuperaDepoisDeErro() {
        val calculator = CalculatorEngine()
        enter(calculator, "8")
        calculator.operation("÷")
        enter(calculator, "0")
        calculator.equals()
        enter(calculator, "2")
        calculator.operation("+")
        enter(calculator, "3")
        calculator.equals()
        assertEquals("5", calculator.display)
    }

    @Test fun restauraCalculoEmAndamento() {
        val anterior = CalculatorEngine()
        enter(anterior, "12")
        anterior.operation("+")
        enter(anterior, "0.5")
        val nova = CalculatorEngine()
        nova.restoreState(anterior.saveState())
        nova.equals()
        assertEquals("12,5", nova.display)
    }

    @Test fun divisaoPeriodicaArredondaEmDozeCasas() = assertCalculation("1", "÷", "3", "0,333333333333")

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
