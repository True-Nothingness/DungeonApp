package com.light.dungeonofhabits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.R
import com.light.dungeonofhabits.models.InventoryItem

class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val onUseClicked: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.item_icon)
        val name: TextView = view.findViewById(R.id.item_name)
        val quantity: TextView = view.findViewById(R.id.item_quantity)
        val useButton: Button = view.findViewById(R.id.btn_use)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.name.text = item.itemName
        holder.quantity.text = "${item.quantity} left"
        holder.useButton.setOnClickListener { onUseClicked(item) }
    }

    override fun getItemCount() = items.size
}
