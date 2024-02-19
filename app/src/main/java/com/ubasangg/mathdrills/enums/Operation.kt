package com.ubasangg.mathdrills.enums

import com.ubasangg.mathdrills.classes.Problem
import com.ubasangg.mathdrills.interfaces.OperationInterface
import java.util.function.BinaryOperator

enum class Operation(val description: String, val index: Int)  : BinaryOperator<Int>, OperationInterface {
    ADDITION("+", 0) {
        override fun apply(t: Int, u: Int): Int = t + u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val min = difficulty.asMin
            val max = difficulty.asMax

            // beginner
            // max top number = 10, max bottom number = 10
            // no negative

            // easy - answer 0 to 100
            // intermediate - answer 0 to 1,000
            // hard - answer -10,000 to 10,000
            // math whiz - answer -100,000 to 100,000
            val answer = (min..max).random()
            val num1 =
                if(min == 0) (min..answer).random()
                else
                    if (answer < 0) (min..max + answer).random()
                    else (answer..max).random()
            val num2 = answer - num1
            return Problem(num1, num2, answer)
        }
    },
    SUBTRACTION("-", 1){
        override fun apply(t: Int, u: Int): Int = t - u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val min = difficulty.asMin
            val max = difficulty.asMax

            // beginner
            // max top number = 10, max bottom number = 10
            // no negative
            // minuend >= subtrahend

            // easy
            // answer 0 to 100
            // top number >= bottom number

            // intermediate
            // answer -1,000 to 1,000
            // bottom number can be greater than top number resulting to negative answer. No negative numbers on top and bottom numbers

            // hard - answer -10,000 to 10,000
            // math whiz - answer -100,000 to 100,000
            val answer = if(difficulty == Difficulty.INTERMEDIATE) (max * -1..max).random() else (min..max).random()
            val num1 =
                if (answer < 0) (min..max + answer).random()
                else (answer..max).random()
            val num2 = num1 - answer
            return Problem(num1, num2, answer)
        }
    },
    MULTIPLICATION("x", 2){
        override fun apply(t: Int, u: Int): Int = t * u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val bottomMin =
                when(difficulty) {
                    Difficulty.EASY, Difficulty.BEGINNER -> 0
                    else -> difficulty.mdBottom * -1
                }
            val bottomMax = difficulty.mdBottom
            val whizSign = intArrayOf(-1 , 1).random()
            val topMin =
                when(difficulty) {
                    Difficulty.EASY, Difficulty.BEGINNER -> 0
                    Difficulty.WHIZ -> 100 * whizSign
                    else -> difficulty.mdTop * -1
                }
            val topMax =
                if(difficulty == Difficulty.WHIZ) difficulty.mdTop * whizSign
                else difficulty.mdTop

            // beginner
            // max top number = 10, max bottom number = 10
            // no negative

            // easy
            // top max 2, and limit bottom number to 1
            // no negative

            // intermediate - top max 2 digits, bottom max 2 digits
            // hard - top max 3 digits, bottom max 2 digits
            // math whiz - top to 3 digits only (100 - 999), bottom max 3 digits
            val num1 =
                if(topMin < topMax) (topMin..topMax).random()
                else (topMax..topMin).random()
            val num2 = (bottomMin..bottomMax).random()
            val answer = this.apply(num1, num2)
            return Problem(num1, num2, answer)
        }
    },
    DIVISION("÷", 3){
        override fun apply(t: Int, u: Int): Int = t / u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val min = difficulty.mdBottom
            val max = difficulty.mdTop

            val num1 = 0
            val num2 = 0
            val answer = 0
            return Problem(num1, num2, answer)
        }
    };
}