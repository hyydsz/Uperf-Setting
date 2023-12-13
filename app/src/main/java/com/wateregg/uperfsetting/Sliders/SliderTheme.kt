package com.wateregg.uperfsetting.Sliders

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.litao.slider.NiftySlider
import com.litao.slider.effect.AnimationEffect
import com.litao.slider.effect.AnimationEffect.OnAnimationChangeListener
import com.wateregg.uperfsetting.Data.Data
import com.wateregg.uperfsetting.Dialog.SettingDialog.SliderThemes

class SliderTheme {
    companion object {
        fun addSliderTheme(slider: NiftySlider) {
            when (Data.MainSliderTheme) {
                SliderThemes.Normal -> NormalTheme(slider)
                SliderThemes.Bilibili -> BiliBiliTheme(slider)
            }
        }

        fun NormalTheme(slider: NiftySlider) {
            val trackColor = Color.parseColor(String.format("#%06X", 0xFFFFFF and Data.MainColor));

            slider.apply {
                setTrackTintList(ColorStateList.valueOf(trackColor))
                setTrackInactiveTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(trackColor, 0x22)))
            }
        }

        fun BiliBiliTheme(slider: NiftySlider) {
            val trackColor = Color.parseColor("#ff6699")

            val biliBiliDrawable = BiliBiliDrawable()
            biliBiliDrawable.callback = slider

            val animEffect = AnimationEffect(slider).apply {

                animDuration = 300

                srcTrackHeight = 8
                srcThumbRadius = 4

                targetThumbRadius = 15
                targetThumbHeight = 15

                animationListener = object : OnAnimationChangeListener {
                    override fun onEnd(slider: NiftySlider) {
                        biliBiliDrawable.startBlinkAnim()
                    }
                }
            }

            slider.apply {
                effect = animEffect

                setThumbCustomDrawable(biliBiliDrawable)

                setTrackTintList(ColorStateList.valueOf(trackColor))
                setTrackInactiveTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(trackColor, 0x22)))
                setHaloTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 0x33)))

                addOnValueChangeListener { niftySlider: NiftySlider, fl: Float, b: Boolean ->
                    biliBiliDrawable.currentStateValue = fl
                }
            }
        }
    }
}