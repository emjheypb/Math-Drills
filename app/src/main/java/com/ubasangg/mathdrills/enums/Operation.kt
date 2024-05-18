package com.ubasangg.mathdrills.enums

import com.ubasangg.mathdrills.classes.Problem
import com.ubasangg.mathdrills.interfaces.OperationInterface
import java.util.function.BinaryOperator
import kotlin.math.abs

enum class Operation(val sign: String, val index: Int, val description: String) :
    BinaryOperator<Int>, OperationInterface {
    ADDITION("+", 0, "Addition") {
        override fun apply(t: Int, u: Int): Int = t + u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val min = difficulty.asdMin
            val max = difficulty.asdMax

            // beginner
            // max top number = 10, max bottom number = 10
            // no negative

            // easy - answer 0 to 100
            // intermediate - answer 0 to 1,000
            // hard - answer -10,000 to 10,000
            // math whiz - answer -100,000 to 100,000
            val answer =
                if (difficulty == Difficulty.BEGINNER) (min..max * 2).random()
                else (min..max).random()
            val num1 =
                if (difficulty == Difficulty.BEGINNER && answer > 10) (answer - max..max).random()
                else if (min == 0) (min..answer).random()
                else
                    if (answer < 0) (min..max + answer).random()
                    else (answer..max).random()
            val num2 = answer - num1
            return Problem(num1, num2, answer)
        }
    },
    SUBTRACTION("-", 1, "Subtraction") {
        override fun apply(t: Int, u: Int): Int = t - u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val min = difficulty.asdMin
            val max = difficulty.asdMax

            // beginner
            // max top number = 20, max bottom number = 10
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
            val answer =
                if (difficulty == Difficulty.INTERMEDIATE) (max * -1..max).random() else (min..max).random()
            val num1 =
                if (answer < 0) (min..max + answer).random()
                else if (difficulty == Difficulty.BEGINNER) (answer..max + answer).random()
                else (answer..max).random()
            val num2 = num1 - answer
            return Problem(num1, num2, answer)
        }
    },
    MULTIPLICATION("x", 2, "Multiplication") {
        override fun apply(t: Int, u: Int): Int = t * u
        override fun generateProblem(difficulty: Difficulty): Problem {
            val bottomMin =
                when (difficulty) {
                    Difficulty.EASY, Difficulty.BEGINNER -> 0
                    else -> difficulty.mBottom * -1
                }
            val bottomMax = difficulty.mBottom
            val whizSign = intArrayOf(-1, 1).random()
            val topMin =
                when (difficulty) {
                    Difficulty.EASY, Difficulty.BEGINNER -> 0
                    Difficulty.WHIZ -> 100 * whizSign
                    else -> difficulty.mTop * -1
                }
            val topMax =
                if (difficulty == Difficulty.WHIZ) difficulty.mTop * whizSign
                else difficulty.mTop

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
                if (topMin < topMax) (topMin..topMax).random()
                else (topMax..topMin).random()
            val num2 = (bottomMin..bottomMax).random()
            val answer = this.apply(num1, num2)
            return Problem(num1, num2, answer)
        }
    },
    DIVISION("÷", 3, "Division") {
        override fun apply(t: Int, u: Int): Int = t / u
        override fun generateProblem(difficulty: Difficulty): Problem {
            // B: top = <=100, bottom = 1-5, 10
            // E: top = <=120, bottom = 2-9, 11, 12
            // I: top = 0-1000 >= bottom = +/- 0-1000
            // H: top = +/- 0-10000 >= bottom = +/- 0-10000
            // W: top = +/- 0-100000 >= bottom = +/- 0-100000

            if (difficulty == Difficulty.BEGINNER) {
                val range = intArrayOf(1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 10)
                val num1 = range.random()
                val num2 = range.random()
                val top = num1 * num2

                return Problem(top, num1, num2)
            }

            if (difficulty == Difficulty.EASY) {
                val range = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 11, 12)
                val num1 = range.random()
                val num2 =
                    if (num1 in intArrayOf(11, 12)) (2..9).random()
                    else range.random()
                val top = num1 * num2

                return Problem(top, num1, num2)
            }

            val min = difficulty.asdMin
            val max = difficulty.asdMax
            val tops = mutableListOf<Int>()
            val factors = mutableListOf<Int>()

            for (i in (min..max)) {
                if (abs(i) > 1 && i % 5 != 0) {
                    tops.add(i)
                    tops.add(i)
                }
                tops.add(i)
            }

            val num1 = tops.random()

            tops.removeAll(listOf(0,num1, num1 * -1));

            val num2 =
                if (num1 == 0) (min..max).random()
                else {
                    for (i in tops) {
                        if(num1 % abs(i) == 0) factors.add(i)
                    }

                    factors.random() * intArrayOf(-1, 1).random()
                }

            return Problem(num1, num2, num1 / num2)
        }
    };
}