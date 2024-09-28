package com.linmu.pal.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.linmu.pal.DataHolder
import com.linmu.pal.R
import com.linmu.pal.entity.MediaInfo
import com.linmu.pal.mediahandler.CropImageLauncher
import com.linmu.pal.utils.DeviceMeta
import java.io.File

class MediaSettingDialogFragment(
    private val settingPosition: Int,
    private val cropImageLauncher: CropImageLauncher
) : DialogFragment() {
    private val TAG: String = "SettingDialog"

    private lateinit var context: Context
    private lateinit var settingsContainer: LinearLayout
    private var thumbnailName: String = "Unknown"
    private var thumbnailBitmap: Bitmap? = null
    private lateinit var newMediaInfo: MediaInfo
    private lateinit var thumbnailSIV: ShapeableImageView
    private lateinit var cancelTV: TextView
    private lateinit var applyTV: TextView

    // settingDimension
    private lateinit var settingDimension: View
    private lateinit var dWidthTV: TextView
    private lateinit var mWidthTV: TextView
    private lateinit var dHeightTV: TextView
    private lateinit var mHeightTV: TextView
    private lateinit var dRatioTV: TextView
    private lateinit var mRatioTV: TextView
    private lateinit var cropTV: TextView

    override fun onAttach(parentContext: Context) {
        super.onAttach(parentContext)
        context = parentContext
    }

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
        // set the window RoundCorner, have to set transparent first
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // copy the mediainfo to store new change
        newMediaInfo = DataHolder.mediaList[settingPosition].deepCopy()
        thumbnailName = newMediaInfo.mediaName
        // dialog layout
        val settingDialog = inflater.inflate(R.layout.dialogfragment_setting, container, false)
        thumbnailSIV = settingDialog.findViewById(R.id.df_thumbnail)
        cancelTV = settingDialog.findViewById(R.id.df_cancel)
        applyTV = settingDialog.findViewById(R.id.df_apply)
        settingsContainer = settingDialog.findViewById(R.id.df_setting)
        setThumbnail()
        // click event
        cancelTV.setOnClickListener {
            this.dismiss()
        }
        applyTV.setOnClickListener {
            DataHolder.mediaList[settingPosition] = newMediaInfo
            Toast.makeText(context, "Apply Success", Toast.LENGTH_SHORT).show()
            this.dismiss()
        }
        // add settingItem to the settingContainer
        addSettings(inflater)
        return settingDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailBitmap?.recycle()
    }

    private fun setThumbnail() {

        if (newMediaInfo.type == MediaInfo.TYPE_IMAGE) {
            val thumbnailDir = File(context.filesDir, "media")
            val thumbnailFile = File(thumbnailDir, thumbnailName)
            thumbnailBitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
            thumbnailSIV.setImageBitmap(thumbnailBitmap)
        } else {
            val thumbnailDir = File(context.filesDir, "thumbnail")
            val thumbnailFile = File(thumbnailDir, thumbnailName)
            thumbnailBitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
            thumbnailSIV.setImageBitmap(thumbnailBitmap)
        }
    }

    override fun onResume() {
        super.onResume()
        setThumbnail()
        newMediaInfo = DataHolder.mediaList[settingPosition].deepCopy()
        updateDimension()
    }


    /**
     * This is a function list that contains many settings.
     * Recommend add new setting function here. All settings have the access to newMediaInfo
     *
     * @param inflater use to inflate layout
     */
    private fun addSettings(inflater: LayoutInflater) {
        addDimensionTable(inflater)
        addEnableClockSwitch(inflater)
        addElementGravitySetting(inflater)
    }

    private fun addDimensionTable(inflater: LayoutInflater) {
        settingDimension = inflater.inflate(R.layout.setting_dimension, settingsContainer, false)
        dWidthTV = settingDimension.findViewById(R.id.sd_dWidth)
        mWidthTV = settingDimension.findViewById(R.id.sd_mWidth)
        dHeightTV = settingDimension.findViewById(R.id.sd_dHeight)
        mHeightTV = settingDimension.findViewById(R.id.sd_mHeight)
        dRatioTV = settingDimension.findViewById(R.id.sd_dRatio)
        mRatioTV = settingDimension.findViewById(R.id.sd_mRatio)
        cropTV = settingDimension.findViewById(R.id.sd_crop)

        updateDimension()

        if (newMediaInfo.type == MediaInfo.TYPE_IMAGE) {
            cropTV.setOnClickListener {
                cropImageLauncher.launch(settingPosition)
            }
        } else {
            cropTV.visibility = View.GONE
        }

        settingsContainer.addView(settingDimension)
    }

    private fun updateDimension() {
        var (dWidth, dHeight) = DeviceMeta.getMaxBounds(context)
        if (newMediaInfo.orientation == MediaInfo.ORIENTATION_LANDSCAPE){
            val tmp = dWidth
            dWidth = dHeight
            dHeight = tmp
        }
        dWidthTV.text = dWidth.toString()
        dHeightTV.text = dHeight.toString()
        val dRatio = dWidth.toDouble() / dHeight.toDouble()
        dRatioTV.text = String.format("%.2f", dRatio)
        mWidthTV.text = newMediaInfo.mediaWidth.toString()
        mHeightTV.text = newMediaInfo.mediaHeight.toString()
        val mRatio = newMediaInfo.mediaWidth.toDouble() / newMediaInfo.mediaHeight.toDouble()
        mRatioTV.text = String.format("%.2f", mRatio)
    }

    private fun addEnableClockSwitch(inflater: LayoutInflater) {
        val enableClockSwitch =
            inflater.inflate(R.layout.element_switchsetting, settingsContainer, false)
        val settingNameTV: TextView = enableClockSwitch.findViewById(R.id.ess_tv_settingName)
        settingNameTV.text = resources.getText(R.string.enableClock)
        val switch: MaterialSwitch = enableClockSwitch.findViewById(R.id.ess_ms_switch)
        switch.isChecked = newMediaInfo.enableClock
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            newMediaInfo.enableClock = isChecked
        }
        settingsContainer.addView(enableClockSwitch)
    }

    private fun addElementGravitySetting(inflater: LayoutInflater) {
        val elementGravitySetting =
            inflater.inflate(R.layout.setting_gravity, settingsContainer, false)
        val gravityView: ImageView = elementGravitySetting.findViewById(R.id.sg_valueView)
        gravityView.rotation = 90F * ((newMediaInfo.gravity + 1) % 4)
        gravityView.setOnClickListener {
            newMediaInfo.rotateQuarter()
            gravityView.rotation = 90F * ((newMediaInfo.gravity + 1) % 4)
        }
        settingsContainer.addView(elementGravitySetting)
    }
}