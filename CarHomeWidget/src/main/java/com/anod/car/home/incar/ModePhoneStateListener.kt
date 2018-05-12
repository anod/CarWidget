package com.anod.car.home.incar

import com.anod.car.home.prefs.preferences.InCar
import info.anodsplace.framework.AppLog

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log

import java.util.Timer
import java.util.TimerTask


class ModePhoneStateListener(private val context: Context, private val audioManager: AudioManager) : PhoneStateListener() {

    private var answered = false
    private var answerTimer: Timer? = null
    private var autoSpeaker: Boolean = false
    private var answerMode: String? = null
    private var state = -1

    fun setActions(useAutoSpeaker: Boolean, autoAnswerMode: String) {
        autoSpeaker = useAutoSpeaker
        answerMode = autoAnswerMode
    }

    fun cancelActions() {
        audioManager.isSpeakerphoneOn = false
        cancelAnswerTimer()
    }

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        this.state = state
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                AppLog.d("Call state idle")
                audioManager.isSpeakerphoneOn = false
                cancelAnswerTimer()
                answered = false
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                AppLog.d("Call state off hook")
                if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
                    AppLog.d("Enable speakerphone while off hook")
                    audioManager.isSpeakerphoneOn = true
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                AppLog.d("Call state ringing")
                if (answerMode == InCar.AUTOANSWER_IMMEDIATLY) {
                    AppLog.d("Check if already answered")
                    if (!answered) {
                        AppLog.d("Answer immediately")
                        answerCall()
                        answered = true
                    }
                } else if (answerMode == InCar.AUTOANSWER_DELAY_5) {
                    AppLog.d("Check if already answered")
                    if (!answered) {
                        AppLog.d("Answer delayed")
                        answerCallDelayed()
                        answered = true
                    }
                } else {
                    AppLog.d("Cancel answer timer")
                    cancelAnswerTimer()
                }
            }
        }
        AppLog.d("Call state <unknown>: $state")
    }

    private fun cancelAnswerTimer() {
        if (answerTimer != null) {
            answerTimer!!.cancel()
            answerTimer = null
        }
    }

    private fun answerCallDelayed() {
        if (answerTimer == null) {
            answerTimer = Timer()
            answerTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    answerCall()
                }
            }, ANSWER_DALAY_MS.toLong())
        }
    }

    private fun answerCall() {
        if (state != TelephonyManager.CALL_STATE_RINGING) {
            return
        }
        answerPhoneHeadsethook(context)
        audioManager.isMicrophoneMute = false

        if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
            AppLog.d("Enable speakerphone while ringing")
            audioManager.isSpeakerphoneOn = true
        }
    }

    private fun answerPhoneHeadsethook(context: Context) {

        val intent = Intent(context, AcceptCallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.putExtra(AcceptCallActivity.EXTRA_ENABLE_SPEAKER, autoSpeaker)
        context.startActivity(intent)
    }

    companion object {
        private const val ANSWER_DALAY_MS = 5000
    }


}
