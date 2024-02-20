package com.ubasangg.mathdrills

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ubasangg.mathdrills.classes.HighScore
import com.ubasangg.mathdrills.classes.Level
import com.ubasangg.mathdrills.databinding.ActivityDrillStartBinding
import com.ubasangg.mathdrills.enums.Difficulty
import com.ubasangg.mathdrills.enums.Operation
import com.ubasangg.mathdrills.enums.SharedPrefRef
import com.ubasangg.mathdrills.enums.TimerSeconds


class DrillStartActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityDrillStartBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private val gson = Gson()
    private val defaultAttempts = 99

    private var currTimerSeconds: TimerSeconds? = null
    private var currOperation: Operation? = null
    private var currDifficulty: Difficulty? = null

    private var score = 0
    private var answer: Int = 0
    private val highScores = mutableListOf<HighScore>()
    private var highscore = 0

    private lateinit var buttonsNumber: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrillStartBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        // region load shared preferences
        this.sharedPreferences = getSharedPreferences(SharedPrefRef.SHAREDPREF.toString(), MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        val levelSP = this.sharedPreferences.getString(SharedPrefRef.SP_LEVEL.toString(), "")
        val level = gson.fromJson(levelSP, Level::class.java)
        // endregion

        // region check if valid initial inputs
        if(level != null) {
            // region set current game initial values
            currTimerSeconds = level.timerSeconds
            currOperation = level.operation
            currDifficulty = level.difficulty

            this.binding.tvScore.text = getString(R.string.number, score)
            updateTimer(currTimerSeconds!!.seconds / 60, currTimerSeconds!!.seconds % 60)
            this.binding.tvOperation.text = currOperation!!.description

            // region set high score display
            val highScoreDS = sharedPreferences.getString(SharedPrefRef.SP_HIGH_SCORES.toString(), "")
            val typeToken = object: TypeToken<List<HighScore>>() {}.type
            if (highScoreDS != "") {
                highScores.addAll(gson.fromJson<List<HighScore>>(highScoreDS, typeToken).toMutableList())
                for(hs in highScores) {
                    if(hs.level.timerSeconds == currTimerSeconds && hs.level.operation == currOperation && hs.level.difficulty == currDifficulty) {
                        highscore = hs.score
                        break
                    }
                }
            }
            // endregion
            // endregion

            // start game
            startGame()
        } else {
            finish()
        }
        // endregion

        // region on home button clicks
        this.binding.btnHome.setOnClickListener {
            backToHome()
        }
        this.binding.btnMenuHome.setOnClickListener {
            backToHome()
        }
        // endregion

        // region on try again click
        this.binding.btnTryAgain.setOnClickListener {
            // reset current score
            score = 0
            this.binding.tvScore.text = getString(R.string.number, score)

            // -1 attempt
            val attempts =
                this.sharedPreferences.getInt(currTimerSeconds!!.spName.toString(), defaultAttempts)
            this.prefEditor.putInt(currTimerSeconds!!.spName.toString(), attempts - 1)
            this.prefEditor.apply()

            // hide results screen
            binding.tvResults.visibility = View.GONE
            binding.llGameOverMenu.visibility = View.GONE

            // show keypad
            binding.clKeyboard.visibility = View.VISIBLE
            for(btn in buttonsNumber) btn.isEnabled = true
            binding.btnEquals.isEnabled = true

            // show home menu
            binding.llMenu.visibility = View.VISIBLE

            startGame()
        }
        // endregion

        // region custom keypad
        buttonsNumber = listOf(this.binding.btn0, this.binding.btn1, this.binding.btn2, this.binding.btn3, this.binding.btn4, this.binding.btn5, this.binding.btn6, this.binding.btn7, this.binding.btn8, this.binding.btn9, this.binding.btn0)

        for(btn in buttonsNumber) btn.setOnClickListener(this)
        this.binding.btnClear.setOnClickListener(this)
        this.binding.btnSign.setOnClickListener(this)
        this.binding.btnEquals.setOnClickListener(this)
        this.binding.btnBackspace.setOnClickListener(this)
        // endregion
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome()
            return true;
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun backToHome() {
        if(this.binding.tvResults.visibility == View.VISIBLE) finish()
        else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setTitle("Are you sure you want to quit?")
                .setMessage(
                    if(currTimerSeconds!! == TimerSeconds.CASUAL) "High Score: ${highscore}\nCurrent Score: $score"
                    else "You will lose your attempt and score.")
                .setPositiveButton("CANCEL") { _, _ ->
                    // Do something.
                }
                .setNegativeButton("QUIT") { _, _ ->
                    if(currTimerSeconds!! == TimerSeconds.CASUAL) setHighScore()
                    finish()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun startTimer() {
        val timer = object : CountDownTimer((currTimerSeconds!!.seconds * 1000L), 1000) {
            var count = 0

            override fun onTick(millisUntilFinished: Long) {
                count++
                val seconds = currTimerSeconds!!.seconds - count
                updateTimer(seconds / 60, seconds % 60)
            }

            override fun onFinish() {
                // region game over
                for(btn in buttonsNumber) btn.isEnabled = false
                binding.btnEquals.isEnabled = false

                binding.tvAnswer.text = ""

                // region hide custom keypad
                val animationFadeOut = AnimationUtils.loadAnimation(this@DrillStartActivity, R.anim.fade_out)
                binding.clKeyboard.startAnimation(animationFadeOut)
                Handler().postDelayed({
                    binding.clKeyboard.visibility = View.GONE
                    binding.llMenu.visibility = View.GONE
                }, 200)
                // endregion

                setHighScore()

                // region show game over screen
                val animationFadeIn = AnimationUtils.loadAnimation(this@DrillStartActivity, R.anim.fade_in)
                binding.tvResults.text = getString(R.string.results, currTimerSeconds!!.description, currOperation.toString(), getString(currDifficulty!!.label), score, highscore, sharedPreferences.getInt(currTimerSeconds!!.spName.toString(), 0))
                binding.tvResults.visibility = View.VISIBLE
                binding.llGameOverMenu.visibility = View.VISIBLE
                binding.tvResults.startAnimation(animationFadeIn)
                binding.llGameOverMenu.startAnimation(animationFadeIn)
                // endregion
                // endregion
            }
        }

        timer.start()
    }

    private fun startGame() {
        generateProblem()
        if(currTimerSeconds!! != TimerSeconds.CASUAL) startTimer()
    }

    private fun setHighScore() {
        if(score > highscore) {
            highscore = score
            for(index in highScores.size - 1 downTo 0) {
                if(highScores[index].level.timerSeconds == currTimerSeconds && highScores[index].level.operation == currOperation && highScores[index].level.difficulty == currDifficulty) {
                    highScores.removeAt(index)
                    break
                }
            }
            val currLevel = Level(currTimerSeconds!!, currOperation!!, currDifficulty!!)
            highScores.add(HighScore(currLevel, highscore))
            val highScoresJSON = gson.toJson(highScores)
            prefEditor.putString(SharedPrefRef.SP_HIGH_SCORES.toString(), highScoresJSON)
            prefEditor.apply()
        }
    }

    private fun updateTimer(minutes: Int, seconds: Int) {
        // update timer display
        this.binding.tvTimer.text = getString(R.string.countdown_timer, minutes.toString().padStart(2, '0'), seconds.toString().padStart(2, '0'))
    }

    private fun generateProblem() {
        val difficulty = currDifficulty!!
        val operation = currOperation!!

        val problem = operation.generateProblem(difficulty)
        answer = problem.answer

        this.binding.tvNum1.text = getString(R.string.number, problem.num1)
        this.binding.tvNum2.text = getString(R.string.number, problem.num2)
    }

    override fun onClick(v: View?) {
        // region custom keypad clicks
        val currAns = binding.tvAnswer.text.toString()
        when(v) {
            in buttonsNumber -> {
                val btn = v as Button
                val num = "${btn.text}"
                if (currAns == "0")
                    binding.tvAnswer.text = getString(R.string.words, num)
                else
                    binding.tvAnswer.text = getString(R.string.words, "$currAns$num")
            }
            binding.btnClear -> {
                binding.tvAnswer.text = ""
            }
            binding.btnEquals -> {
                val userAnswer = currAns.toIntOrNull()

                if(userAnswer != null && userAnswer == answer) {
                    score++
                    this.binding.tvScore.text = getString(R.string.number, score)
                } else {
                    val mColors = arrayOf(ColorDrawable(getColor(R.color.red)), ColorDrawable(getColor(R.color.transparent)))
                    val colorTransition = TransitionDrawable(mColors)
                    colorTransition.isCrossFadeEnabled = true
                    binding.tvAnswer.background = colorTransition
                    colorTransition.startTransition(500)
                }

                this.binding.tvAnswer.text = ""
                generateProblem()
            }
            binding.btnSign -> {
                if(currAns.contains("-"))
                    binding.tvAnswer.text = currAns.replace("-", "")
                else {
                    val negative = "-$currAns"
                    binding.tvAnswer.text = negative
                }
            }
            binding.btnBackspace -> {
                if(currAns.isNotEmpty()) binding.tvAnswer.text = currAns.dropLast(1)
            }
        }
        // endregion
    }
}