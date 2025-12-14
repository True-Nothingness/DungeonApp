package com.light.dungeonofhabits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.models.ShopItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopViewModel : ViewModel() {

    private val _gold = MutableLiveData<Int>()
    val gold: LiveData<Int> = _gold

    private val _shopItems = MutableLiveData<List<ShopItem>>()
    val shopItems: LiveData<List<ShopItem>> = _shopItems

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    init {
        loadShopItems()
        fetchUserGold()
    }

    private fun loadShopItems() {
        _shopItems.value = listOf(
            ShopItem("Small HP Potion", "potion", 25, "heal", 35, R.drawable.small_hp),
            ShopItem("Medium HP Potion", "potion", 50, "heal", 80, R.drawable.medium_hp),
            ShopItem("Large HP Potion", "potion", 100, "heal", 170, R.drawable.big_hp),
            ShopItem("ATK Potion", "statPotion", 170, "atk", 4, R.drawable.atk),
            ShopItem("DEF Potion", "statPotion", 170, "def", 2, R.drawable.def)
        )
    }

    fun fetchUserGold() {
        ApiClient.apiService.getUserProfile().enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    _gold.value = response.body()?.user?.gold ?: 0
                } else {
                    _toastMessage.value = "Failed to load profile"
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {
                _toastMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun buyItem(item: ShopItem, quantity: Int = 1) {
        val buyRequest = mapOf(
            "itemName" to item.name,
            "quantity" to quantity
        )

        ApiClient.apiService.buyItem(buyRequest).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    _toastMessage.value = "Bought $quantity x ${item.name}"
                    fetchUserGold() // Refresh gold after successful purchase
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to buy item"
                    _toastMessage.value = errorMsg
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                _toastMessage.value = "Error: ${t.localizedMessage}"
            }
        })
    }
}
