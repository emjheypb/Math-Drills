package com.ubasangg.mathdrills

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
import java.util.Arrays
import java.util.Locale


class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private val gson = Gson()

    private lateinit var timerButtons: List<ImageButton>
    private lateinit var operationButtons: List<ImageButton>

    private val difficulties = Difficulty.entries
    private val defaultAttempts = 3

    private var currTimerSeconds: TimerSeconds? = null
    private var currOperation: Operation? = null
    private var currDifficulty: Difficulty? = null
    private var attempts = mutableListOf(0, 0, 0)

    private var interstitialAd: InterstitialAd? = null

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
//        this.binding.btnStart.isEnabled = false
        // endregion

        // region ads
        MobileAds.initialize(
            this
        ) { }

        // thing to add
        val reqConfig = RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("8EF0378F82DBA6EE30709DE3B3EC89D4")).build()
        MobileAds.setRequestConfiguration(reqConfig)
        // endregion
    }

    override fun onResume() {
        val spDate = this.sharedPreferences.getString(SharedPrefRef.SP_CURR_DATE.toString(), "")
        val levelSP = this.sharedPreferences.getString(SharedPrefRef.SP_LEVEL.toString(), "")
        val level = gson.fromJson(levelSP, Level::class.java)

        val dateToday = LocalDate.now().toString()

        for (timex in TimerSeconds.entries) {
            attempts[timex.index] =
                this.sharedPreferences.getInt(timex.spName.toString(), defaultAttempts)
        }

        // region check if tries need to be reset
        if (spDate != dateToday) {
            for (timex in TimerSeconds.entries) {
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
        } else {
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
        this.binding.tvAttemptsLbl.text = if (currTimerSeconds == null) getString(
            R.string.number,
            defaultAttempts
        ) else getString(R.string.number, attempts[currTimerSeconds!!.index])
        this.binding.tvHighScoreLbl.text = getString(R.string.number, 0)
        if (attempts[currTimerSeconds!!.index] == 0 && interstitialAd == null) {
            loadInterstitialAd()
        }
        // endregion

        if (currTimerSeconds != null && currOperation != null && currDifficulty != null) {
            // region set level shared preference
            val level =
                gson.toJson(Level(currTimerSeconds!!, currOperation!!, currDifficulty!!))
            this.prefEditor.putString(SharedPrefRef.SP_LEVEL.toString(), level)
            this.prefEditor.apply()

//            this.binding.btnStart.isEnabled = attempts[currTimerSeconds!!.index] > 0
            this.binding.btnStart.background =
                if (attempts[currTimerSeconds!!.index] > 0) getDrawable(R.drawable.btn_long_play)
                else getDrawable(R.drawable.btn_long_ad)
            // endregion

            // region set high score
            val highScoreDS =
                sharedPreferences.getString(SharedPrefRef.SP_HIGH_SCORES.toString(), "")
            if (highScoreDS != "") {
                val typeToken = object : TypeToken<List<HighScore>>() {}.type
                val highScores =
                    gson.fromJson<List<HighScore>>(highScoreDS, typeToken).toMutableList()
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
        currDifficulty =
            difficulties[if (index == difficulties.size) 0 else if (index < 0) 4 else index]
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
                if (attempts[currTimerSeconds!!.index] == 0) {
                    if (interstitialAd == null) loadInterstitialAd()
                    showInterstitial()
                } else {
                    val intent = Intent(this@MainActivity, DrillStartActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    private fun loadInterstitialAd() {
        val TAG = "loadInterstitialAd"
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
                    ad.setFullScreenContentCallback(
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                interstitialAd = null
                                val attempts =
                                    sharedPreferences.getInt(
                                        currTimerSeconds!!.spName.toString(),
                                        defaultAttempts
                                    )
                                prefEditor.putInt(
                                    currTimerSeconds!!.spName.toString(),
                                    attempts + 1
                                )
                                prefEditor.apply()

                                setLevel()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                interstitialAd = null
                                Toast.makeText(
                                    this@MainActivity,
                                    "Failed to Show Ad",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                            }
                        })
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    interstitialAd = null
                    val error: String = String.format(
                        Locale.ENGLISH,
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                    Toast.makeText(this@MainActivity, "Failed to Load Ad", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (interstitialAd != null) interstitialAd!!.show(this)
    }
}