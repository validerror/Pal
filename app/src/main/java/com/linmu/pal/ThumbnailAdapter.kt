package com.linmu.pal

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.linmu.pal.entity.MediaInfo
import com.linmu.pal.ui.PalActivity
import java.io.File

class ThumbnailAdapter(
    private val context: Context,
    private val deleteClick: (Int) -> Unit,
    private val settingClick:(Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private val TAG: String = "ThumbnailAdapter"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverSIV: ShapeableImageView = itemView.findViewById(R.id.e_p_cover)
        private val deleteIV: ImageView = itemView.findViewById(R.id.e_p_delete)
        private val playIV: ImageView = itemView.findViewById(R.id.e_p_play)
        val settingIV:ImageView = itemView.findViewById(R.id.e_p_setting)

        init {
            deleteIV.setOnClickListener {
                deleteClick(adapterPosition)
            }
            playIV.setOnClickListener {
                val intent = Intent(context, PalActivity::class.java)
                intent.putExtra("MediaIndex", adapterPosition)
                context.startActivity(intent)
            }
            settingIV.setOnClickListener{
                settingClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.element_thumbnail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = DataHolder.mediaList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.coverSIV.setImageBitmap(DataHolder.thumbnailList[position])
    }
}