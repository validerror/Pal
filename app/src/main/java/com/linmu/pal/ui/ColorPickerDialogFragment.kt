package com.linmu.pal.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.linmu.pal.R

class ColorPickerDialogFragment(
    private val originalColor: Int,
    private val applyColor: (Int) -> Unit
) : DialogFragment() {

    private lateinit var previewIV: ImageView
    private lateinit var aSB: SeekBar
    private lateinit var rSB: SeekBar
    private lateinit var gSB: SeekBar
    private lateinit var bSB: SeekBar
    private lateinit var cancelTV: TextView
    private lateinit var applyTV: TextView
    private var newColor: Int = originalColor
    private var newAlpha: Int = 0
    private var newR: Int = 0
    private var newG: Int = 0
    private var newB: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val pickColorDialog = inflater.inflate(R.layout.dialogfragment_pickcolor, container, false)
        previewIV = pickColorDialog.findViewById(R.id.dfpc_preview)
        aSB = pickColorDialog.findViewById(R.id.dfpc_sb_a)
        rSB = pickColorDialog.findViewById(R.id.dfpc_sb_r)
        gSB = pickColorDialog.findViewById(R.id.dfpc_sb_g)
        bSB = pickColorDialog.findViewById(R.id.dfpc_sb_b)
        cancelTV = pickColorDialog.findViewById(R.id.dfpc_cancel)
        applyTV = pickColorDialog.findViewById(R.id.dfpc_apply)

        cancelTV.setOnClickListener {
            applyColor(originalColor)
            this.dismiss()
        }

        applyTV.setOnClickListener {
            newColor = Color.argb(newAlpha, newR, newG, newB)
            applyColor(newColor)
            this.dismiss()
        }

        setSeekBarOriginal(originalColor)
        setPreview(originalColor)
        argbEvent()

        return pickColorDialog
    }

    private fun setPreview(argbColor: Int) {
        previewIV.setImageDrawable(ColorDrawable(argbColor))
    }

    private fun updatePreview(alpha: Int, argbR: Int, argbG: Int, argbB: Int) {
        previewIV.setImageDrawable(ColorDrawable(Color.argb(alpha, argbR, argbG, argbB)))
    }

    private fun setSeekBarOriginal(argbColor: Int) {
        newAlpha = getAlpha(argbColor)
        aSB.progress = newAlpha
        newR = getR(argbColor)
        rSB.progress = newR
        newG = getG(argbColor)
        gSB.progress = newG
        newB = getB(argbColor)
        bSB.progress = newB
    }

    private fun argbEvent() {
        aSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    newAlpha = progress
                    updatePreview(newAlpha, newR, newG, newB)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        rSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    newR = progress
                    updatePreview(newAlpha, newR, newG, newB)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        gSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    newG = progress
                    updatePreview(newAlpha, newR, newG, newB)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        bSB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    newB = progress
                    updatePreview(newAlpha, newR, newG, newB)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getAlpha(argbColor: Int): Int {
        return (argbColor shr 24) and 0xFF
    }

    private fun getR(argbColor: Int): Int {
        return (argbColor shr 16) and 0xFF
    }

    private fun getG(argbColor: Int): Int {
        return (argbColor shr 8) and 0xFF
    }

    private fun getB(argbColor: Int): Int {
        return argbColor and 0xFF
    }
}