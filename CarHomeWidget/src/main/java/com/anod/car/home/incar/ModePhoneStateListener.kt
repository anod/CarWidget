package com.anod.car.home.incar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.utils.AnswerPhoneCalls
import com.anod.car.home.utils.AppPermissions
import com.anod.car.home.utils.ModifyPhoneState
import info.anodsplace.framework.AppLog
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
                if (answerMode == InCarInterface.AUTOANSWER_IMMEDIATLY) {
                    AppLog.d("Check if already answered")
                    if (!answered) {
                        AppLog.d("Answer immediately")
                        answerCall()
                        answered = true
                    }
                } else if (answerMode == InCarInterface.AUTOANSWER_DELAY_5) {
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
            AppLog.d("Enable speakerphone while ringing")
            audioManager.isSpeakerphoneOn = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun answerPhoneHeadsethook(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (AppPermissions.isGranted(context, AnswerPhoneCalls) || AppPermissions.isGranted(context, ModifyPhoneState)) {
                telecomManager.acceptRingingCall()
                if (autoSpeaker && !audioManager.isSpeakerphoneOn) {
                    AppLog.d("Enable speakerphone in AcceptCallActivity")
                    audioManager.isSpeakerphoneOn = true
                }
            }
        }

        val intent = Intent(context, AcceptCallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.putExtra(AcceptCallActivity.EXTRA_ENABLE_SPEAKER, autoSpeaker)
        context.startActivity(intent)
    }

    companion object {
        private const val answerDelayMs = 5000
    }

}
