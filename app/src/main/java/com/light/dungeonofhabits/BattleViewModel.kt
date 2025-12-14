package com.light.dungeonofhabits

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.BattleResponse
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.models.User
import com.light.dungeonofhabits.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BattleViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _battleResult = MutableLiveData<BattleResponse?>()
    val battleResult: LiveData<BattleResponse?> = _battleResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _highestFloor = MutableLiveData<Int>()
    val highestFloor: LiveData<Int> = _highestFloor

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val prefs = application.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE)

    init {
        fetchUserProfile()
        loadHighestFloor()
    }

    private fun loadHighestFloor() {
        _highestFloor.value = prefs.getInt(Constants.HIGHEST_FLOOR, -1) // -1 indicates no record
    }

    fun fetchUserProfile() {
        _isLoading.value = true
        ApiClient.apiService.getUserProfile().enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _user.value = response.body()?.user
                } else {
                    _toastMessage.value = "Failed to load user profile"
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {
                _isLoading.value = false
                _toastMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun startBattle() {
        _isLoading.value = true
        ApiClient.apiService.runBattle().enqueue(object : Callback<BattleResponse> {
            override fun onResponse(call: Call<BattleResponse>, response: Response<BattleResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val newRecord = response.body()?.highestFloorReached ?: 0
                    if (newRecord > (_highestFloor.value ?: -1)) {
                        _highestFloor.value = newRecord
                        prefs.edit().putInt(Constants.HIGHEST_FLOOR, newRecord).apply()
                    }
                    _battleResult.value = response.body()
                    fetchUserProfile()
                } else {
                    _toastMessage.value = response.errorBody()?.string() ?: "Battle failed"
                    _battleResult.value = null
                }
            }

            override fun onFailure(call: Call<BattleResponse>, t: Throwable) {
                _isLoading.value = false
                _toastMessage.value = "Network error: ${t.message}"
                _battleResult.value = null
            }
        })
    }

    fun consumeBattleResult() {
        _battleResult.value = null
    }
}
