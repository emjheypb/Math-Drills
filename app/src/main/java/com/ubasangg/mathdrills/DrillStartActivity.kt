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
import android.widget.ImageButton
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
    private val countdownStart = 4

    private var currTimerSeconds: TimerSeconds? = null
    private var currOperation: Operation? = null
    private var currDifficulty: Difficulty? = null

    private var score = 0
    private var answer: Int = 0
    private val highScores = mutableListOf<HighScore>()
    private var highscore = 0
    private var gameover = false

    private lateinit var numberButtons: List<ImageButton>
    private val numpadButtons = mutableListOf<ImageButton>()

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
            this.binding.tvOperation.text = currOperation!!.sign

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

            // start countdown
            startCountdown()
        } else {
            finish()
        }
        // endregion

        // region setOnClickListeners
        numberButtons = listOf(this.binding.btn0, this.binding.btn1, this.binding.btn2, this.binding.btn3, this.binding.btn4, this.binding.btn5, this.binding.btn6, this.binding.btn7, this.binding.btn8, this.binding.btn9, this.binding.btn0)
        numpadButtons.addAll(numberButtons)
        numpadButtons.add(this.binding.btnClear)
        numpadButtons.add(this.binding.btnSign)
        numpadButtons.add(this.binding.btnEquals)
        numpadButtons.add(this.binding.btnBackspace)
        numpadButtons.add(this.binding.btnQuit)

        for(btn in numpadButtons) {
            btn.setOnClickListener(this)
            btn.isEnabled = false
        }
        this.binding.btnTryAgain.setOnClickListener(this)
        this.binding.btnHome.setOnClickListener(this)
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
        if(gameover) finish()
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

    private fun startGame() {
        // hide results screen
        binding.llGameOver.visibility = View.GONE

        // enable keypad
        for(btn in numpadButtons) btn.isEnabled = true

        // enable quit
        binding.btnQuit.isEnabled = true

        generateProblem()
        if(currTimerSeconds!! != TimerSeconds.CASUAL) startTimer()
    }

    private fun gameOver() {
        gameover = true
        for(btn in numpadButtons) btn.isEnabled = false

        binding.tvAnswer.text = ""

        // region hide custom keypad
        val animationFadeOut = AnimationUtils.loadAnimation(this@DrillStartActivity, R.anim.fade_out)
        binding.clKeyboard.startAnimation(animationFadeOut)
        Handler().postDelayed({
            binding.btnQuit.isEnabled = false
        }, 200)
        // endregion

        setHighScore()

        binding.tvResults.text = getString(R.string.results, currTimerSeconds!!.description, currOperation.toString(), getString(currDifficulty!!.label), score)
        this.binding.tvHighScoreLbl.text = getString(R.string.number, highscore)
        this.binding.tvAttemptsLbl.text = getString(R.string.number, sharedPreferences.getInt(currTimerSeconds!!.spName.toString(), 0))

        // region show game over screen
        val animationFadeIn = AnimationUtils.loadAnimation(this@DrillStartActivity, R.anim.fade_in)
        binding.llGameOver.visibility = View.VISIBLE
        binding.llGameOver.startAnimation(animationFadeIn)
        // endregion
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
                gameOver()
            }
        }

        timer.start()
    }

    private fun startCountdown() {
        gameover = false

        // hide results
        this.binding.llGameOver.visibility = View.GONE

        // -1 attempt
        val attempts =
            this.sharedPreferences.getInt(currTimerSeconds!!.spName.toString(), defaultAttempts)
        this.prefEditor.putInt(currTimerSeconds!!.spName.toString(), attempts - 1)
        this.prefEditor.apply()

        // reset problem
        this.binding.tvNum1.text = getString(R.string.number, 0)
        this.binding.tvNum2.text = getString(R.string.number, 0)

        // reset current score
        score = 0
        this.binding.tvScore.text = getString(R.string.number, score)

        // show countdown
        this.binding.llCountdown.visibility = View.VISIBLE

        // set countdown
        val timer = object : CountDownTimer((countdownStart * 1000L), 1000) {
            var count = 0

            override fun onTick(millisUntilFinished: Long) {
                count++
                val seconds = countdownStart - count
                updateCountdown(seconds % 60)
            }

            override fun onFinish() {
                binding.tvCountdown.text = ""
                binding.llCountdown.visibility = View.GONE
                startGame()
            }
        }

        timer.start()
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

    private fun updateCountdown(seconds: Int) {
        // update curtain countdown display
        this.binding.tvCountdown.text = if(seconds != 0) getString(R.string.number, seconds) else getString(R.string.lbl_start)
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
        val currAns = binding.tvAnswer.text.toString()
        when(v) {
            in numberButtons -> {
                val btn = v as ImageButton
                val num = "${btn.contentDescription}"
                if (currAns == "0")
                    binding.tvAnswer.text = getString(R.string.words, num)
                else if (currAns == "-0")
                    binding.tvAnswer.text = getString(R.string.words, "-${num}")
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
            binding.btnTryAgain -> {
                startCountdown()
            }
            binding.btnHome, binding.btnQuit -> {
                backToHome()
            }
        }
    }
}