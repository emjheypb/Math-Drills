package com.ubasangg.mathdrills.enums

enum class TimerSeconds(val description: String, val seconds: Int, val index: Int, val spName: SharedPrefRef) {
    SECS60("60 Seconds", 60, 0, SharedPrefRef.SP_ATTEMPTS_60),
    SECS180("180 Seconds", 180, 1, SharedPrefRef.SP_ATTEMPTS_180);
}