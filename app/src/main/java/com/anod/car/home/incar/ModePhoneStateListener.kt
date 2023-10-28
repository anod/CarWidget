package com.anod.car.home.incar

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import info.anodsplace.applog.AppLog
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import java.util.*

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
                AppLog.i("Call state idle")
                audioManager.isSpeakerphoneOn = false
                cancelAnswerTimer()
                answered = false
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                AppLog.i("Call state off hook")
                if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
                    AppLog.d("Enable speakerphone while off hook")
                    audioManager.isSpeakerphoneOn = true
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                AppLog.i("Call state ringing")
                if (answerMode == info.anodsplace.carwidget.content.preferences.InCarInterface.AUTOANSWER_IMMEDIATLY) {
                    AppLog.d("Check if already answered")
                    if (!answered) {
                        AppLog.d("Answer immediately")
                        answerCall()
                        answered = true
                    }
                } else if (answerMode == info.anodsplace.carwidget.content.preferences.InCarInterface.AUTOANSWER_DELAY_5) {
                    AppLog.d("Check if already answered")
                    if (!answered) {
                        AppLog.i("Answer delayed")
                        answerCallDelayed()
                        answered = true
                    }
                } else {
                    AppLog.i("Cancel answer timer")
                    cancelAnswerTimer()
                }
            }
        }
        AppLog.e("Call state <unknown>: $state")
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
            }, answerDelayMs.toLong())
        }
    }

    private fun answerCall() {
        if (state != TelephonyManager.CALL_STATE_RINGING) {
            return
        }
        answerPhoneHeadsethook(context)
        audioManager.isMicrophoneMute = false

        if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
            AppLog.i("Enable speakerphone while ringing")
            audioManager.isSpeakerphoneOn = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun answerPhoneHeadsethook(context: Context) {
        if (AppPermissions.isGranted(context, AppPermission.AnswerPhoneCalls) || AppPermissions.isGranted(context, AppPermission.PhoneStateModify)) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.acceptRingingCall()
            if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
                AppLog.i("Enable speakerphone")
                audioManager.isSpeakerphoneOn = true
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, info.anodsplace.carwidget.content.R.string.answer_error_oreo, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val answerDelayMs = 5000
    }
}