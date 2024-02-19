package com.ubasangg.mathdrills.enums

import com.ubasangg.mathdrills.R

enum class Difficulty(val index: Int, val label: Int, val min: Int, val max: Int, val minAnswer: Int, val maxAnswer: Int) {
    EASY(0, R.string.lbl_easy, 0, 100, 0, 100),
    INTERMEDIATE(1, R.string.lbl_intermediate, -1000,1000, -1000, 1000),
    HARD(2, R.string.lbl_hard,  -10000,10000, -10000, 10000),
    WHIZ(3, R.string.lbl_whiz, -100000,100000, -100000, 100000);
}