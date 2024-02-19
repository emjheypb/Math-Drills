package com.ubasangg.mathdrills.enums

import com.ubasangg.mathdrills.R

enum class Difficulty(val index: Int, val label: Int, val mdBottom: Int, val mdTop: Int, val asMin: Int, val asMax: Int) {
    EASY(0, R.string.lbl_easy, 9, 99, 0, 100),
    INTERMEDIATE(1, R.string.lbl_intermediate, 99,99, 0, 1000),
    HARD(2, R.string.lbl_hard,  99,999, -10000, 10000),
    WHIZ(3, R.string.lbl_whiz, 999,999, -100000, 100000);
}