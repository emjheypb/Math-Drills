package com.ubasangg.mathdrills

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
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

    private lateinit var timerButtons: List<ImageButton>
    private lateinit var operationButtons: List<ImageButton>

    private val difficulties = Difficulty.entries
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
        )

        val buttons = mutableListOf<ImageButton>()
        buttons.addAll(timerButtons)
        buttons.addAll(operationButtons)
        buttons.add(this.binding.btnStart)
        buttons.add(this.binding.btnPrevDifficulty)
        buttons.add(this.binding.btnNextDifficulty)

        for (btn in buttons) btn.setOnClickListener(this)

        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName //Version Name

        this.binding.tvVersion.text = getString(R.string.words, "v$version")
        this.binding.btnStart.isEnabled = false
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
        }
        else {
            currTimerSeconds = TimerSeconds.SECS60
            currOperation = Operation.ADDITION
            currDifficulty = difficulties[1]

            btnIsSelected(timerButtons, timerButtons[currTimerSeconds!!.index])
            btnIsSelected(operationButtons, operationButtons[currOperation!!.index])
        }
        // endregion

        setLevel()
        super.onResume()
    }

    private fun setLevel() {
        this.binding.tvDifficulty.text = getString(currDifficulty!!.label)

        // region set attempts & high score
        this.binding.tvAttemptsLbl.text = if(currTimerSeconds == null) getString(R.string.number, defaultAttempts) else getString(R.string.number, attempts[currTimerSeconds!!.index])
        this.binding.tvHighScoreLbl.text = getString(R.string.number, 0)
        // endregion

        if(currTimerSeconds != null && currOperation != null && currDifficulty != null) {
            // region set level shared preference
            val level =
                gson.toJson(Level(currTimerSeconds!!, currOperation!!, currDifficulty!!))
            this.prefEditor.putString(SharedPrefRef.SP_LEVEL.toString(), level)
            this.prefEditor.apply()

            this.binding.btnStart.isEnabled = attempts[currTimerSeconds!!.index] > 0
            // endregion

            // region set high score
            val highScoreDS = sharedPreferences.getString(SharedPrefRef.SP_HIGH_SCORES.toString(), "")
            if (highScoreDS != "") {
                val typeToken = object : TypeToken<List<HighScore>>() {}.type
                val highScores = gson.fromJson<List<HighScore>>(highScoreDS, typeToken).toMutableList()
                for (hs in highScores) {
                    if (hs.level.timerSeconds == currTimerSeconds && hs.level.operation == currOperation && hs.level.difficulty == currDifficulty) {
                        this.binding.tvHighScoreLbl.text = getString(R.string.number, hs.score)
                        break
                    }
                }
            }
            // endregion
        }
    }

    private fun btnIsSelected(buttons: List<ImageButton>, currBtn: ImageButton) {
        // set highlighted button based on last user selection
        for (btn in buttons) {
            btn.isActivated = btn == currBtn
        }
    }

    private fun setDifficulty(step: Int) {
        val index = currDifficulty!!.index + step
        currDifficulty = difficulties[if(index == difficulties.size) 0 else if(index < 0) 4 else index]
        this.binding.tvDifficulty.text = getString(currDifficulty!!.label)
    }

    override fun onClick(v: View?) {
        when (v as ImageButton) {
            in timerButtons -> {
                for (timerSeconds in TimerSeconds.entries) {
                    if (timerSeconds.description == v.contentDescription) {
                        currTimerSeconds = timerSeconds
                        break
                    }
                }
                btnIsSelected(timerButtons, v)
                setLevel()
            }

            in operationButtons -> {
                for (operation in Operation.entries) {
                    if (operation.description == v.contentDescription) {
                        currOperation = operation
                        break
                    }
                }
                btnIsSelected(operationButtons, v)
                setLevel()
            }

            this.binding.btnNextDifficulty -> {
                setDifficulty(1)
                setLevel()
            }

            this.binding.btnPrevDifficulty -> {
                setDifficulty(-1)
                setLevel()
            }

            this.binding.btnStart -> {
                val intent = Intent(this@MainActivity, DrillStartActivity::class.java)
                startActivity(intent)
            }
        }
    }
}