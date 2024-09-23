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
    private val cropClick:(Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private val TAG: String = "ThumbnailAdapter"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var coverBitmap: Bitmap? = null
        val coverSIV: ShapeableImageView = itemView.findViewById(R.id.e_p_cover)
        private val deleteIV: ImageView = itemView.findViewById(R.id.e_p_delete)
        private val playIV: ImageView = itemView.findViewById(R.id.e_p_play)
        val configIV: ImageView = itemView.findViewById(R.id.e_p_config)
        val anchorIV: ImageView = itemView.findViewById(R.id.e_p_anchor)

        init {
            deleteIV.setOnClickListener {
                deleteClick(adapterPosition)
//                coverBitmap?.recycle()
            }
            playIV.setOnClickListener {
                val intent = Intent(context, PalActivity::class.java)
                intent.putExtra("MediaIndex", adapterPosition)
                context.startActivity(intent)
            }
            configIV.setOnClickListener{
                cropClick(adapterPosition)
            }
            anchorIV.setOnClickListener{
                DataHolder.mediaList[adapterPosition].rotateQuarter()
                notifyItemChanged(adapterPosition)
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
        holder.coverBitmap?.recycle()
        val displayMediaInfo:MediaInfo = DataHolder.mediaList[position]
        val thumbnailDir = File(context.filesDir, "thumbnail")
        val coverFile = File(thumbnailDir, displayMediaInfo.mediaName)
        if (coverFile.exists()) {
            holder.coverBitmap = BitmapFactory.decodeFile(coverFile.absolutePath)
            holder.coverSIV.setImageBitmap(holder.coverBitmap)
        } else {
            Log.d(TAG, "Missing Thumbnail:${displayMediaInfo.mediaName}")
        }
        if (displayMediaInfo.type == MediaInfo.TYPE_VIDEO){
            holder.configIV.visibility = View.GONE
        }
        when(displayMediaInfo.gravity){
            MediaInfo.GRAVITY_START -> {
                holder.anchorIV.rotation = 90F
            }
            MediaInfo.GRAVITY_TOP -> {
                holder.anchorIV.rotation = 180F
            }
            MediaInfo.GRAVITY_END -> {
                holder.anchorIV.rotation = 270F
            }
            else -> {
                holder.anchorIV.rotation = 0F
            }
        }
    }
}