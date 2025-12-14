package com.light.dungeonofhabits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.R

class CharacterAdapter(
    private val characters: List<CharacterData>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    data class CharacterData(
        val name: String,
        val atk: Int,
        val def: Int,
        val hp: Int,
        val drawableResId: Int
    )

    inner class CharacterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val characterName: TextView = view.findViewById(R.id.characterName)
        val characterImage: ImageView = view.findViewById(R.id.characterImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.character_item, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]
        holder.characterName.text = character.name
        holder.characterImage.setImageResource(character.drawableResId)
        holder.characterImage.scaleX = 1.5f
        holder.characterImage.scaleY = 1.5f
        holder.view.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = characters.size
}
