package com.example.userinterfaceapp

import android.R
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load

data class Character(
    val name: String,
    val image: String,
    val status: String,
    val species: String
)

class CharacterAdapter(private val characters: List<Character>) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val row = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val img = ImageView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(250, 250)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val textContainer = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(16, 0, 0, 0)
        }

        row.addView(img)
        row.addView(textContainer)

        return CharacterViewHolder(row)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]
        val row = holder.itemView as LinearLayout
        val img = row.getChildAt(0) as ImageView
        val textContainer = row.getChildAt(1) as LinearLayout

        textContainer.removeAllViews()

        img.load(character.image)
        img.contentDescription = character.name

        val info = TextView(holder.itemView.context).apply {
            text = character.name
            setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.morty_white))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        }

        val statusView = TextView(holder.itemView.context).apply {
            text = "Статус: ${character.status}"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
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
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 2, 0, 0)
            setTextColor(
                when (character.species) {
                    "Human" -> 0xFF2196F3.toInt()
                    "Alien" -> 0xFF9C27B0.toInt()
                    else -> 0xFFFF9800.toInt()
                }
            )
        }

        textContainer.addView(info)
        textContainer.addView(statusView)
        textContainer.addView(speciesView)
    }

    override fun getItemCount(): Int = characters.size
}