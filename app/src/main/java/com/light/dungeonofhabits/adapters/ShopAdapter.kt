package com.light.dungeonofhabits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.R
import com.light.dungeonofhabits.models.ShopItem

class ShopAdapter(
    private val items: List<ShopItem>,
    private val onBuyClicked: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.item_icon)
        val name: TextView = view.findViewById(R.id.item_name)
        val price: TextView = view.findViewById(R.id.item_price)
        val buyButton: Button = view.findViewById(R.id.btn_buy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.name.text = item.name
        holder.price.text = "${item.price} gold"
        holder.buyButton.setOnClickListener { onBuyClicked(item) }
    }

    override fun getItemCount() = items.size
}
