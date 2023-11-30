package com.ubasangg.mathdrills.enums

import java.util.function.BinaryOperator
import java.util.function.LongBinaryOperator

enum class Operation(val description: String, val index: Int)  : BinaryOperator<Long> {
    ADDITION("+", 0) {
        override fun apply(t: Long, u: Long): Long = t + u
    },
    SUBTRACTION("-", 1){
        override fun apply(t: Long, u: Long): Long = t - u
    },
    MULTIPLICATION("x", 2){
        override fun apply(t: Long, u: Long): Long = t * u
    },
    DIVISION("÷", 3){
        override fun apply(t: Long, u: Long): Long = t / u
    };
}