package com.light.dungeonofhabits

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.light.dungeonofhabits.adapters.InventoryAdapter
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.databinding.ActivityInventoryBinding
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.InventoryItem
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.models.ShopItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var shopItems: List<ShopItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.inventoryRecycler.layoutManager = LinearLayoutManager(this)
        shopItems = listOf(
            ShopItem("Small HP Potion", "potion", 25, "heal", 35, R.drawable.small_hp),
            ShopItem("Medium HP Potion", "potion", 50, "heal", 80, R.drawable.medium_hp),
            ShopItem("Large HP Potion", "potion", 100, "heal", 170, R.drawable.big_hp),
            ShopItem("ATK Potion", "statPotion", 170, "atk", 4, R.drawable.atk),
            ShopItem("DEF Potion", "statPotion", 170, "def", 2, R.drawable.def)
        )
        loadItems()
    }

    private fun loadItems(){
        ApiClient.apiService.getUserProfile().enqueue(object: Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    val profile = response.body() ?: return
                    val userInventory = profile.user.inventory

                    val merged = userInventory.mapNotNull { userItem ->
                        val shopItem = shopItems.find { it.name == userItem.itemName }
                        shopItem?.let {
                            InventoryItem(
                                itemName = it.name,
                                type = it.type,
                                effect = it.effect,
                                amount = it.amount,
                                iconResId = it.iconResId,
                                quantity = userItem.quantity
                            )
                        }
                    }

                    binding.inventoryRecycler.adapter = InventoryAdapter(merged) { item ->
                        useItem(item.itemName)
                    }
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {}
        })
    }

    private fun useItem(itemName: String) {
        val useRequest = mapOf("itemName" to itemName)

        ApiClient.apiService.useItem(useRequest).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@InventoryActivity, "Used $itemName", Toast.LENGTH_SHORT).show()
                    loadItems()
                } else {
                    Toast.makeText(this@InventoryActivity, "Failed to use item: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@InventoryActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}