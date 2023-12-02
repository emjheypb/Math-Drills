package com.ubasangg.mathdrills.enums

enum class Difficulty(val index: Int, val min: Int, val max: Int) {
    EASY(0, 0, 100),
    INTERMEDIATE(1, -100,100),
    HARD(2,-10000,10000),
    WIZ(3,-1000000,1000000);
}