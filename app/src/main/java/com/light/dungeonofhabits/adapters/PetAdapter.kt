package com.light.dungeonofhabits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.R

class PetAdapter(
    private val pets: List<String>,  // species list
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petImage: ImageView = itemView.findViewById(R.id.petImage)
        val petName: TextView = itemView.findViewById(R.id.species)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_item, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val species = pets[position]
        holder.petName.text = species

        // Set pet image based on species
        val drawableId = getDrawableIdForPet(species)
        holder.petImage.setImageResource(drawableId)
        holder.petImage.scaleX = 1.5f
        holder.petImage.scaleY = 1.5f
    }

    override fun getItemCount() = pets.size

    private fun getDrawableIdForPet(species: String): Int {
        return when (species.lowercase()) {
            "fox" -> R.drawable.fox
            "turtle" -> R.drawable.turtle
            "pig" -> R.drawable.pig
            "scorpion" -> R.drawable.scorpion
            "slime" -> R.drawable.slime
            "squirrel" -> R.drawable.squirrel
            else -> R.drawable.fox
        }
    }
}
