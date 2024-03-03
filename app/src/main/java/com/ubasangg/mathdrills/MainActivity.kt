package com.ubasangg.mathdrills

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ubasangg.mathdrills.classes.HighScore
import com.ubasangg.mathdrills.classes.Level
import com.ubasangg.mathdrills.databinding.ActivityMainBinding
import com.ubasangg.mathdrills.enums.Difficulty
import com.ubasangg.mathdrills.enums.Operation
import com.ubasangg.mathdrills.enums.SharedPrefRef
import com.ubasangg.mathdrills.enums.TimerSeconds
import java.time.LocalDate


class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private val gson = Gson()

    private lateinit var timerButtons: List<Button>
    private lateinit var operationButtons: List<Button>
    private lateinit var difficultyButtons: List<Button>

    private val defaultAttempts = 99

    private var currTimerSeconds: TimerSeconds? = null
    private var currOperation: Operation? = null
    private var currDifficulty: Difficulty? = null
    private var attempts = mutableListOf(0,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        // region load shared preferences
        this.sharedPreferences =
            getSharedPreferences(SharedPrefRef.SHAREDPREF.toString(), MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()
        // endregion

        // region initialize screen controls
        timerButtons = listOf(this.binding.btn60, this.binding.btn180, this.binding.btnCasual)
        operationButtons = listOf(
            this.binding.btnOpAddition,
            this.binding.btnOpSubtraction,
            this.binding.btnOpMultiplication,
            this.binding.btnOpDivision,
            this.binding.btnOpRandom,
            this.binding.btnOpAll
        )
        difficultyButtons = listOf(
            this.binding.btnDifficultyEasy,
            this.binding.btnDifficultyIntermediate,
            this.binding.btnDifficultyHard,
            this.binding.btnDifficultyWhiz,
            this.binding.btnDifficultyBeginner
        )

        val buttons = mutableListOf<Button>()
        buttons.addAll(timerButtons)
        buttons.addAll(operationButtons)
        buttons.addAll(difficultyButtons)

        for (btn in buttons) btn.setOnClickListener(this)
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName //Version Name

        this.binding.btnStart.setOnClickListener(this)
        this.binding.tvVersion.text = getString(R.string.words, "v$version")
        this.binding.tvAttemptsLbl.text = getString(R.string.lbl_attempts_remaining, defaultAttempts)
        // endregion
    }

    override fun onResume() {
        val spDate = this.sharedPreferences.getString(SharedPrefRef.SP_CURR_DATE.toString(), "")
        val levelSP = this.sharedPreferences.getString(SharedPrefRef.SP_LEVEL.toString(), "")
        val level = gson.fromJson(levelSP, Level::class.java)

        val dateToday = LocalDate.now().toString()

        for(timex in TimerSeconds.entries) {
            attempts[timex.index] = this.sharedPreferences.getInt(timex.spName.toString(), defaultAttempts)
        }

        // region check if tries need to be reset
        if (spDate != dateToday) {
            for(timex in TimerSeconds.entries) {
                attempts[timex.index] = this.defaultAttempts
                this.prefEditor.putInt(timex.spName.toString(), defaultAttempts)
            }
            this.prefEditor.putString(SharedPrefRef.SP_CURR_DATE.toString(), dateToday)
            this.prefEditor.apply()
        }
        // endregion

        // region set selected level options
        if (level != null) {
            currTimerSeconds = level.timerSeconds
            currOperation = level.operation
            currDifficulty = level.difficulty

            btnIsSelected(timerButtons, timerButtons[currTimerSeconds!!.index])
            btnIsSelected(operationButtons, operationButtons[currOperation!!.index])
            btnIsSelected(difficultyButtons, difficultyButtons[currDifficulty!!.index])
        }
        // endregion

        super.onResume()
    }

    private fun btnIsSelected(buttons: List<Button>, currBtn: Button) {
        // region set highlighted button based on last user selection
        for (btn in buttons) {
            if (btn == currBtn) btn.setBackgroundColor(getColor(R.color.primary))
            else btn.setBackgroundColor(getColor(R.color.disabled))
        }
        // endregion

        // region set defaults for selected level options
        this.binding.tvHighscoreLbl.text = getString(R.string.lbl_high_score, 0)
        this.binding.tvHighscoreLbl.visibility = View.VISIBLE

        this.binding.tvAttemptsLbl.text = if(currTimerSeconds == null) getString(R.string.lbl_attempts_remaining, 0) else getString(R.string.lbl_attempts_remaining, attempts[currTimerSeconds!!.index])

        val highScoreDS = sharedPreferences.getString(SharedPrefRef.SP_HIGH_SCORES.toString(), "")
        val typeToken = object : TypeToken<List<HighScore>>() {}.type
        if (highScoreDS != "") {
            val highscores = gson.fromJson<List<HighScore>>(highScoreDS, typeToken).toMutableList()
            for (hs in highscores) {
                if (hs.level.timerSeconds == currTimerSeconds && hs.level.operation == currOperation && hs.level.difficulty == currDifficulty) {
                    this.binding.tvHighscoreLbl.text = getString(R.string.lbl_high_score, hs.score)
                    break
                }
            }
        }
        // endregion

        // region set level shared preference
        if(currTimerSeconds != null && currOperation != null && currDifficulty != null) {
            val level =
                gson.toJson(Level(currTimerSeconds!!, currOperation!!, currDifficulty!!))
            this.prefEditor.putString(SharedPrefRef.SP_LEVEL.toString(), level)
            this.prefEditor.apply()

            this.binding.btnStart.isEnabled = attempts[currTimerSeconds!!.index] > 0
        }
        // endregion
    }

    override fun onClick(v: View?) {
        when (v as Button) {
            in timerButtons -> {
                for (timerseconds in TimerSeconds.entries) {
                    if (timerseconds.description == v.text) {
                        currTimerSeconds = timerseconds
                        break
                    }
                }
                btnIsSelected(timerButtons, v)
            }

            in operationButtons -> {
                for (operation in Operation.entries) {
                    if (operation.description == v.text) {
                        currOperation = operation
                        break
                    }
                }
                btnIsSelected(operationButtons, v)
            }

            in difficultyButtons -> {
                currDifficulty = if(v.text.toString() == getString(R.string.lbl_whiz)) {
                    Difficulty.WHIZ
                } else {
                    Difficulty.valueOf(v.text.toString().uppercase())
                }
                btnIsSelected(difficultyButtons, v)
            }

            this.binding.btnStart -> {
                // -1 attempt
//                val attempts =
//                    this.sharedPreferences.getInt(currTimerSeconds!!.spName.toString(), defaultAttempts)
//                this.prefEditor.putInt(currTimerSeconds!!.spName.toString(), attempts - 1)
//                this.prefEditor.apply()

                val intent = Intent(this@MainActivity, DrillStartActivity::class.java)
                startActivity(intent)
            }
        }
    }
}