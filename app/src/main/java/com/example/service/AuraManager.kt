package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.AuraSignature
import com.example.model.LauncherCoreIdentity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuraManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("empire_aura_settings", Context.MODE_PRIVATE)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // State flows
    private val _auraSignature = MutableStateFlow(AuraSignature.CRIMSON)
    val auraSignature: StateFlow<AuraSignature> = _auraSignature.asStateFlow()

    private val _launcherCore = MutableStateFlow(LauncherCoreIdentity.DEFAULT)
    val launcherCore: StateFlow<LauncherCoreIdentity> = _launcherCore.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _orbType = MutableStateFlow("Neon") // Classic, Energy, Neon
    val orbType: StateFlow<String> = _orbType.asStateFlow()

    private val _orbSizeMultiplier = MutableStateFlow(1.0f)
    val orbSizeMultiplier: StateFlow<Float> = _orbSizeMultiplier.asStateFlow()

    private val _auraBorderMode = MutableStateFlow(true)
    val auraBorderMode: StateFlow<Boolean> = _auraBorderMode.asStateFlow()

    private val _voiceVisualizerEnabled = MutableStateFlow(true)
    val voiceVisualizerEnabled: StateFlow<Boolean> = _voiceVisualizerEnabled.asStateFlow()

    // System Modes
    private val _cyberShield = MutableStateFlow(true)
    val cyberShield: StateFlow<Boolean> = _cyberShield.asStateFlow()

    private val _ultraPerformance = MutableStateFlow(true)
    val ultraPerformance: StateFlow<Boolean> = _ultraPerformance.asStateFlow()

    private val _whisperVoice = MutableStateFlow(false)
    val whisperVoice: StateFlow<Boolean> = _whisperVoice.asStateFlow()

    fun setAuraSignature(signature: AuraSignature) {
        _auraSignature.value = signature
        triggerHaptic()
    }

    fun setLauncherCore(core: LauncherCoreIdentity) {
        _launcherCore.value = core
        triggerHaptic()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        if (enabled) triggerHaptic(force = true)
    }

    fun setOrbType(type: String) {
        _orbType.value = type
        triggerHaptic()
    }

    fun setOrbSizeMultiplier(size: Float) {
        _orbSizeMultiplier.value = size
    }

    fun setAuraBorderMode(enabled: Boolean) {
        _auraBorderMode.value = enabled
        triggerHaptic()
    }

    fun setVoiceVisualizerEnabled(enabled: Boolean) {
        _voiceVisualizerEnabled.value = enabled
        triggerHaptic()
    }

    fun toggleCyberShield() {
        _cyberShield.value = !_cyberShield.value
        triggerHaptic()
    }

    fun toggleUltraPerformance() {
        _ultraPerformance.value = !_ultraPerformance.value
        triggerHaptic()
    }

    fun toggleWhisperVoice() {
        _whisperVoice.value = !_whisperVoice.value
        triggerHaptic()
    }

    fun triggerHaptic(force: Boolean = false) {
        if ((_hapticsEnabled.value || force) && vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35)
                }
            } catch (e: Exception) {
                // Ignore vibration failure
            }
        }
    }
}
