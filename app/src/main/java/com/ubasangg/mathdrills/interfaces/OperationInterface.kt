package com.ubasangg.mathdrills.interfaces

import com.ubasangg.mathdrills.classes.Problem
import com.ubasangg.mathdrills.enums.Difficulty

interface OperationInterface {
    fun generateProblem(difficulty: Difficulty): Problem
}