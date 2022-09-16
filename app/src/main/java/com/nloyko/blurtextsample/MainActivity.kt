package com.nloyko.blurtextsample

import android.annotation.SuppressLint
import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.MaskFilterSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.nloyko.blurtextsample.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val maskFilterSpans = mutableListOf<MaskFilterSpan>()
    private var removeBlurEffectJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.submitButton.setOnClickListener { applyBlurEffect() }
        binding.clearButton.setOnClickListener { clearResults() }
        binding.root.setOnTouchListener { _, _ -> onRootViewTouched() }
    }

    private fun onRootViewTouched(): Boolean {
        hideKeyboard(binding.root)
        return false
    }

    private fun clearResults() {
        removeBlurEffectJob?.cancel()
        hideKeyboard(binding.root)
        binding.resultText.text = ""
        maskFilterSpans.clear()
    }

    private fun applyBlurEffect() {
        clearResults()

        val spannable = SpannableString(binding.initialText.text)

        val padding = 20
        // The trick to add some padding around the text view and prevent a cropping of a blur effect
        binding.resultText.setShadowLayer(padding.toFloat(), 0f, 0f, 0)
        binding.resultText.setPadding(padding, padding, padding, padding)

        binding.textToBlur.text.toString()
            .replace(" ", "")
            .split(",")
            .forEach {
                val startIndex = spannable.indexOf(it)
                if (startIndex != -1) {
                    val span = MaskFilterSpan(BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL))
                    spannable.setSpan(
                        span,
                        startIndex,
                        startIndex + it.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    maskFilterSpans.add(span)
                }
            }

        binding.resultText.setText(spannable, TextView.BufferType.SPANNABLE)

        removeBlurEffectDelayed()
    }

    private fun removeBlurEffectDelayed() {
        removeBlurEffectJob = lifecycleScope.launch {
            whenStarted {
                while (maskFilterSpans.isNotEmpty()) {
                    delay(INTERVAL)
                    val currentSpan = maskFilterSpans.removeFirst()
                    val spannable = binding.resultText.text as Spannable
                    spannable.removeSpan(currentSpan)
                }
            }
        }
    }

    companion object {
        private const val INTERVAL = 5000L
    }
}