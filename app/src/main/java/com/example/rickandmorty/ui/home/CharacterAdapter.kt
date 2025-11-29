package com.example.rickandmorty.ui.home

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.rickandmorty.R

class CharacterAdapter(
    private val characters: MutableList<CharacterUi>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var showFooter = false

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == characters.size && showFooter) TYPE_FOOTER else TYPE_ITEM
    }

    override fun getItemCount(): Int = characters.size + if (showFooter) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val row = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER_VERTICAL
            }

            val img = ImageView(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(250, 250)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val textContainer = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(16, 0, 0, 0)
            }

            row.addView(img)
            row.addView(textContainer)
            ItemViewHolder(row)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading_footer, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder && position < characters.size) {
            val character = characters[position]
            val row = holder.itemView as LinearLayout
            val img = row.getChildAt(0) as ImageView
            val textContainer = row.getChildAt(1) as LinearLayout
            textContainer.removeAllViews()

            img.load(character.image)
            img.contentDescription = character.name

            val nameView = TextView(holder.itemView.context).apply {
                text = character.name
                setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.morty_white
                    )
                )
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            val statusView = TextView(holder.itemView.context).apply {
                text = "Статус: ${character.status}"
                textSize = 14f
                setPadding(0, 4, 0, 0)
                setTextColor(
                    when (character.status) {
                        "Alive" -> 0xFF4CAF50.toInt()
                        "Dead" -> 0xFFF44336.toInt()
                        else -> 0xFF9E9E9E.toInt()
                    }
                )
            }

            val speciesView = TextView(holder.itemView.context).apply {
                text = "Вид: ${character.species}"
                textSize = 14f
                setPadding(0, 2, 0, 0)
                setTextColor(
                    when (character.species) {
                        "Human" -> 0xFF2196F3.toInt()
                        "Alien" -> 0xFF9C27B0.toInt()
                        else -> 0xFFFF9800.toInt()
                    }
                )
            }

            textContainer.addView(nameView)
            textContainer.addView(statusView)
            textContainer.addView(speciesView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showLoadingFooter(show: Boolean) {
        if (show != showFooter) {
            showFooter = show
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
